package com.gdp.mytollid.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class CardCategory {
    PERSONAL,
    BUSINESS,
    FAMILY,
    OTHER
}

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey
    val cardNumber: String,
    val cardName: String = "",
    val cardAlias: String = "",
    val category: CardCategory = CardCategory.PERSONAL,
    val balance: Double = 0.0,
    val lastCheck: Date = Date(),
    val isActive: Boolean = true,
    val isPremium: Boolean = false,
    val notes: String = ""
) 