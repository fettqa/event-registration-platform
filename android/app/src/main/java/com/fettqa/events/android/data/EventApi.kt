package com.fettqa.events.android.data

import com.fettqa.events.android.model.CreateEventRequest
import com.fettqa.events.android.model.EventRegistrationResponse
import com.fettqa.events.android.model.EventResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface EventApi {
    @GET("api/events")
    fun listEvents(): Call<List<EventResponse>>

    @GET("api/events/{id}")
    fun getEvent(@Path("id") id: Long): Call<EventResponse>

    @POST("api/events")
    fun createEvent(@Body body: CreateEventRequest): Call<EventResponse>

    @DELETE("api/events/{id}")
    fun deleteEvent(@Path("id") id: Long): Call<Void>

    @GET("api/events/{id}/registrations")
    fun listRegistrations(@Path("id") id: Long): Call<List<EventRegistrationResponse>>

    @POST("api/events/{id}/registrations")
    fun registerForEvent(@Path("id") id: Long): Call<EventRegistrationResponse>

    @DELETE("api/events/{eventId}/registrations/{registrationId}")
    fun deleteRegistration(
        @Path("eventId") eventId: Long,
        @Path("registrationId") registrationId: Long,
    ): Call<Void>
}
