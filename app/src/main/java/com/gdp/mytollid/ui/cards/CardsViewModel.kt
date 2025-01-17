package com.gdp.mytollid.ui.cards

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.gdp.mytollid.data.entity.Card
import com.gdp.mytollid.data.entity.CardCategory
import com.gdp.mytollid.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class CardsViewModel(application: Application) : BaseViewModel(application) {
    private val _selectedCategory = MutableLiveData<CardCategory?>()
    val selectedCategory: LiveData<CardCategory?> = _selectedCategory

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    val cards: LiveData<List<Card>> = _selectedCategory.switchMap { category ->
        when {
            category != null -> cardRepository.getCardsByCategory(category)
            _searchQuery.value?.isNotEmpty() == true -> cardRepository.searchCards(_searchQuery.value!!)
            else -> cardRepository.getAllActiveCards()
        }
    }

    fun setCategory(category: CardCategory?) {
        _selectedCategory.value = category
        _searchQuery.value = ""
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _selectedCategory.value = null
    }

    fun updateCard(
        cardNumber: String,
        alias: String,
        category: CardCategory,
        notes: String
    ) {
        viewModelScope.launch {
            val card = cardRepository.getCardByNumber(cardNumber)
            if (card != null) {
                cardRepository.updateCard(
                    card.copy(
                        cardAlias = alias,
                        category = category,
                        notes = notes
                    )
                )
            }
        }
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            cardRepository.deleteCard(card)
        }
    }
} 