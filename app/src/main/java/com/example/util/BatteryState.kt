package com.example.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object BatteryState {
    private val _isPowerSaveMode = MutableStateFlow(false)
    val isPowerSaveMode: StateFlow<Boolean> = _isPowerSaveMode

    fun setPowerSaveMode(isPowerSave: Boolean) {
        _isPowerSaveMode.value = isPowerSave
    }
}
