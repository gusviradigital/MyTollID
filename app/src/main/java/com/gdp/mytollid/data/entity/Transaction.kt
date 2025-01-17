package com.gdp.mytollid.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Card::class,
            parentColumns = ["cardNumber"],
            childColumns = ["cardNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardNumber: String,
    val amount: Double,
    val balance: Double,
    val type: TransactionType,
    val location: String = "",
    val date: Date = Date()
)

enum class TransactionType {
    CHECK_BALANCE,
    TOP_UP,
    PAYMENT
} 