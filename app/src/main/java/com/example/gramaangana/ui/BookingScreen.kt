package com.example.gramaangana.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gramaangana.data.remote.BookingRequest
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: MainViewModel = viewModel()
) {
    var eventName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var timeSlot by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }

    val bookingState by viewModel.bookingState.collectAsState()
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    LaunchedEffect(bookingState) {
        when (bookingState) {
            is BookingState.Success -> {
                Toast.makeText(context, "Booking requested successfully!", Toast.LENGTH_SHORT).show()
                eventName = ""
                date = ""
                timeSlot = ""
                startTime = ""
                endTime = ""
                purpose = ""
                contactNumber = ""
                viewModel.resetBookingState()
            }
            is BookingState.Error -> {
                val errorMessage = (bookingState as BookingState.Error).message
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                viewModel.resetBookingState()
            }
            else -> {}
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val startTimePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            startTime = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    val endTimePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            endTime = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Request Booking", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Ask the Panchayat for a slot.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = { Text("Event Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // Date Picker
        val dateInteractionSource = remember { MutableInteractionSource() }
        if (dateInteractionSource.collectIsPressedAsState().value) {
            datePickerDialog.show()
        }
        OutlinedTextField(
            value = date,
            onValueChange = {},
            readOnly = true,
            label = { Text("Date") },
            interactionSource = dateInteractionSource,
            modifier = Modifier.fillMaxWidth()
        )

        // Time Slot Dropdown
        var expanded by remember { mutableStateOf(false) }
        val options = listOf("Morning", "Afternoon", "Evening")
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = timeSlot,
                onValueChange = {},
                readOnly = true,
                label = { Text("Time Slot") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            timeSlot = selectionOption
                            expanded = false
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val startInteractionSource = remember { MutableInteractionSource() }
            if (startInteractionSource.collectIsPressedAsState().value) {
                startTimePickerDialog.show()
            }
            OutlinedTextField(
                value = startTime,
                onValueChange = {},
                readOnly = true,
                label = { Text("Start Time") },
                interactionSource = startInteractionSource,
                modifier = Modifier.weight(1f)
            )

            val endInteractionSource = remember { MutableInteractionSource() }
            if (endInteractionSource.collectIsPressedAsState().value) {
                endTimePickerDialog.show()
            }
            OutlinedTextField(
                value = endTime,
                onValueChange = {},
                readOnly = true,
                label = { Text("End Time") },
                interactionSource = endInteractionSource,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = purpose,
            onValueChange = { purpose = it },
            label = { Text("Purpose (e.g., Wedding, Sports)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        OutlinedTextField(
            value = contactNumber,
            onValueChange = { contactNumber = it },
            label = { Text("Contact Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (eventName.isNotBlank() && date.isNotBlank() && timeSlot.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()) {
                    val request = BookingRequest(
                        eventName = eventName,
                        date = date,
                        timeSlot = timeSlot,
                        startTime = startTime,
                        endTime = endTime,
                        purpose = purpose,
                        contactNumber = contactNumber
                    )
                    viewModel.submitBooking(request)
                } else {
                    Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = bookingState !is BookingState.Loading
        ) {
            if (bookingState is BookingState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Submit Request")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookingScreenPreview() {
    MaterialTheme {
        BookingScreen()
    }
}
