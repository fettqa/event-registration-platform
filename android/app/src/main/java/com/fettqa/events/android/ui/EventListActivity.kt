package com.fettqa.events.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fettqa.events.android.R
import com.fettqa.events.android.data.AppServices
import com.fettqa.events.android.data.errorMessage
import com.fettqa.events.android.databinding.ActivityEventListBinding
import com.fettqa.events.android.model.EventResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventListBinding
    private lateinit var adapter: EventAdapter
    private var allEvents: List<EventResponse> = emptyList()
    private var activeQuery: String = ""

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

        binding.eventsSearchButton.setOnClickListener { applyFilter() }
        binding.eventsClearButton.setOnClickListener {
            binding.eventsSearchInput.setText("")
            activeQuery = ""
            applyFilter()
        }
        binding.eventsSearchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                applyFilter()
                true
            } else {
                false
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
        setLoading(true)
        binding.errorText.visibility = View.GONE
        AppServices.api(this).eventApi.listEvents()
            .enqueue(object : Callback<List<EventResponse>> {
                override fun onResponse(
                    call: Call<List<EventResponse>>,
                    response: Response<List<EventResponse>>,
                ) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        allEvents = response.body().orEmpty()
                        applyFilter()
                    } else {
                        showError(response.errorMessage(AppServices.api(this@EventListActivity).gsonPublic))
                    }
                }

                override fun onFailure(call: Call<List<EventResponse>>, t: Throwable) {
                    setLoading(false)
                    showError(t.message ?: getString(R.string.error_generic))
                }
            })
    }

    private fun applyFilter() {
        activeQuery = binding.eventsSearchInput.text?.toString()?.trim().orEmpty()
        binding.eventsClearButton.visibility =
            if (activeQuery.isNotEmpty()) View.VISIBLE else View.GONE

        val filtered = if (activeQuery.isEmpty()) {
            allEvents
        } else {
            allEvents.filter { it.name.contains(activeQuery, ignoreCase = true) }
        }
        adapter.submit(filtered)
        binding.emptyText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        binding.errorText.visibility = View.VISIBLE
        binding.errorText.text = message
    }
}
