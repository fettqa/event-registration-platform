package com.fettqa.events.android.data

import com.fettqa.events.android.model.AuthResponse

interface TokenStore {
    var accessToken: String?
    fun clear()
}

/** JWT + profile fields (like web localStorage erp.auth). */
interface SessionStore : TokenStore {
    var fullName: String?
    var email: String?
    var role: String?

    fun save(auth: AuthResponse) {
        accessToken = auth.accessToken
        fullName = auth.fullName
        email = auth.email ?: JwtPayload.email(auth.accessToken)
        role = auth.role ?: JwtPayload.role(auth.accessToken)
    }

    /** Fill missing email/role from JWT (e.g. after app upgrade). */
    fun hydrateFromToken() {
        val token = accessToken ?: return
        if (email.isNullOrBlank()) {
            email = JwtPayload.email(token)
        }
        if (role.isNullOrBlank()) {
            role = JwtPayload.role(token)
        }
    }

    fun isLoggedIn(): Boolean = !accessToken.isNullOrBlank()

    fun effectiveRole(): String? {
        hydrateFromToken()
        return role?.uppercase()
    }

    fun effectiveEmail(): String? {
        hydrateFromToken()
        return email
    }

    fun canCreateEvent(): Boolean {
        val r = effectiveRole()
        return r == "ADMIN" || r == "SUPER_USER"
    }

    fun isAdmin(): Boolean = effectiveRole() == "ADMIN"

    fun canDeleteEvent(createdByEmail: String?): Boolean {
        return when (effectiveRole()) {
            "ADMIN" -> true
            "SUPER_USER" -> {
                val me = effectiveEmail()
                !me.isNullOrBlank() && me.equals(createdByEmail, ignoreCase = true)
            }
            else -> false
        }
    }

    fun canDeleteRegistration(createdByEmail: String?, registrationEmail: String?): Boolean {
        return when (effectiveRole()) {
            "ADMIN" -> true
            "SUPER_USER" -> {
                val me = effectiveEmail()
                !me.isNullOrBlank() && me.equals(createdByEmail, ignoreCase = true)
            }
            else -> {
                val me = effectiveEmail()
                !me.isNullOrBlank() && me.equals(registrationEmail, ignoreCase = true)
            }
        }
    }
}

class InMemoryTokenStore(
    initial: String? = null,
) : SessionStore {
    override var accessToken: String? = initial
    override var fullName: String? = null
    override var email: String? = null
    override var role: String? = null

    override fun clear() {
        accessToken = null
        fullName = null
        email = null
        role = null
    }
}

class PrefsTokenStore(
    context: android.content.Context,
) : SessionStore {
    private val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)

    override var accessToken: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_TOKEN, value).apply()
        }

    override var fullName: String?
        get() = prefs.getString(KEY_FULL_NAME, null)
        set(value) {
            prefs.edit().putString(KEY_FULL_NAME, value).apply()
        }

    override var email: String?
        get() = prefs.getString(KEY_EMAIL, null)
        set(value) {
            prefs.edit().putString(KEY_EMAIL, value).apply()
        }

    override var role: String?
        get() = prefs.getString(KEY_ROLE, null)
        set(value) {
            prefs.edit().putString(KEY_ROLE, value).apply()
        }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "erp_auth"
        private const val KEY_TOKEN = "access_token"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
    }
}
