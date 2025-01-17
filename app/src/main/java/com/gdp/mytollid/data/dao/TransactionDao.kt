package com.gdp.mytollid.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.gdp.mytollid.data.entity.Transaction

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE cardNumber = :cardNumber ORDER BY date DESC")
    fun getTransactionsByCard(cardNumber: String): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): LiveData<List<Transaction>>

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE cardNumber = :cardNumber")
    suspend fun deleteTransactionsByCard(cardNumber: String)

    @Query("SELECT * FROM transactions WHERE cardNumber = :cardNumber ORDER BY date DESC LIMIT 1")
    suspend fun getLatestTransaction(cardNumber: String): Transaction?
} 