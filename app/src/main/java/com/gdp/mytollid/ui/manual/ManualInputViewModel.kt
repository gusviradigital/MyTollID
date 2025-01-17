package com.gdp.mytollid.ui.manual

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gdp.mytollid.data.entity.Card
import com.gdp.mytollid.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class ManualInputViewModel(application: Application) : BaseViewModel(application) {
    private val _cardState = MutableLiveData<CardState>()
    val cardState: LiveData<CardState> = _cardState

    fun checkCard(cardNumber: String) {
        if (cardNumber.length < 16) {
            _cardState.value = CardState.Error("Nomor kartu harus 16 digit")
            return
        }

        viewModelScope.launch {
            try {
                val card = cardRepository.getCardByNumber(cardNumber)
                if (card != null) {
                    _cardState.value = CardState.Success(card)
                } else {
                    _cardState.value = CardState.Error("Kartu tidak ditemukan")
                }
            } catch (e: Exception) {
                _cardState.value = CardState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }
}

sealed class CardState {
    data class Success(val card: Card) : CardState()
    data class Error(val message: String) : CardState()
} 