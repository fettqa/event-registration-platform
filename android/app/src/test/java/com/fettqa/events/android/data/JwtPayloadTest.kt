package com.fettqa.events.android.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class JwtPayloadTest {
    @Test
    fun parses_sub_and_role() {
        val json = """{"sub":"admin@example.com","role":"ADMIN"}"""
        val payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(json.toByteArray(Charsets.UTF_8))
        val token = "hdr.$payload.sig"

        assertEquals("admin@example.com", JwtPayload.email(token))
        assertEquals("ADMIN", JwtPayload.role(token))
    }

    @Test
    fun hydrate_makes_admin_can_delete() {
        val json = """{"sub":"admin@example.com","role":"ADMIN"}"""
        val payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(json.toByteArray(Charsets.UTF_8))
        val store = InMemoryTokenStore("hdr.$payload.sig")
        assertNull(store.role)
        store.hydrateFromToken()
        assertEquals("ADMIN", store.role)
        assertEquals("admin@example.com", store.email)
        assertTrue(store.isAdmin())
        assertTrue(store.canDeleteEvent("anyone@example.com"))
    }
}
