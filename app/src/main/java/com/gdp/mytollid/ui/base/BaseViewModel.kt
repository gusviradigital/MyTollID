package com.gdp.mytollid.ui.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.gdp.mytollid.data.AppDatabase
import com.gdp.mytollid.data.repository.CardRepository
import com.gdp.mytollid.data.repository.TransactionRepository

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    protected val database = AppDatabase.getDatabase(application)
    protected val cardRepository = CardRepository(database)
    protected val transactionRepository = TransactionRepository(database)
} 