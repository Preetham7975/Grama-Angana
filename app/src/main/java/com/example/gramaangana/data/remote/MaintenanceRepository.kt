package com.example.gramaangana.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

// DTO for Firestore
data class MaintenanceItemDto(
    var id: String = "",
    val title: String = "",
    val raised: Double = 0.0,
    val goal: Double = 0.0
)

class MaintenanceRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    
    suspend fun getAllMaintenanceItems(): Result<List<MaintenanceItemDto>> {
        return try {
            val result = firestore.collection("maintenance").get().await()
            val list = result.documents.mapNotNull { doc ->
                doc.toObject(MaintenanceItemDto::class.java)?.apply {
                    id = doc.id
                }
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pledgeSupport(itemId: String, amount: Double = 50.0): Result<Boolean> {
        return try {
            firestore.collection("maintenance").document(itemId)
                .update("raised", FieldValue.increment(amount))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
