package com.example.gramaangana.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel

class MaintenanceFundViewModel : ViewModel() {

    fun initiateUpiPayment(context: Context) {
        val upiUri = Uri.parse("upi://pay?pa=test@upi&pn=GramaAngana&am=50.00&cu=INR")
        val intent = Intent(Intent.ACTION_VIEW, upiUri)
        
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No UPI app found on this device", Toast.LENGTH_SHORT).show()
        }
    }
}
