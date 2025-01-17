package com.gdp.mytollid.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.gdp.mytollid.data.entity.Card

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE isActive = 1")
    fun getAllActiveCards(): LiveData<List<Card>>

    @Query("SELECT * FROM cards WHERE cardNumber = :cardNumber")
    suspend fun getCardByNumber(cardNumber: String): Card?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Card)

    @Update
    suspend fun updateCard(card: Card)

    @Delete
    suspend fun deleteCard(card: Card)

    @Query("UPDATE cards SET balance = :balance, lastCheck = :lastCheck WHERE cardNumber = :cardNumber")
    suspend fun updateCardBalance(cardNumber: String, balance: Double, lastCheck: Long)
} 