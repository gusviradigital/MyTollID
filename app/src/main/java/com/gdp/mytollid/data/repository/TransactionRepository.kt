package com.gdp.mytollid.data.repository

import androidx.lifecycle.LiveData
import com.gdp.mytollid.data.AppDatabase
import com.gdp.mytollid.data.entity.Transaction
import com.gdp.mytollid.data.entity.TransactionType

class TransactionRepository(private val database: AppDatabase) {

    fun getTransactionsByCard(cardNumber: String): LiveData<List<Transaction>> {
        return database.transactionDao().getTransactionsByCard(cardNumber)
    }

    fun getRecentTransactions(limit: Int = 50): LiveData<List<Transaction>> {
        return database.transactionDao().getRecentTransactions(limit)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        database.transactionDao().insertTransaction(transaction)
    }

    suspend fun deleteTransactionsByCard(cardNumber: String) {
        database.transactionDao().deleteTransactionsByCard(cardNumber)
    }

    suspend fun getLatestTransaction(cardNumber: String): Transaction? {
        return database.transactionDao().getLatestTransaction(cardNumber)
    }

    suspend fun recordBalanceCheck(cardNumber: String, balance: Double) {
        val transaction = Transaction(
            cardNumber = cardNumber,
            amount = 0.0,
            balance = balance,
            type = TransactionType.CHECK_BALANCE
        )
        insertTransaction(transaction)
    }
} 