package com.gdp.mytollid.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gdp.mytollid.ui.base.BaseViewModel
import com.gdp.mytollid.util.BillingManager

class SettingsViewModel(application: Application) : BaseViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val billingManager = BillingManager.getInstance(application)
    
    private val _isPremium = MutableLiveData<Boolean>()
    val isPremium: LiveData<Boolean> = _isPremium

    private val _notificationEnabled = MutableLiveData<Boolean>()
    val notificationEnabled: LiveData<Boolean> = _notificationEnabled

    private val _purchaseEvent = MutableLiveData<PurchaseEvent>()
    val purchaseEvent: LiveData<PurchaseEvent> = _purchaseEvent

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

        // Observe purchase status
        billingManager.purchaseStatus.observeForever { status ->
            when (status) {
                BillingManager.PurchaseStatus.PURCHASED -> {
                    upgradeToPremium()
                    _purchaseEvent.value = PurchaseEvent.SUCCESS
                }
                BillingManager.PurchaseStatus.CANCELED -> {
                    _purchaseEvent.value = PurchaseEvent.CANCELED
                }
                BillingManager.PurchaseStatus.ERROR -> {
                    _purchaseEvent.value = PurchaseEvent.ERROR
                }
                else -> {}
            }
        }
    }

    fun initiatePurchase() {
        _purchaseEvent.value = PurchaseEvent.LOADING
    }

    fun getBillingManager() = billingManager

    companion object {
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
    }

    enum class PurchaseEvent {
        LOADING,
        SUCCESS,
        CANCELED,
        ERROR
    }
} 