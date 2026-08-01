package com.fettqa.events.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.fettqa.events.android.R
import com.fettqa.events.android.data.AppServices
import com.fettqa.events.android.data.errorMessage
import com.fettqa.events.android.databinding.ActivityLoginBinding
import com.fettqa.events.android.model.AuthResponse
import com.fettqa.events.android.model.LoginRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (AppServices.session(this).isLoggedIn()) {
            openEvents()
            return
        }

        binding.loginButton.setOnClickListener { login() }
        binding.goRegisterButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()
        if (email.isBlank() || password.isBlank()) {
            showError("Email and password are required")
            return
        }

        setLoading(true)
        AppServices.api(this).authApi.login(LoginRequest(email, password))
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body == null) {
                            showError("Empty response")
                            return
                        }
                        AppServices.session(this@LoginActivity).save(body)
                        openEvents()
                    } else {
                        showError(response.errorMessage(AppServices.api(this@LoginActivity).gsonPublic))
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    setLoading(false)
                    showError(t.message ?: getString(R.string.error_generic))
                }
            })
    }

    private fun openEvents() {
        startActivity(
            Intent(this, EventListActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        )
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !loading
    }

    private fun showError(message: String) {
        binding.errorText.visibility = View.VISIBLE
        binding.errorText.text = message
    }
}
