package com.gdp.mytollid.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.gdp.mytollid.data.entity.Card
import com.gdp.mytollid.data.entity.CardCategory

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

    @Query("SELECT * FROM cards WHERE category = :category AND isActive = 1")
    fun getCardsByCategory(category: CardCategory): LiveData<List<Card>>

    @Query("UPDATE cards SET cardAlias = :alias WHERE cardNumber = :cardNumber")
    suspend fun updateCardAlias(cardNumber: String, alias: String)

    @Query("UPDATE cards SET category = :category WHERE cardNumber = :cardNumber")
    suspend fun updateCardCategory(cardNumber: String, category: CardCategory)

    @Query("UPDATE cards SET notes = :notes WHERE cardNumber = :cardNumber")
    suspend fun updateCardNotes(cardNumber: String, notes: String)

    @Query("SELECT * FROM cards WHERE cardAlias LIKE '%' || :query || '%' OR cardNumber LIKE '%' || :query || '%'")
    fun searchCards(query: String): LiveData<List<Card>>
} 