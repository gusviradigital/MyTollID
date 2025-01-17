package com.gdp.mytollid.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gdp.mytollid.data.entity.TollGate

@Dao
interface TollGateDao {
    @Query("SELECT * FROM toll_gates WHERE isActive = 1")
    fun getAllActiveTollGates(): LiveData<List<TollGate>>

    @Query("SELECT * FROM toll_gates WHERE hasTopUp = 1 AND isActive = 1")
    fun getTopUpLocations(): LiveData<List<TollGate>>

    @Query("""
        SELECT *, 
        (((latitude - :lat) * (latitude - :lat)) + ((longitude - :lng) * (longitude - :lng))) as distance 
        FROM toll_gates 
        WHERE isActive = 1 
        ORDER BY distance ASC 
        LIMIT :limit
    """)
    fun getNearbyTollGates(lat: Double, lng: Double, limit: Int = 10): LiveData<List<TollGate>>

    @Query("SELECT * FROM toll_gates WHERE name LIKE '%' || :query || '%' AND isActive = 1")
    fun searchTollGates(query: String): LiveData<List<TollGate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTollGates(gates: List<TollGate>)

    @Query("UPDATE toll_gates SET isActive = 0 WHERE id = :gateId")
    suspend fun deactivateTollGate(gateId: String)
} 