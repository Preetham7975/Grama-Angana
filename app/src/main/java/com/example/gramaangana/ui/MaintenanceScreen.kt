package com.example.gramaangana.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gramaangana.data.remote.MaintenanceItemDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    viewModel: MaintenanceViewModel = viewModel()
) {
    val items by viewModel.maintenanceList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    // Fetch data immediately when this screen is launched
    LaunchedEffect(Unit) {
        viewModel.fetchMaintenanceItems(context)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Maintenance Jar", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Crowdfund repairs and community goals.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty() && isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (items.isEmpty()) {
            Text("No maintenance tasks right now!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(items) { item ->
                    MaintenanceItemCard(
                        item = item,
                        isLoading = isLoading,
                        onPledgeClick = { amount -> 
                            viewModel.processPledge(context, item, amount)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MaintenanceItemCard(item: MaintenanceItemDto, isLoading: Boolean, onPledgeClick: (String) -> Unit) {
    var pledgeAmount by remember { mutableStateOf("") }
    val progress = if (item.goal > 0) {
        (item.raised / item.goal).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
    
    val isFullyFunded = item.raised >= item.goal
    val progressColor = if (isFullyFunded) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "₹${item.raised} / ₹${item.goal}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (!isFullyFunded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = pledgeAmount,
                        onValueChange = { pledgeAmount = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    
                    Button(
                        onClick = { onPledgeClick(pledgeAmount) },
                        enabled = pledgeAmount.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Pledge via UPI")
                        }
                    }
                }
            } else {
                Text("Fully Funded!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
        }
    }
}
