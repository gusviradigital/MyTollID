package com.gdp.mytollid.data.repository

import androidx.lifecycle.LiveData
import com.gdp.mytollid.data.dao.TollGateDao
import com.gdp.mytollid.data.entity.TollGate

class TollGateRepository(private val tollGateDao: TollGateDao) {
    fun getAllActiveTollGates(): LiveData<List<TollGate>> = tollGateDao.getAllActiveTollGates()

    fun getTopUpLocations(): LiveData<List<TollGate>> = tollGateDao.getTopUpLocations()

    fun getNearbyTollGates(lat: Double, lng: Double, limit: Int = 10): LiveData<List<TollGate>> =
        tollGateDao.getNearbyTollGates(lat, lng, limit)

    fun searchTollGates(query: String): LiveData<List<TollGate>> = tollGateDao.searchTollGates(query)

    suspend fun insertTollGates(gates: List<TollGate>) = tollGateDao.insertTollGates(gates)

    suspend fun deactivateTollGate(gateId: String) = tollGateDao.deactivateTollGate(gateId)
} 