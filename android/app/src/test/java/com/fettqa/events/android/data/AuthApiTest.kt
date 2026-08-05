package com.fettqa.events.android.data

import com.fettqa.events.android.model.LoginRequest
import com.fettqa.events.android.model.RegisterRequest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthApiTest {
    private lateinit var server: MockWebServer
    private lateinit var tokenStore: InMemoryTokenStore
    private lateinit var api: ApiClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        tokenStore = InMemoryTokenStore()
        api = ApiClient(server.url("/").toString(), tokenStore, enableLogging = false)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun login_success_stores_token_fields() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "accessToken":"jwt-123",
                      "tokenType":"Bearer",
                      "fullName":"Admin",
                      "email":"admin@example.com",
                      "role":"ADMIN"
                    }
                    """.trimIndent(),
                ),
        )

        val response = api.authApi.login(LoginRequest("admin@example.com", "admin123")).execute()
        assertTrue(response.isSuccessful)
        assertEquals("jwt-123", response.body()!!.accessToken)
        assertEquals("ADMIN", response.body()!!.role)

        val recorded = server.takeRequest()
        assertEquals("/api/auth/login", recorded.path)
        assertTrue(recorded.body.readUtf8().contains("admin@example.com"))
    }

    @Test
    fun login_bad_credentials_returns_401() {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"error":"bad credentials"}"""),
        )

        val response = api.authApi.login(LoginRequest("a@b.com", "wrong")).execute()
        assertEquals(401, response.code())
        assertEquals("bad credentials", response.errorMessage(api.gsonPublic))
    }

    @Test
    fun register_success() {
        server.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody(
                    """
                    {
                      "accessToken":"jwt-new",
                      "tokenType":"Bearer",
                      "fullName":"New User",
                      "email":"new@example.com",
                      "role":"USER"
                    }
                    """.trimIndent(),
                ),
        )

        val response = api.authApi
            .register(RegisterRequest("New User", "new@example.com", "secret1"))
            .execute()
        assertTrue(response.isSuccessful)
        assertEquals("jwt-new", response.body()!!.accessToken)
        assertEquals("/api/auth/register", server.takeRequest().path)
    }

    @Test
    fun bearer_interceptor_adds_authorization_header() {
        tokenStore.accessToken = "abc"
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[]"),
        )

        api.eventApi.listEvents().execute()
        val recorded = server.takeRequest()
        assertEquals("Bearer abc", recorded.getHeader("Authorization"))
    }

    @Test
    fun bearer_interceptor_skips_header_when_no_token() {
        assertNull(tokenStore.accessToken)
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
        api.eventApi.listEvents().execute()
        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun health_ok() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"status":"UP"}"""))
        val response = api.authApi.health().execute()
        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals("UP", response.body()!!["status"])
    }
}
