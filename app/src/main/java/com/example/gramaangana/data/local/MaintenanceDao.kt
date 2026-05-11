package com.example.gramaangana.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Maintenance Items.
 */
@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance_items")
    fun getAllItems(): Flow<List<MaintenanceItem>>

    @Insert
    suspend fun insertItem(item: MaintenanceItem)

    @Query("UPDATE maintenance_items SET currentPledgedAmount = currentPledgedAmount + :pledge WHERE id = :itemId")
    suspend fun pledgeSupport(itemId: Int, pledge: Double)
}
