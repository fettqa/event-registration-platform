package com.fettqa.events.android.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(
    baseUrl: String,
    private val tokenStore: TokenStore,
    enableLogging: Boolean = true,
) {
    private val gson: Gson = GsonBuilder().create()

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = tokenStore.accessToken
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        chain.proceed(request)
    }

    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .apply {
            if (enableLogging) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    },
                )
            }
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(normalizeBaseUrl(baseUrl))
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val eventApi: EventApi = retrofit.create(EventApi::class.java)
    val adminApi: AdminApi = retrofit.create(AdminApi::class.java)

    val gsonPublic: Gson get() = gson

    companion object {
        fun normalizeBaseUrl(url: String): String =
            if (url.endsWith("/")) url else "$url/"
    }
}
