package com.example.gramaangana.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gramaangana.data.remote.BookingRepository
import com.example.gramaangana.data.remote.BookingRequest
import com.example.gramaangana.data.remote.MaintenanceRepository
import com.example.gramaangana.data.remote.MaintenanceItemDto
import com.example.gramaangana.data.remote.UserRepository
import com.example.gramaangana.data.remote.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BookingState {
    object Idle : BookingState()
    object Loading : BookingState()
    object Success : BookingState()
    data class Error(val message: String) : BookingState()
}

class MainViewModel(
    private val bookingRepository: BookingRepository = BookingRepository(),
    private val maintenanceRepository: MaintenanceRepository = MaintenanceRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    // --- Booking Logic ---
    private val _bookingState = MutableStateFlow<BookingState>(BookingState.Idle)
    val bookingState: StateFlow<BookingState> = _bookingState

    private val _bookingsList = MutableStateFlow<List<BookingRequest>>(emptyList())
    val bookingsList: StateFlow<List<BookingRequest>> = _bookingsList

    fun fetchBookings() {
        viewModelScope.launch {
            val result = bookingRepository.getAllBookings()
            result.onSuccess { list ->
                _bookingsList.value = list
            }
        }
    }

    fun submitBooking(request: BookingRequest) {
        _bookingState.value = BookingState.Loading
        viewModelScope.launch {
            val result = bookingRepository.submitBookingRequest(request)
            result.onSuccess {
                _bookingState.value = BookingState.Success
                fetchBookings() 
            }.onFailure { exception ->
                _bookingState.value = BookingState.Error(exception.message ?: "Unknown error occurred")
            }
        }
    }

    fun deleteBooking(documentId: String) {
        viewModelScope.launch {
            val result = bookingRepository.deleteBookingRequest(documentId)
            if (result.isSuccess) {
                fetchBookings()
            }
        }
    }

    fun updateBooking(documentId: String, request: BookingRequest) {
        _bookingState.value = BookingState.Loading
        viewModelScope.launch {
            val result = bookingRepository.updateBookingRequest(documentId, request)
            result.onSuccess {
                _bookingState.value = BookingState.Success
                fetchBookings()
            }.onFailure { exception ->
                _bookingState.value = BookingState.Error(exception.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetBookingState() {
        _bookingState.value = BookingState.Idle
    }

    // --- Maintenance Logic ---
    private val _maintenanceList = MutableStateFlow<List<MaintenanceItemDto>>(emptyList())
    val maintenanceList: StateFlow<List<MaintenanceItemDto>> = _maintenanceList

    fun fetchMaintenanceItems() {
        viewModelScope.launch {
            val result = maintenanceRepository.getAllMaintenanceItems()
            result.onSuccess { list ->
                _maintenanceList.value = list
            }
        }
    }

    fun pledgeSupport(itemId: String) {
        viewModelScope.launch {
            val result = maintenanceRepository.pledgeSupport(itemId, 50.0)
            if (result.isSuccess) {
                fetchMaintenanceItems() 
            }
        }
    }

    // --- Profile Logic ---
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail

    private val _isProfileLoading = MutableStateFlow(false)
    val isProfileLoading: StateFlow<Boolean> = _isProfileLoading

    fun fetchUserProfile() {
        _userEmail.value = FirebaseAuth.getInstance().currentUser?.email ?: ""
        viewModelScope.launch {
            val result = userRepository.getUserProfile()
            if (result.isSuccess) {
                _userProfile.value = result.getOrNull()
            }
        }
    }

    fun updateUserProfile(name: String, contact: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (name.isBlank()) {
            onError("Name cannot be blank")
            return
        }
        if (contact.length != 10 || !contact.all { it.isDigit() }) {
            onError("Phone number must be exactly 10 digits")
            return
        }

        _isProfileLoading.value = true
        viewModelScope.launch {
            try {
                val result = userRepository.updateUserProfile(name, contact)
                if (result.isSuccess) {
                    fetchUserProfile()
                    onSuccess()
                } else {
                    onError("Network error. Please check your connection and try again.")
                }
            } catch (e: Exception) {
                onError("Network error. Please check your connection and try again.")
            } finally {
                _isProfileLoading.value = false
            }
        }
    }
}
