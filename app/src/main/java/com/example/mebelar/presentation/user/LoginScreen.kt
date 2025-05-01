package com.example.mebelar.presentation.user

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mebelar.ui.theme.ErrorScreen
import com.example.mebelar.ui.theme.LoadingScreen
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLogin: () -> Unit,
    initialLogin: String = "",
    showSuccessMessage: Boolean = false,
    snackbarHostState: SnackbarHostState
) {
    val login = remember { mutableStateOf(initialLogin) }
    val password = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }
    val isLoginError = remember { mutableStateOf(false) }
    val isPasswordError = remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val loginState by viewModel.loginState.collectAsState()

    // Показываем Snackbar при успешной регистрации
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Успешно зарегистрированы")
            }
        }
    }

    // Регулярное выражение для валидации email
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (loginState) {
            LoginState.Loading -> {
                LoadingScreen()
            }
            is LoginState.Idle -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Авторизация",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage.value,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = login.value,
                        onValueChange = {
                            if (it.length <= 50) {
                                login.value = it
                                isLoginError.value = false
                            }
                        },
                        label = { Text("Логин (email)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isLoginError.value) 2.dp else 0.dp,
                                color = if (isLoginError.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                            ),
                        isError = isLoginError.value,
                        singleLine = true,
                        trailingIcon = {
                            if (isLoginError.value) {
                                IconButton(onClick = { login.value = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Очистить логин"
                                    )
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = password.value,
                        onValueChange = {
                            if (it.length <= 50) {
                                password.value = it
                                isPasswordError.value = false
                            }
                        },
                        label = { Text("Пароль") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isPasswordError.value) 2.dp else 0.dp,
                                color = if (isPasswordError.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                            ),
                        isError = isPasswordError.value,
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Row {
                                if (isPasswordError.value) {
                                    IconButton(onClick = { password.value = "" }) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = "Очистить пароль"
                                        )
                                    }
                                }
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (showPassword) "Скрыть пароль" else "Показать пароль"
                                    )
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isLoginError.value = false
                            isPasswordError.value = false

                            when {
                                login.value.isBlank() || password.value.isBlank() -> {
                                    errorMessage.value = "Пожалуйста, заполните все поля"
                                }
                                !login.value.matches(emailRegex) -> {
                                    errorMessage.value = "Неверный формат email"
                                    isLoginError.value = true
                                }
                                password.value.length < 6 -> {
                                    errorMessage.value = "Короткий пароль (минимум 6 символов)"
                                    isPasswordError.value = true
                                }
                                else -> {
                                    errorMessage.value = ""
                                    viewModel.login(login.value, password.value)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Войти")
                    }
                }
            }
            LoginState.Success -> {
                LaunchedEffect(Unit) {
                    onLogin()
                }
            }
            is LoginState.Error -> {
                LaunchedEffect(loginState) {
                    val error = loginState as LoginState.Error
                    errorMessage.value = error.message
                    isLoginError.value = error.message.contains("Неверный логин или пароль")
                    isPasswordError.value = error.message.contains("Неверный логин или пароль")
                    viewModel.resetState()
                }
                ErrorScreen {
                    viewModel.login(login.value, password.value)
                }
            }
        }
    }
}