package com.fettqa.events.android.data

import com.fettqa.events.android.model.AuthResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenStoreTest {
    @Test
    fun session_save_and_clear() {
        val store = InMemoryTokenStore()
        assertFalse(store.isLoggedIn())

        store.save(
            AuthResponse("jwt", "Bearer", "Ada", "ada@example.com", "USER"),
        )
        assertTrue(store.isLoggedIn())
        assertEquals("jwt", store.accessToken)
        assertEquals("Ada", store.fullName)
        assertEquals("ada@example.com", store.email)
        assertEquals("USER", store.role)

        store.clear()
        assertNull(store.accessToken)
        assertNull(store.fullName)
    }

    @Test
    fun role_permissions() {
        val store = InMemoryTokenStore()
        store.save(AuthResponse("t", "Bearer", "Admin", "admin@example.com", "ADMIN"))
        assertTrue(store.canCreateEvent())
        assertTrue(store.isAdmin())
        assertTrue(store.canDeleteEvent("anyone@example.com"))
        assertTrue(store.canDeleteRegistration("x", "y"))

        store.save(AuthResponse("t", "Bearer", "Su", "su@example.com", "SUPER_USER"))
        assertTrue(store.canCreateEvent())
        assertFalse(store.isAdmin())
        assertTrue(store.canDeleteEvent("su@example.com"))
        assertFalse(store.canDeleteEvent("other@example.com"))
        assertTrue(store.canDeleteRegistration("su@example.com", "guest@example.com"))

        store.save(AuthResponse("t", "Bearer", "User", "user@example.com", "USER"))
        assertFalse(store.canCreateEvent())
        assertTrue(store.canDeleteRegistration("owner@example.com", "user@example.com"))
        assertFalse(store.canDeleteRegistration("owner@example.com", "other@example.com"))
        assertFalse(store.canDeleteEvent("user@example.com"))
    }
}
