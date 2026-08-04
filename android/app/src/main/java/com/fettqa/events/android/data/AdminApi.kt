package com.fettqa.events.android.data

import com.fettqa.events.android.model.UpdateUserRoleRequest
import com.fettqa.events.android.model.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface AdminApi {
    @GET("api/admin/users")
    fun listUsers(): Call<List<UserResponse>>

    @PUT("api/admin/users/{id}/role")
    fun updateRole(
        @Path("id") id: Long,
        @Body body: UpdateUserRoleRequest,
    ): Call<UserResponse>
}
