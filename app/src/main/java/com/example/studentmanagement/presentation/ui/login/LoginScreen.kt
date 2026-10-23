package com.example.studentmanagement.presentation.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center) {
        Text("Student App Login", style = MaterialTheme.typography.headlineMedium)
        Text("LAB 3 · Authentication + JWT", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(viewModel.username, viewModel::onUsernameChange, label = { Text("Username") },
            singleLine = true, enabled = !viewModel.isLoading, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(viewModel.password, viewModel::onPasswordChange, label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true, enabled = !viewModel.isLoading, modifier = Modifier.fillMaxWidth())
        viewModel.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(20.dp))
        Button(onClick = viewModel::login, enabled = !viewModel.isLoading, modifier = Modifier.fillMaxWidth()) {
            Text(if (viewModel.isLoading) "กำลังเข้าสู่ระบบ..." else "Login")
        }
    }
}
