package com.gdp.mytollid.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "toll_gates")
data class TollGate(
    @PrimaryKey
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val operator: String,
    val isActive: Boolean = true,
    val hasTopUp: Boolean = false,
    val notes: String = ""
) 