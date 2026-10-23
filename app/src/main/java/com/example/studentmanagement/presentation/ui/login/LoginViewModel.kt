package com.example.studentmanagement.presentation.ui.login

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.studentmanagement.data.remote.AuthApi
import com.example.studentmanagement.data.remote.TokenStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class LoginViewModel @Inject constructor(private val api: AuthApi, private val storage: TokenStorage) : ViewModel() {
    var username by mutableStateOf(""); private set
    var password by mutableStateOf(""); private set
    var errorMessage by mutableStateOf<String?>(null); private set
    var isLoading by mutableStateOf(false); private set
    fun onUsernameChange(value: String) { username = value }
    fun onPasswordChange(value: String) { password = value }
    fun login() {
        if (isLoading) return
        if (username.isBlank() || password.isBlank()) { errorMessage = "กรุณากรอก Username และ Password"; return }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = api.login(username.trim(), password)
                check(result.token_type.equals("bearer", true) && result.access_token.isNotBlank())
                password = ""
                storage.saveToken(result.access_token)
            } catch (e: CancellationException) { throw e }
            catch (e: HttpException) { errorMessage = if (e.code() == 401) "Username หรือ Password ไม่ถูกต้อง" else "เข้าสู่ระบบไม่สำเร็จ" }
            catch (e: Exception) { errorMessage = "เชื่อมต่อเซิร์ฟเวอร์ไม่ได้ กรุณาลองใหม่" }
            finally { isLoading = false }
        }
    }
}
