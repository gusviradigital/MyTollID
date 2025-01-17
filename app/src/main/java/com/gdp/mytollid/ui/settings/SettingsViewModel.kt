package com.gdp.mytollid.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gdp.mytollid.ui.base.BaseViewModel

class SettingsViewModel(application: Application) : BaseViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("settings", Context.MODE_PRIVATE)
    
    private val _isPremium = MutableLiveData<Boolean>()
    val isPremium: LiveData<Boolean> = _isPremium

    private val _notificationEnabled = MutableLiveData<Boolean>()
    val notificationEnabled: LiveData<Boolean> = _notificationEnabled

    fun setNotificationEnabled(enabled: Boolean) {
        _notificationEnabled.value = enabled
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }

    fun upgradeToPremium() {
        _isPremium.value = true
        sharedPreferences.edit().putBoolean(KEY_IS_PREMIUM, true).apply()
    }

    fun shouldShowAds(): Boolean {
        return !isPremium.value!!
    }

    init {
        _isPremium.value = sharedPreferences.getBoolean(KEY_IS_PREMIUM, false)
        _notificationEnabled.value = sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, false)
    }

    companion object {
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
    }
} 