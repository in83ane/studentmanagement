package com.example.studentmanagement.di

import android.content.Context
import com.example.studentmanagement.data.remote.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun storage(@ApplicationContext context: Context) = TokenStorage(context)
    @Provides @Singleton
    fun retrofit(storage: TokenStorage): Retrofit = RetrofitClient.create(storage)
    @Provides @Singleton fun students(retrofit: Retrofit): StudentApi = retrofit.create(StudentApi::class.java)
    @Provides @Singleton fun auth(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
}
