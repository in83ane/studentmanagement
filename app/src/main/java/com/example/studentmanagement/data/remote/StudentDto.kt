package com.example.studentmanagement.data.remote

// Data Layer: Data Transfer Object (DTO) for parsing Retrofit JSON responses
data class StudentDto(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val company: CompanyDto?
)

data class CompanyDto(
    val name: String,
    val catchPhrase: String,
    val bs: String
)
