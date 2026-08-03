package com.fettqa.events.android.data

import okio.ByteString.Companion.decodeBase64

/** Read email (sub) and role from JWT payload without verifying signature (UI only). */
object JwtPayload {
    fun email(token: String?): String? = claim(token, "sub")

    fun role(token: String?): String? = claim(token, "role")

    private fun claim(token: String?, name: String): String? {
        if (token.isNullOrBlank()) return null
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            var payload = parts[1].replace('-', '+').replace('_', '/')
            val pad = (4 - payload.length % 4) % 4
            if (pad > 0) payload += "=".repeat(pad)
            val bytes = payload.decodeBase64()?.toByteArray() ?: return null
            val json = String(bytes, Charsets.UTF_8)
            val match = Regex(""""$name"\s*:\s*"([^"]+)"""").find(json)
            match?.groupValues?.get(1)?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}
