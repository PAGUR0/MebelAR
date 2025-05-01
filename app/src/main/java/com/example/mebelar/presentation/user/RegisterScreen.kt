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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mebelar.ui.theme.ErrorScreen
import com.example.mebelar.ui.theme.LoadingScreen

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegister: () -> Unit,
    onLogin: (String) -> Unit
) {
    val login = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val firstName = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }
    val isLoginError = remember { mutableStateOf(false) }
    val isPasswordError = remember { mutableStateOf(false) }
    val isConfirmPasswordError = remember { mutableStateOf(false) }
    val isFirstNameError = remember { mutableStateOf(false) }
    val isLastNameError = remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) } // Флаг для предотвращения повторной навигации

    val registerState by viewModel.registerState.collectAsState()

    // Регулярные выражения для валидации
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()
    val noDigitsRegex = "^[^0-9]*\$".toRegex()

    when (registerState) {
        RegisterState.Loading -> {
            LoadingScreen()
        }
        is RegisterState.Idle -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Регистрация",
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
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = confirmPassword.value,
                    onValueChange = {
                        if (it.length <= 50) {
                            confirmPassword.value = it
                            isConfirmPasswordError.value = false
                        }
                    },
                    label = { Text("Подтверждение пароля") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isConfirmPasswordError.value) 2.dp else 0.dp,
                            color = if (isConfirmPasswordError.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        ),
                    isError = isConfirmPasswordError.value,
                    singleLine = true,
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row {
                            if (isConfirmPasswordError.value) {
                                IconButton(onClick = { confirmPassword.value = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Очистить подтверждение пароля"
                                    )
                                }
                            }
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    imageVector = if (showConfirmPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (showConfirmPassword) "Скрыть пароль" else "Показать пароль"
                                )
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = firstName.value,
                    onValueChange = {
                        if (it.length <= 50) {
                            firstName.value = it
                            isFirstNameError.value = false
                        }
                    },
                    label = { Text("Имя") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isFirstNameError.value) 2.dp else 0.dp,
                            color = if (isFirstNameError.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        ),
                    isError = isFirstNameError.value,
                    singleLine = true,
                    trailingIcon = {
                        if (isFirstNameError.value) {
                            IconButton(onClick = { firstName.value = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Очистить имя"
                                )
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = lastName.value,
                    onValueChange = {
                        if (it.length <= 50) {
                            lastName.value = it
                            isLastNameError.value = false
                        }
                    },
                    label = { Text("Фамилия") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isLastNameError.value) 2.dp else 0.dp,
                            color = if (isLastNameError.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        ),
                    isError = isLastNameError.value,
                    singleLine = true,
                    trailingIcon = {
                        if (isLastNameError.value) {
                            IconButton(onClick = { lastName.value = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Очистить фамилию"
                                )
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Сброс ошибок перед новой валидацией
                        isLoginError.value = false
                        isPasswordError.value = false
                        isConfirmPasswordError.value = false
                        isFirstNameError.value = false
                        isLastNameError.value = false

                        when {
                            login.value.isBlank() || password.value.isBlank() || confirmPassword.value.isBlank() -> {
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
                            password.value != confirmPassword.value -> {
                                errorMessage.value = "Пароли не совпадают"
                                isPasswordError.value = true
                                isConfirmPasswordError.value = true
                            }
                            !firstName.value.matches(noDigitsRegex) || !lastName.value.matches(noDigitsRegex) -> {
                                errorMessage.value = "Имя и фамилия не должны содержать цифры"
                                isFirstNameError.value = !firstName.value.matches(noDigitsRegex)
                                isLastNameError.value = !lastName.value.matches(noDigitsRegex)
                            }
                            else -> {
                                errorMessage.value = ""
                                viewModel.register(login.value, password.value, firstName.value, lastName.value)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Зарегистрироваться")
                }
            }
        }
        RegisterState.Success -> {
            LaunchedEffect(Unit) {
                if (!hasNavigated) {
                    hasNavigated = true
                    onLogin(login.value)
                }
            }
        }
        is RegisterState.Error -> {
            LaunchedEffect(registerState) {
                val error = registerState as RegisterState.Error
                errorMessage.value = error.message
                isLoginError.value = error.message.contains("Пользователь уже существует")
                isPasswordError.value = error.message.contains("Пользователь уже существует")
                isConfirmPasswordError.value = error.message.contains("Пользователь уже существует")
                viewModel.resetState()
            }
            ErrorScreen {
                viewModel.register(login.value, password.value, firstName.value, lastName.value)
            }
        }
    }
}