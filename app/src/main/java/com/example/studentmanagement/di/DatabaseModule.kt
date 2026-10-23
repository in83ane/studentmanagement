package com.example.studentmanagement.di

import android.content.Context
import com.example.studentmanagement.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun database(@ApplicationContext context: Context): AppDatabase = AppDatabase.getDatabase(context)
    @Provides fun dao(database: AppDatabase): StudentDao = database.studentDao()
}
