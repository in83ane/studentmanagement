package com.example.studentmanagement.data.remote

import android.util.Log
import com.example.studentmanagement.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"
    fun create(storage: TokenStorage): Retrofit {
        val logging = HttpLoggingInterceptor { message -> Log.d("Lab3Http", message) }.apply {
            // Never log request/response bodies: login bodies contain credentials and JWTs.
            redactHeader("Authorization")
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(storage))
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder().baseUrl(BASE_URL).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }
}
