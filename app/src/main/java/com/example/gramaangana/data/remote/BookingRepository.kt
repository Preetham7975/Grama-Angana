package com.example.gramaangana.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}

data class BookingRequest(
    var id: String = "",
    val eventName: String = "",
    val date: String = "",
    val timeSlot: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val purpose: String = "",
    val contactNumber: String = "",
    val status: String = "Pending",
    val creatorId: String = ""
)

class BookingRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun getAllBookings(): Result<List<BookingRequest>> {
        return try {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val result = firestore.collection("bookings")
                .whereGreaterThanOrEqualTo("date", todayStr)
                .get()
                .await()
            
            // Map Firestore documents directly into our Data Class and extract Document ID
            val bookingsList = result.documents.mapNotNull { doc ->
                doc.toObject(BookingRequest::class.java)?.apply {
                    id = doc.id
                }
            }.filter { it.status == "Approved" || it.status == "Pending" }
            
            Result.success(bookingsList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitBookingRequest(request: BookingRequest): Result<Boolean> {
        return try {
            val existingBookings = firestore.collection("bookings")
                .whereEqualTo("date", request.date)
                .whereEqualTo("timeSlot", request.timeSlot)
                .whereIn("status", listOf("Approved", "Pending"))
                .get()
                .await()

            if (!existingBookings.isEmpty) {
                return Result.failure(Exception("This slot is already booked or pending approval."))
            }

            // Automatically inject the currently logged-in user's UID
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val requestWithCreator = request.copy(creatorId = currentUserId)

            firestore.collection("bookings").add(requestWithCreator).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBookingRequest(documentId: String): Result<Boolean> {
        return try {
            firestore.collection("bookings").document(documentId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBookingRequest(documentId: String, request: BookingRequest): Result<Boolean> {
        return try {
            // Check double booking again, excluding this specific document
            val existingBookings = firestore.collection("bookings")
                .whereEqualTo("date", request.date)
                .whereEqualTo("timeSlot", request.timeSlot)
                .whereIn("status", listOf("Approved", "Pending"))
                .get()
                .await()

            val isDoubleBooked = existingBookings.documents.any { it.id != documentId }
            if (isDoubleBooked) {
                return Result.failure(Exception("This slot is already booked or pending approval."))
            }

            firestore.collection("bookings").document(documentId).set(request).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
