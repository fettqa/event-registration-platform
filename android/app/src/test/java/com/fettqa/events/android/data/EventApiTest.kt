package com.fettqa.events.android.data

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EventApiTest {
    private lateinit var server: MockWebServer
    private lateinit var api: ApiClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = ApiClient(
            server.url("/").toString(),
            InMemoryTokenStore("token"),
            enableLogging = false,
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun list_events_parses_json() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    [
                      {
                        "id":1,
                        "name":"QA Meetup",
                        "maxSeats":50,
                        "createdById":1,
                        "createdByEmail":"admin@example.com",
                        "createdAt":"2026-01-01T10:00:00Z"
                      }
                    ]
                    """.trimIndent(),
                ),
        )

        val response = api.eventApi.listEvents().execute()
        assertTrue(response.isSuccessful)
        assertEquals(1, response.body()!!.size)
        assertEquals("QA Meetup", response.body()!![0].name)
        assertEquals(50, response.body()!![0].maxSeats)
        assertEquals("/api/events", server.takeRequest().path)
    }

    @Test
    fun search_events_parses_page_json() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "content":[
                        {
                          "id":1,
                          "name":"QA Meetup",
                          "maxSeats":50,
                          "createdById":1,
                          "createdByEmail":"admin@example.com",
                          "createdAt":"2026-01-01T10:00:00Z"
                        }
                      ],
                      "totalElements":3,
                      "totalPages":3,
                      "number":0,
                      "first":true,
                      "last":false
                    }
                    """.trimIndent(),
                ),
        )

        val response = api.eventApi.searchEvents(page = 0, size = 1, q = "QA").execute()
        assertTrue(response.isSuccessful)
        val page = response.body()!!
        assertEquals(1, page.content.size)
        assertEquals("QA Meetup", page.content[0].name)
        assertEquals(3L, page.totalElements)
        assertEquals(3, page.totalPages)
        assertEquals("/api/events?page=0&size=1&q=QA", server.takeRequest().path)
    }

    @Test
    fun get_event_by_id() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "id":7,
                      "name":"Workshop",
                      "maxSeats":10,
                      "createdById":1,
                      "createdByEmail":"admin@example.com",
                      "createdAt":"2026-01-01T10:00:00Z"
                    }
                    """.trimIndent(),
                ),
        )

        val response = api.eventApi.getEvent(7).execute()
        assertTrue(response.isSuccessful)
        assertEquals(7L, response.body()!!.id)
        assertEquals("/api/events/7", server.takeRequest().path)
    }

    @Test
    fun register_for_event_created() {
        server.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody(
                    """
                    {
                      "id":99,
                      "eventId":7,
                      "email":"user@example.com",
                      "fullName":"User",
                      "createdAt":"2026-01-02T10:00:00Z"
                    }
                    """.trimIndent(),
                ),
        )

        val response = api.eventApi.registerForEvent(7).execute()
        assertTrue(response.isSuccessful)
        assertEquals(99L, response.body()!!.id)
        assertEquals(7L, response.body()!!.eventId)

        val recorded = server.takeRequest()
        assertEquals("/api/events/7/registrations", recorded.path)
        assertEquals("Bearer token", recorded.getHeader("Authorization"))
        assertEquals("POST", recorded.method)
    }

    @Test
    fun register_for_event_conflict_409() {
        server.enqueue(
            MockResponse()
                .setResponseCode(409)
                .setBody("""{"error":"Already registered for this event"}"""),
        )

        val response = api.eventApi.registerForEvent(7).execute()
        assertEquals(409, response.code())
        assertEquals(
            "Already registered for this event",
            response.errorMessage(api.gsonPublic),
        )
    }

    @Test
    fun list_registrations() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    [{"id":1,"eventId":7,"email":"a@b.com","fullName":"A","createdAt":"2026-01-01T00:00:00Z"}]
                    """.trimIndent(),
                ),
        )
        val response = api.eventApi.listRegistrations(7).execute()
        assertTrue(response.isSuccessful)
        assertEquals("a@b.com", response.body()!![0].email)
        assertEquals("/api/events/7/registrations", server.takeRequest().path)
    }

    @Test
    fun create_event() {
        server.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody(
                    """
                    {"id":3,"name":"New","maxSeats":20,"createdById":1,"createdByEmail":"admin@example.com","createdAt":"2026-01-01T00:00:00Z"}
                    """.trimIndent(),
                ),
        )
        val response = api.eventApi
            .createEvent(com.fettqa.events.android.model.CreateEventRequest("New", 20))
            .execute()
        assertTrue(response.isSuccessful)
        assertEquals(3L, response.body()!!.id)
        assertEquals("POST", server.takeRequest().method)
    }
}
