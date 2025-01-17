package com.gdp.mytollid.ui.history

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.gdp.mytollid.data.entity.Card
import com.gdp.mytollid.data.entity.Transaction
import com.gdp.mytollid.ui.base.BaseViewModel

class HistoryViewModel(application: Application) : BaseViewModel(application) {
    val activeCards: LiveData<List<Card>> = cardRepository.getAllActiveCards()
    
    private val _selectedCardNumber = MutableLiveData<String>()
    val selectedCardNumber: LiveData<String> = _selectedCardNumber

    val transactions: LiveData<List<Transaction>> = _selectedCardNumber.switchMap { cardNumber ->
        if (cardNumber.isEmpty()) {
            transactionRepository.getRecentTransactions()
        } else {
            transactionRepository.getTransactionsByCard(cardNumber)
        }
    }

    fun setSelectedCard(cardNumber: String) {
        _selectedCardNumber.value = cardNumber
    }

    init {
        _selectedCardNumber.value = ""  // Load all transactions initially
    }
} 