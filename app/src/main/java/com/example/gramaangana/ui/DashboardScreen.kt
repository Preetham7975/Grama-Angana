package com.example.gramaangana.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gramaangana.data.remote.BookingRequest
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

data class CalendarDay(val date: String, val dayOfWeek: String, val isFree: Boolean)

@Composable
fun DashboardScreen(
    viewModel: MainViewModel = viewModel()
) {
    val bookings by viewModel.bookingsList.collectAsState()
    var bookingToEdit by remember { mutableStateOf<BookingRequest?>(null) }
    val bookingState by viewModel.bookingState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchBookings()
    }

    // Show error toast if edit update fails
    LaunchedEffect(bookingState) {
        if (bookingState is BookingState.Error) {
            Toast.makeText(context, (bookingState as BookingState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetBookingState()
        } else if (bookingState is BookingState.Success) {
            viewModel.resetBookingState()
        }
    }

    val calendarDays = remember(bookings) {
        val today = LocalDate.now()
        (0..6).map { offset ->
            val date = today.plusDays(offset.toLong())
            val formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            
            val isFree = bookings.none { it.date == formattedDate }
            
            CalendarDay(
                date = date.dayOfMonth.toString(),
                dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                isFree = isFree
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Event Calendar", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(calendarDays) { day ->
                CalendarDateCard(day)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Upcoming Events", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (bookings.isEmpty()) {
                item {
                    Text("No upcoming events found.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                items(bookings) { booking ->
                    EventCard(
                        booking = booking,
                        onEditClick = { bookingToEdit = booking },
                        onDeleteClick = { viewModel.deleteBooking(booking.id) }
                    )
                }
            }
        }
    }

    // Show Edit Dialog when a booking is selected
    if (bookingToEdit != null) {
        EditBookingDialog(
            booking = bookingToEdit!!,
            onDismiss = { bookingToEdit = null },
            onSave = { updatedBooking ->
                viewModel.updateBooking(bookingToEdit!!.id, updatedBooking)
                bookingToEdit = null
            }
        )
    }
}

@Composable
fun CalendarDateCard(day: CalendarDay) {
    val statusColor = if (day.isFree) Color(0xFF4CAF50) else Color(0xFFF44336) 

    Card(
        modifier = Modifier.size(70.dp, 90.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(day.dayOfWeek, style = MaterialTheme.typography.bodySmall)
            Text(day.date, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
        }
    }
}

@Composable
fun EventCard(
    booking: BookingRequest,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(booking.eventName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                
                // Task 2: Authorization logic for Edit/Delete buttons
                if (currentUserId != null && currentUserId == booking.creatorId) {
                    Row {
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            val timeString = if (booking.startTime.isNotBlank() && booking.endTime.isNotBlank()) {
                "${booking.startTime} - ${booking.endTime}"
            } else {
                booking.timeSlot
            }
            
            Text("${booking.date} | $timeString", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(booking.purpose, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookingDialog(
    booking: BookingRequest,
    onDismiss: () -> Unit,
    onSave: (BookingRequest) -> Unit
) {
    var eventName by remember { mutableStateOf(booking.eventName) }
    var date by remember { mutableStateOf(booking.date) }
    var timeSlot by remember { mutableStateOf(booking.timeSlot) }
    var startTime by remember { mutableStateOf(booking.startTime) }
    var endTime by remember { mutableStateOf(booking.endTime) }
    var purpose by remember { mutableStateOf(booking.purpose) }
    var contactNumber by remember { mutableStateOf(booking.contactNumber) }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    val datePickerDialog = DatePickerDialog(context, { _, y, m, d -> date = String.format("%04d-%02d-%02d", y, m + 1, d) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    val startTimePickerDialog = TimePickerDialog(context, { _, h, m -> startTime = String.format("%02d:%02d", h, m) }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
    val endTimePickerDialog = TimePickerDialog(context, { _, h, m -> endTime = String.format("%02d:%02d", h, m) }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Booking") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(value = eventName, onValueChange = { eventName = it }, label = { Text("Event Name") }, modifier = Modifier.fillMaxWidth())

                val dateInteractionSource = remember { MutableInteractionSource() }
                if (dateInteractionSource.collectIsPressedAsState().value) { datePickerDialog.show() }
                OutlinedTextField(value = date, onValueChange = {}, readOnly = true, label = { Text("Date") }, interactionSource = dateInteractionSource, modifier = Modifier.fillMaxWidth())

                var expanded by remember { mutableStateOf(false) }
                val options = listOf("Morning", "Afternoon", "Evening")
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = timeSlot, onValueChange = {}, readOnly = true, label = { Text("Time Slot") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        options.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { timeSlot = option; expanded = false })
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val startInteractionSource = remember { MutableInteractionSource() }
                    if (startInteractionSource.collectIsPressedAsState().value) { startTimePickerDialog.show() }
                    OutlinedTextField(value = startTime, onValueChange = {}, readOnly = true, label = { Text("Start Time") }, interactionSource = startInteractionSource, modifier = Modifier.weight(1f))

                    val endInteractionSource = remember { MutableInteractionSource() }
                    if (endInteractionSource.collectIsPressedAsState().value) { endTimePickerDialog.show() }
                    OutlinedTextField(value = endTime, onValueChange = {}, readOnly = true, label = { Text("End Time") }, interactionSource = endInteractionSource, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(value = purpose, onValueChange = { purpose = it }, label = { Text("Purpose") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = contactNumber, onValueChange = { contactNumber = it }, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (eventName.isNotBlank() && date.isNotBlank() && timeSlot.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()) {
                    onSave(booking.copy(
                        eventName = eventName,
                        date = date,
                        timeSlot = timeSlot,
                        startTime = startTime,
                        endTime = endTime,
                        purpose = purpose,
                        contactNumber = contactNumber
                    ))
                } else {
                    Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                }
            }) { Text("Save Changes") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MaterialTheme {
        DashboardScreen()
    }
}
