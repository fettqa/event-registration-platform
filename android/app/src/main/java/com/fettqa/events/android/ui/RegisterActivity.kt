package com.fettqa.events.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.fettqa.events.android.R
import com.fettqa.events.android.data.AppServices
import com.fettqa.events.android.data.errorMessage
import com.fettqa.events.android.databinding.ActivityRegisterBinding
import com.fettqa.events.android.model.AuthResponse
import com.fettqa.events.android.model.RegisterRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener { register() }
        binding.goLoginButton.setOnClickListener { finish() }
    }

    private fun register() {
        val fullName = binding.fullNameInput.text?.toString()?.trim().orEmpty()
        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()
        if (fullName.isBlank() || email.isBlank() || password.length < 6) {
            showError("Full name, email and password (min 6) are required")
            return
        }

        setLoading(true)
        AppServices.api(this).authApi.register(RegisterRequest(fullName, email, password))
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body == null) {
                            showError("Empty response")
                            return
                        }
                        AppServices.session(this@RegisterActivity).save(body)
                        startActivity(
                            Intent(this@RegisterActivity, EventListActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                        )
                        finish()
                    } else {
                        showError(response.errorMessage(AppServices.api(this@RegisterActivity).gsonPublic))
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    setLoading(false)
                    showError(t.message ?: getString(R.string.error_generic))
                }
            })
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.registerButton.isEnabled = !loading
    }

    private fun showError(message: String) {
        binding.errorText.visibility = View.VISIBLE
        binding.errorText.text = message
    }
}
