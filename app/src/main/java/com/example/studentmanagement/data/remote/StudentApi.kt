package com.example.studentmanagement.data.remote

import retrofit2.http.*

interface StudentApi {
    @GET("students") suspend fun getStudents(): List<StudentDto>
    @GET("students/{id}") suspend fun getStudent(@Path("id") id: Int): StudentDto
    @POST("students") suspend fun createStudent(@Body student: StudentCreateRequest): StudentDto
    @PUT("students/{id}") suspend fun updateStudent(@Path("id") id: Int, @Body student: StudentCreateRequest): StudentDto
    @DELETE("students/{id}") suspend fun deleteStudent(@Path("id") id: Int)
}
