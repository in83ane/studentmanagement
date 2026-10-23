package com.example.studentmanagement.data.remote

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Lab storage in app-private preferences; production should protect tokens with Keystore.
class TokenStorage(context: Context) {
    private val preferences = context.getSharedPreferences("auth_preferences", Context.MODE_PRIVATE)
    private val tokenState = MutableStateFlow(preferences.getString("access_token", null))
    val token = tokenState.asStateFlow()
    fun getToken(): String? = tokenState.value
    fun saveToken(value: String) {
        preferences.edit().putString("access_token", value).apply()
        tokenState.value = value
    }
    fun clearToken() {
        preferences.edit().remove("access_token").apply()
        tokenState.value = null
    }
}
