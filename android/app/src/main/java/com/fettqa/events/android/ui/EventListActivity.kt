package com.fettqa.events.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import com.fettqa.events.android.R
import com.fettqa.events.android.data.AppServices
import com.fettqa.events.android.data.errorMessage
import com.fettqa.events.android.databinding.ActivityEventListBinding
import com.fettqa.events.android.model.EventResponse
import com.fettqa.events.android.model.PageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventListActivity : BaseActivity() {
    private lateinit var binding: ActivityEventListBinding
    private lateinit var adapter: EventAdapter
    private var activeQuery: String = ""
    private var currentPage: Int = 0
    private var totalPages: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        adapter = EventAdapter { event ->
            startActivity(
                Intent(this, EventDetailActivity::class.java)
                    .putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.id),
            )
        }
        binding.eventsRecycler.layoutManager = LinearLayoutManager(this)
        binding.eventsRecycler.adapter = adapter

        binding.createEventFab.setOnClickListener {
            startActivity(Intent(this, CreateEventActivity::class.java))
        }

        binding.eventsSearchButton.setOnClickListener {
            currentPage = 0
            loadEvents()
        }
        binding.eventsClearButton.setOnClickListener {
            binding.eventsSearchInput.setText("")
            activeQuery = ""
            currentPage = 0
            loadEvents()
        }
        binding.eventsSearchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentPage = 0
                loadEvents()
                true
            } else {
                false
            }
        }
        binding.eventsPreviousButton.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                loadEvents()
            }
        }
        binding.eventsNextButton.setOnClickListener {
            if (currentPage + 1 < totalPages) {
                currentPage++
                loadEvents()
            }
        }

        refreshHeader()
        loadEvents()
    }

    override fun onResume() {
        super.onResume()
        refreshHeader()
        if (::adapter.isInitialized) {
            loadEvents()
        }
    }

    private fun refreshHeader() {
        AuthHeaderBinder.bind(this, binding.authHeaderInclude.root) {
            refreshHeader()
            loadEvents()
        }
        binding.createEventFab.visibility =
            if (AppServices.session(this).canCreateEvent()) View.VISIBLE else View.GONE
    }

    private fun loadEvents() {
        activeQuery = binding.eventsSearchInput.text?.toString()?.trim().orEmpty()
        binding.eventsClearButton.visibility =
            if (activeQuery.isNotEmpty()) View.VISIBLE else View.GONE

        setLoading(true)
        binding.errorText.visibility = View.GONE
        val q = activeQuery.ifBlank { null }
        AppServices.api(this).eventApi.searchEvents(page = currentPage, size = PAGE_SIZE, q = q)
            .enqueue(object : Callback<PageResponse<EventResponse>> {
                override fun onResponse(
                    call: Call<PageResponse<EventResponse>>,
                    response: Response<PageResponse<EventResponse>>,
                ) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        bindPage(response.body() ?: PageResponse())
                    } else {
                        showError(response.errorMessage(AppServices.api(this@EventListActivity).gsonPublic))
                    }
                }

                override fun onFailure(call: Call<PageResponse<EventResponse>>, t: Throwable) {
                    setLoading(false)
                    showError(t.message ?: getString(R.string.error_generic))
                }
            })
    }

    private fun bindPage(page: PageResponse<EventResponse>) {
        currentPage = page.number
        totalPages = page.totalPages
        adapter.submit(page.content)
        binding.emptyText.visibility = if (page.content.isEmpty()) View.VISIBLE else View.GONE

        val showPager = page.totalPages > 1
        binding.eventsPaginationBar.visibility = if (showPager) View.VISIBLE else View.GONE
        if (showPager) {
            binding.eventsPageInfo.text = getString(
                R.string.page_info,
                page.number + 1,
                page.totalPages,
                page.totalElements.toInt(),
            )
            binding.eventsPreviousButton.isEnabled = !page.first
            binding.eventsNextButton.isEnabled = !page.last
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        binding.errorText.visibility = View.VISIBLE
        binding.errorText.text = message
    }

    companion object {
        private const val PAGE_SIZE = 10
    }
}
