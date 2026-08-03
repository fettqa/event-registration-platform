package com.fettqa.events.android.ui

import android.os.Bundle
import android.view.View
import com.fettqa.events.android.R
import com.fettqa.events.android.data.AppServices
import com.fettqa.events.android.data.errorMessage
import com.fettqa.events.android.databinding.ActivityCreateEventBinding
import com.fettqa.events.android.model.CreateEventRequest
import com.fettqa.events.android.model.EventResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateEventActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateEventBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        if (!AppServices.session(this).canCreateEvent()) {
            finish()
            return
        }

        binding.submitButton.setOnClickListener { create() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun create() {
        val name = binding.nameInput.text?.toString()?.trim().orEmpty()
        val seats = binding.seatsInput.text?.toString()?.toIntOrNull()
        if (name.isBlank() || seats == null || seats < 1) {
            showError("Name and max seats (>= 1) are required")
            return
        }
        setLoading(true)
        AppServices.api(this).eventApi.createEvent(CreateEventRequest(name, seats))
            .enqueue(object : Callback<EventResponse> {
                override fun onResponse(call: Call<EventResponse>, response: Response<EventResponse>) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        val id = response.body()?.id
                        if (id != null) {
                            startActivity(
                                android.content.Intent(this@CreateEventActivity, EventDetailActivity::class.java)
                                    .putExtra(EventDetailActivity.EXTRA_EVENT_ID, id),
                            )
                        }
                        finish()
                    } else {
                        showError(response.errorMessage(AppServices.api(this@CreateEventActivity).gsonPublic))
                    }
                }

                override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                    setLoading(false)
                    showError(t.message ?: getString(R.string.error_generic))
                }
            })
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.submitButton.isEnabled = !loading
    }

    private fun showError(message: String) {
        binding.errorText.visibility = View.VISIBLE
        binding.errorText.text = message
    }
}
