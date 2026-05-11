package com.example.gramaangana.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val contact: String = ""
)

class UserRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun getUserProfile(): Result<UserProfile?> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure(Exception("No user logged in"))
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            Result.success(doc.toObject(UserProfile::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(name: String, contact: String): Result<Boolean> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure(Exception("No user logged in"))
        return try {
            firestore.collection("users").document(uid)
                .set(mapOf("name" to name, "contact" to contact), SetOptions.merge()).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
