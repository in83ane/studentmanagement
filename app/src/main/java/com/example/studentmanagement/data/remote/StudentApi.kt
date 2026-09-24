package com.example.studentmanagement.data.remote

import retrofit2.http.GET

// Data Layer: Retrofit API Interface to fetch remote data
interface StudentApi {
    @GET("users")
    suspend fun getStudents(): List<StudentDto>
}
