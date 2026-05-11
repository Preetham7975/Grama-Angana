package com.example.gramaangana.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gramaangana.data.remote.MaintenanceItemDto
import com.example.gramaangana.data.remote.MaintenanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MaintenanceViewModel(
    private val maintenanceRepository: MaintenanceRepository = MaintenanceRepository()
) : ViewModel() {

    private val _maintenanceList = MutableStateFlow<List<MaintenanceItemDto>>(emptyList())
    val maintenanceList: StateFlow<List<MaintenanceItemDto>> = _maintenanceList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchMaintenanceItems(context: Context? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = maintenanceRepository.getAllMaintenanceItems()
                if (result.isSuccess) {
                    _maintenanceList.value = result.getOrNull() ?: emptyList()
                } else {
                    context?.let { Toast.makeText(it, "Network error. Please check your connection and try again.", Toast.LENGTH_SHORT).show() }
                }
            } catch (e: Exception) {
                context?.let { Toast.makeText(it, "Network error. Please check your connection and try again.", Toast.LENGTH_SHORT).show() }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun processPledge(context: Context, item: MaintenanceItemDto, rawAmount: String) {
        val requestedAmount = rawAmount.toDoubleOrNull()
        if (requestedAmount == null || requestedAmount <= 0) {
            Toast.makeText(context, "Please enter a valid amount greater than 0", Toast.LENGTH_SHORT).show()
            return
        }

        val remaining = item.goal - item.raised
        if (remaining <= 0) {
            Toast.makeText(context, "Goal already reached!", Toast.LENGTH_SHORT).show()
            return
        }

        val finalAmount = if (requestedAmount > remaining) {
            Toast.makeText(context, "Pledge capped at remaining amount: ₹${String.format("%.2f", remaining)}", Toast.LENGTH_LONG).show()
            remaining
        } else {
            requestedAmount
        }

        _isLoading.value = true
        val cleanAmount = String.format("%.2f", finalAmount)
        initiateUpiPayment(context, cleanAmount)
        _isLoading.value = false
    }

    private fun initiateUpiPayment(context: Context, amount: String) {
        // Real Live Demo UPI URI
        val upiUri = Uri.parse("upi://pay?pa=preetham7975@ybl&pn=Grama-Angana&am=$amount&cu=INR")
        val intent = Intent(Intent.ACTION_VIEW, upiUri)
        
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No UPI app installed", Toast.LENGTH_SHORT).show()
        }
    }
}
