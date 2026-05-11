package com.example.gramaangana.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a maintenance item needed for the community hall.
 */
@Entity(tableName = "maintenance_items")
data class MaintenanceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val currentPledgedAmount: Double
)
