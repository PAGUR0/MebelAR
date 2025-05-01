package com.example.mebelar.ui.state

sealed class ScreenState {
    object Idle : ScreenState()
    object Loading : ScreenState()
    object Error : ScreenState()
}