package com.gdp.mytollid.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey
    val cardNumber: String,
    val cardName: String = "",
    val balance: Double = 0.0,
    val lastCheck: Date = Date(),
    val isActive: Boolean = true,
    val isPremium: Boolean = false
) 