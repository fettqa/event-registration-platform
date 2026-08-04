package com.fettqa.events.android.data

import android.content.Context
import com.fettqa.events.android.BuildConfig
import com.fettqa.events.android.model.ApiError
import com.google.gson.Gson
import retrofit2.Response

object AppServices {
    @Volatile
    private var sessionStore: SessionStore? = null

    @Volatile
    private var apiClient: ApiClient? = null

    fun session(context: Context): SessionStore {
        val store = sessionStore ?: PrefsTokenStore(context.applicationContext).also { sessionStore = it }
        store.hydrateFromToken()
        return store
    }

    /** @deprecated use [session] */
    fun tokenStore(context: Context): SessionStore = session(context)

    fun api(context: Context): ApiClient {
        return apiClient ?: ApiClient(
            baseUrl = BuildConfig.BASE_URL,
            tokenStore = session(context),
        ).also { apiClient = it }
    }

    fun resetForTests() {
        sessionStore = null
        apiClient = null
    }
}

fun <T> Response<T>.errorMessage(gson: Gson = Gson()): String {
    val raw = errorBody()?.string().orEmpty()
    if (raw.isBlank()) {
        return "HTTP ${code()}"
    }
    return try {
        gson.fromJson(raw, ApiError::class.java)?.error?.takeIf { it.isNotBlank() } ?: raw
    } catch (_: Exception) {
        raw
    }
}
