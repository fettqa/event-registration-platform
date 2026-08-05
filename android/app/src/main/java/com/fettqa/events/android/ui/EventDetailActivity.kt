package com.fettqa.events.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import com.fettqa.events.android.R
import com.fettqa.events.android.data.AppServices
import com.fettqa.events.android.data.errorMessage
import com.fettqa.events.android.databinding.ActivityEventDetailBinding
import com.fettqa.events.android.model.EventRegistrationResponse
import com.fettqa.events.android.model.EventResponse
import com.fettqa.events.android.model.PageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityEventDetailBinding
    private lateinit var registrationsAdapter: RegistrationAdapter
    private var eventId: Long = -1L
    private var currentEvent: EventResponse? = null
    private var registrationsQuery: String = ""
    private var registrationsPage: Int = 0
    private var registrationsTotalPages: Int = 0
    private var registrationsTotalCount: Int = 0
    private var pageEmails: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        if (eventId < 0) {
            finish()
            return
        }

        registrationsAdapter = RegistrationAdapter(
            canDelete = { reg ->
                val session = AppServices.session(this)
                session.isLoggedIn() &&
                    session.canDeleteRegistration(currentEvent?.createdByEmail, reg.email)
            },
            onDelete = { reg -> deleteRegistration(reg) },
        )
        binding.registrationsRecycler.layoutManager = LinearLayoutManager(this)
        binding.registrationsRecycler.adapter = registrationsAdapter

        binding.registerButton.setOnClickListener { registerForEvent() }
        binding.loginToRegisterButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.deleteEventButton.setOnClickListener { deleteEvent() }

        binding.registrationsSearchButton.setOnClickListener {
            registrationsPage = 0
            loadRegistrations()
        }
        binding.registrationsClearButton.setOnClickListener {
            binding.registrationsSearchInput.setText("")
            registrationsQuery = ""
            registrationsPage = 0
            loadRegistrations()
        }
        binding.registrationsSearchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                registrationsPage = 0
                loadRegistrations()
                true
            } else {
                false
            }
        }
        binding.registrationsPreviousButton.setOnClickListener {
            if (registrationsPage > 0) {
                registrationsPage--
                loadRegistrations()
            }
        }
        binding.registrationsNextButton.setOnClickListener {
            if (registrationsPage + 1 < registrationsTotalPages) {
                registrationsPage++
                loadRegistrations()
            }
        }

        AuthHeaderBinder.bind(this, binding.authHeaderInclude.root) {
            AuthHeaderBinder.bind(this, binding.authHeaderInclude.root)
            bindActions()
            loadRegistrations()
        }
        loadAll()
    }

    override fun onResume() {
        super.onResume()
        AuthHeaderBinder.bind(this, binding.authHeaderInclude.root) {
            AuthHeaderBinder.bind(this, binding.authHeaderInclude.root)
            bindActions()
        }
        bindActions()
        if (currentEvent != null) {
            loadRegistrations()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadAll() {
        setLoading(true)
        AppServices.api(this).eventApi.getEvent(eventId)
            .enqueue(object : Callback<EventResponse> {
                override fun onResponse(call: Call<EventResponse>, response: Response<EventResponse>) {
                    if (response.isSuccessful) {
                        val event = response.body()
                        if (event == null) {
                            setLoading(false)
                            showError("Empty response")
                            return
                        }
                        currentEvent = event
                        bindEvent(event)
                        bindActions()
                        loadRegistrations()
                    } else {
                        setLoading(false)
                        showError(response.errorMessage(AppServices.api(this@EventDetailActivity).gsonPublic))
                    }
                }

                override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                    setLoading(false)
                    showError(t.message ?: getString(R.string.error_generic))
                }
            })
    }

    private fun loadRegistrations() {
        registrationsQuery = binding.registrationsSearchInput.text?.toString()?.trim().orEmpty()
        binding.registrationsClearButton.visibility =
            if (registrationsQuery.isNotEmpty()) View.VISIBLE else View.GONE

        val q = registrationsQuery.ifBlank { null }
        AppServices.api(this).eventApi
            .searchRegistrations(eventId, page = registrationsPage, size = PAGE_SIZE, q = q)
            .enqueue(object : Callback<PageResponse<EventRegistrationResponse>> {
                override fun onResponse(
                    call: Call<PageResponse<EventRegistrationResponse>>,
                    response: Response<PageResponse<EventRegistrationResponse>>,
                ) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        bindRegistrationsPage(response.body() ?: PageResponse())
                        currentEvent?.let { updateSeatsLeft(it) }
                        bindActions()
                    } else {
                        showError(response.errorMessage(AppServices.api(this@EventDetailActivity).gsonPublic))
                    }
                }

                override fun onFailure(
                    call: Call<PageResponse<EventRegistrationResponse>>,
                    t: Throwable,
                ) {
                    setLoading(false)
                    showError(t.message ?: getString(R.string.error_generic))
                }
            })
    }

    private fun bindRegistrationsPage(page: PageResponse<EventRegistrationResponse>) {
        registrationsPage = page.number
        registrationsTotalPages = page.totalPages
        if (registrationsQuery.isEmpty()) {
            registrationsTotalCount = page.totalElements.toInt()
        }
        pageEmails = page.content.mapNotNull { it.email?.lowercase() }.toSet()
        registrationsAdapter.submit(page.content)
        binding.emptyRegistrations.visibility =
            if (page.content.isEmpty()) View.VISIBLE else View.GONE

        val showPager = page.totalPages > 1
        binding.registrationsPaginationBar.visibility =
            if (showPager) View.VISIBLE else View.GONE
        if (showPager) {
            binding.registrationsPageInfo.text = getString(
                R.string.page_info,
                page.number + 1,
                page.totalPages,
                page.totalElements.toInt(),
            )
            binding.registrationsPreviousButton.isEnabled = !page.first
            binding.registrationsNextButton.isEnabled = !page.last
        }
    }

    private fun bindEvent(event: EventResponse) {
        binding.toolbar.title = event.name
        binding.eventName.text = event.name
        binding.eventSeats.text = getString(R.string.max_seats, event.maxSeats)
        binding.eventCreatedBy.text =
            getString(R.string.created_by, event.createdByEmail ?: "—")
        updateSeatsLeft(event)
    }

    private fun updateSeatsLeft(event: EventResponse) {
        val left = (event.maxSeats - registrationsTotalCount).coerceAtLeast(0)
        binding.seatsLeft.text = getString(R.string.seats_left, left)
    }

    private fun bindActions() {
        val session = AppServices.session(this)
        session.hydrateFromToken()
        val event = currentEvent

        if (!session.isLoggedIn()) {
            binding.registerButton.visibility = View.GONE
            binding.loginToRegisterButton.visibility = View.VISIBLE
            binding.deleteEventButton.visibility = View.GONE
            binding.deleteEventButton.isEnabled = false
        } else {
            binding.loginToRegisterButton.visibility = View.GONE
            val me = session.effectiveEmail()?.lowercase()
            val alreadyRegistered = me != null && pageEmails.contains(me)
            // Keep enabled so a second tap can surface API 409 (duplicate) in UI / E2E.
            binding.registerButton.visibility = View.VISIBLE
            binding.registerButton.isEnabled = true
            if (alreadyRegistered) {
                binding.statusText.visibility = View.VISIBLE
                binding.statusText.text = getString(R.string.registration_success)
            } else if (binding.statusText.text != getString(R.string.registration_success)) {
                binding.statusText.visibility = View.GONE
            }

            val canDelete = event != null && session.canDeleteEvent(event.createdByEmail)
            binding.deleteEventButton.visibility = if (canDelete) View.VISIBLE else View.GONE
            binding.deleteEventButton.isEnabled = canDelete
        }
        registrationsAdapter.notifyDataSetChanged()
    }

    private fun registerForEvent() {
        if (!AppServices.session(this).isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }
        setLoading(true)
        binding.errorText.visibility = View.GONE
        AppServices.api(this).eventApi.registerForEvent(eventId)
            .enqueue(object : Callback<EventRegistrationResponse> {
                override fun onResponse(
                    call: Call<EventRegistrationResponse>,
                    response: Response<EventRegistrationResponse>,
                ) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        binding.statusText.visibility = View.VISIBLE
                        binding.statusText.text = getString(R.string.registration_success)
                        registrationsPage = 0
                        loadRegistrations()
                    } else {
                        showError(response.errorMessage(AppServices.api(this@EventDetailActivity).gsonPublic))
                    }
                }

                override fun onFailure(call: Call<EventRegistrationResponse>, t: Throwable) {
                    setLoading(false)
                    showError(t.message ?: getString(R.string.error_generic))
                }
            })
    }

    private fun deleteRegistration(reg: EventRegistrationResponse) {
        setLoading(true)
        AppServices.api(this).eventApi.deleteRegistration(eventId, reg.id)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    setLoading(false)
                    if (response.isSuccessful || response.code() == 204) {
                        loadRegistrations()
                    } else {
                        showError(response.errorMessage(AppServices.api(this@EventDetailActivity).gsonPublic))
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    setLoading(false)
                    showError(t.message ?: getString(R.string.error_generic))
                }
            })
    }

    private fun deleteEvent() {
        setLoading(true)
        AppServices.api(this).eventApi.deleteEvent(eventId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    setLoading(false)
                    if (response.isSuccessful || response.code() == 204) {
                        finish()
                    } else {
                        showError(response.errorMessage(AppServices.api(this@EventDetailActivity).gsonPublic))
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    setLoading(false)
                    showError(t.message ?: getString(R.string.error_generic))
                }
            })
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        if (loading) {
            binding.registerButton.isEnabled = false
            binding.deleteEventButton.isEnabled = false
        } else {
            bindActions()
        }
    }

    private fun showError(message: String) {
        binding.errorText.visibility = View.VISIBLE
        binding.errorText.text = message
    }

    companion object {
        const val EXTRA_EVENT_ID = "event_id"
        private const val PAGE_SIZE = 10
    }
}
