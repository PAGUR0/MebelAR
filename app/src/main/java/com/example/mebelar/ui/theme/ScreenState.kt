package com.example.mebelar.ui.theme

sealed class ScreenState {
    object Idle : ScreenState()
    object Loading : ScreenState()
    object Success : ScreenState()
    object Error : ScreenState()
}