package com.fettqa.events.android.data

import com.fettqa.events.android.model.AuthResponse
import com.fettqa.events.android.model.LoginRequest
import com.fettqa.events.android.model.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    fun login(@Body body: LoginRequest): Call<AuthResponse>

    @POST("api/auth/register")
    fun register(@Body body: RegisterRequest): Call<AuthResponse>

    @GET("actuator/health")
    fun health(): Call<Map<String, Any>>
}
