package com.gdp.mytollid.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gdp.mytollid.data.converter.CardCategoryConverter
import com.gdp.mytollid.data.dao.CardDao
import com.gdp.mytollid.data.dao.TollGateDao
import com.gdp.mytollid.data.dao.TransactionDao
import com.gdp.mytollid.data.entity.Card
import com.gdp.mytollid.data.entity.TollGate
import com.gdp.mytollid.data.entity.Transaction

@Database(
    entities = [Card::class, Transaction::class, TollGate::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(CardCategoryConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun transactionDao(): TransactionDao
    abstract fun tollGateDao(): TollGateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mytollid_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 