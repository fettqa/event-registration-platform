package com.fettqa.events.android.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.fettqa.events.android.R
import com.fettqa.events.android.data.AppServices
import com.fettqa.events.android.data.errorMessage
import com.fettqa.events.android.databinding.ActivityAdminPanelBinding
import com.fettqa.events.android.model.UpdateUserRoleRequest
import com.fettqa.events.android.model.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminPanelActivity : BaseActivity() {
    private lateinit var binding: ActivityAdminPanelBinding
    private lateinit var adapter: AdminUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        if (!AppServices.session(this).isAdmin()) {
            finish()
            return
        }

        adapter = AdminUserAdapter { user, role -> updateRole(user, role) }
        binding.usersRecycler.layoutManager = LinearLayoutManager(this)
        binding.usersRecycler.adapter = adapter
        loadUsers()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadUsers() {
        setLoading(true)
        AppServices.api(this).adminApi.listUsers()
            .enqueue(object : Callback<List<UserResponse>> {
                override fun onResponse(
                    call: Call<List<UserResponse>>,
                    response: Response<List<UserResponse>>,
                ) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        adapter.submit(response.body().orEmpty())
                    } else {
                        showError(response.errorMessage(AppServices.api(this@AdminPanelActivity).gsonPublic))
                    }
                }

                override fun onFailure(call: Call<List<UserResponse>>, t: Throwable) {
                    setLoading(false)
                    showError(t.message ?: getString(R.string.error_generic))
                }
            })
    }

    private fun updateRole(user: UserResponse, role: String) {
        setLoading(true)
        binding.statusText.visibility = View.GONE
        AppServices.api(this).adminApi.updateRole(user.id, UpdateUserRoleRequest(role))
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        binding.statusText.visibility = View.VISIBLE
                        binding.statusText.text = getString(R.string.role_updated)
                        loadUsers()
                    } else {
                        showError(response.errorMessage(AppServices.api(this@AdminPanelActivity).gsonPublic))
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    setLoading(false)
                    showError(t.message ?: getString(R.string.error_generic))
                }
            })
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        binding.errorText.visibility = View.VISIBLE
        binding.errorText.text = message
    }
}
