package com.gdp.mytollid.data.repository

import androidx.lifecycle.LiveData
import com.gdp.mytollid.data.AppDatabase
import com.gdp.mytollid.data.entity.Card
import com.gdp.mytollid.data.entity.CardCategory
import java.util.Date

class CardRepository(private val database: AppDatabase) {
    
    fun getAllActiveCards(): LiveData<List<Card>> {
        return database.cardDao().getAllActiveCards()
    }

    suspend fun getCardByNumber(cardNumber: String): Card? {
        return database.cardDao().getCardByNumber(cardNumber)
    }

    suspend fun insertCard(card: Card) {
        database.cardDao().insertCard(card)
    }

    suspend fun updateCard(card: Card) {
        database.cardDao().updateCard(card)
    }

    suspend fun deleteCard(card: Card) {
        database.cardDao().deleteCard(card)
    }

    suspend fun updateCardBalance(cardNumber: String, balance: Double) {
        database.cardDao().updateCardBalance(cardNumber, balance, Date().time)
    }

    fun getCardsByCategory(category: CardCategory): LiveData<List<Card>> {
        return database.cardDao().getCardsByCategory(category)
    }

    suspend fun updateCardAlias(cardNumber: String, alias: String) {
        database.cardDao().updateCardAlias(cardNumber, alias)
    }

    suspend fun updateCardCategory(cardNumber: String, category: CardCategory) {
        database.cardDao().updateCardCategory(cardNumber, category)
    }

    suspend fun updateCardNotes(cardNumber: String, notes: String) {
        database.cardDao().updateCardNotes(cardNumber, notes)
    }

    fun searchCards(query: String): LiveData<List<Card>> {
        return database.cardDao().searchCards(query)
    }
} 