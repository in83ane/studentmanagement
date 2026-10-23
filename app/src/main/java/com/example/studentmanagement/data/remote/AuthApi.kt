package com.example.studentmanagement.data.remote

import retrofit2.http.*

interface AuthApi {
    @FormUrlEncoded
    @POST("token")
    suspend fun login(@Field("username") username: String, @Field("password") password: String): TokenResponse
}
