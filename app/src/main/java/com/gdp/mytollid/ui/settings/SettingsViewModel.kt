package com.gdp.mytollid.ui.settings

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gdp.mytollid.ui.base.BaseViewModel

class SettingsViewModel(application: Application) : BaseViewModel(application) {
    private val _isPremium = MutableLiveData<Boolean>()
    val isPremium: LiveData<Boolean> = _isPremium

    private val _notificationEnabled = MutableLiveData<Boolean>()
    val notificationEnabled: LiveData<Boolean> = _notificationEnabled

    fun setNotificationEnabled(enabled: Boolean) {
        _notificationEnabled.value = enabled
        // TODO: Save to SharedPreferences
    }

    fun upgradeToPremium() {
        // TODO: Implement in-app purchase
    }

    init {
        // TODO: Load settings from SharedPreferences
        _isPremium.value = false
        _notificationEnabled.value = false
    }
} 