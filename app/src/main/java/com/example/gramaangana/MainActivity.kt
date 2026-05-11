package com.example.gramaangana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gramaangana.ui.DashboardScreen
import com.example.gramaangana.ui.BookingScreen
import com.example.gramaangana.ui.MaintenanceScreen
import com.example.gramaangana.ui.SplashScreen
import com.example.gramaangana.ui.LoginScreen
import com.example.gramaangana.ui.ProfileScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Screens that require the Top Bar and Bottom Bar
    val authenticatedRoutes = listOf("dashboard", "booking", "maintenance", "profile")
    val showBottomBarRoutes = listOf("dashboard", "booking", "maintenance")

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (currentRoute in authenticatedRoutes) {
                TopAppBar(
                    title = { Text("Grama-Angana") },
                    navigationIcon = {
                        if (navController.previousBackStackEntry != null && currentRoute !in showBottomBarRoutes) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Profile") },
                                onClick = { 
                                    showMenu = false
                                    navController.navigate("profile") { launchSingleTop = true }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = { 
                                    showMenu = false
                                    FirebaseAuth.getInstance().signOut()
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true } // Clear entire backstack on logout
                                    }
                                }
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentRoute in showBottomBarRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "dashboard",
                        onClick = { navController.navigate("dashboard") { launchSingleTop = true; popUpTo("dashboard") } },
                        icon = { Text("D") },
                        label = { Text("Dashboard") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "booking",
                        onClick = { navController.navigate("booking") { launchSingleTop = true; popUpTo("dashboard") } },
                        icon = { Text("B") },
                        label = { Text("Book") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "maintenance",
                        onClick = { navController.navigate("maintenance") { launchSingleTop = true; popUpTo("dashboard") } },
                        icon = { Text("M") },
                        label = { Text("Jar") }
                    )
                }
            }
        }
    ) { innerPadding ->
        val startDestination = remember {
            if (FirebaseAuth.getInstance().currentUser != null) "dashboard" else "login"
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                SplashScreen(onTimeout = {
                    navController.navigate(startDestination) {
                        popUpTo("splash") { inclusive = true }
                    }
                })
            }
            composable("login") {
                LoginScreen(onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                })
            }
            composable("dashboard") { DashboardScreen() }
            composable("booking") { BookingScreen() }
            composable("maintenance") { MaintenanceScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}
