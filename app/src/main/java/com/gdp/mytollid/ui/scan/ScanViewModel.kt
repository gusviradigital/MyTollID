package com.gdp.mytollid.ui.scan

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gdp.mytollid.data.entity.Card
import com.gdp.mytollid.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class ScanViewModel(application: Application) : BaseViewModel(application) {
    private val _scanResult = MutableLiveData<ScanResult>()
    val scanResult: LiveData<ScanResult> = _scanResult

    fun processNfcCard(cardNumber: String, balance: Double) {
        viewModelScope.launch {
            try {
                var card = cardRepository.getCardByNumber(cardNumber)
                if (card == null) {
                    card = Card(cardNumber = cardNumber, balance = balance)
                    cardRepository.insertCard(card)
                } else {
                    cardRepository.updateCardBalance(cardNumber, balance)
                }
                transactionRepository.recordBalanceCheck(cardNumber, balance)
                _scanResult.value = ScanResult.Success(card)
            } catch (e: Exception) {
                _scanResult.value = ScanResult.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }
}

sealed class ScanResult {
    data class Success(val card: Card) : ScanResult()
    data class Error(val message: String) : ScanResult()
} 