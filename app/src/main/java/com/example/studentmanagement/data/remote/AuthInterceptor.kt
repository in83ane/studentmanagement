package com.example.studentmanagement.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val storage: TokenStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = storage.getToken()
        val protected = original.url.encodedPath != "/token"
        val request = if (protected && !token.isNullOrBlank()) {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        } else original
        val response = chain.proceed(request)
        if (protected && response.code == 401 && storage.getToken() == token) storage.clearToken()
        return response
    }
}
