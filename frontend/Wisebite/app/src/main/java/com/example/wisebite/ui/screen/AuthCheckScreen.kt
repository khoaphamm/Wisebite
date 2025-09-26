package com.example.wisebite.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wisebite.ui.theme.*
import com.example.wisebite.ui.viewmodel.AuthCheckViewModel
import com.example.wisebite.ui.viewmodel.AuthCheckViewModelFactory
import com.example.wisebite.data.repository.AuthRepository
import kotlinx.coroutines.delay

@Composable
fun AuthCheckScreen(
    onAuthenticated: () -> Unit,
    onNotAuthenticated: () -> Unit
) {
    val context = LocalContext.current
    val authRepository = AuthRepository.getInstance(context)
    val viewModel: AuthCheckViewModel = viewModel(
        factory = AuthCheckViewModelFactory(authRepository)
    )
    
    // Check authentication status when screen loads
    LaunchedEffect(Unit) {
        delay(1500) // Show splash for 1.5 seconds
        val isLoggedIn = viewModel.checkAuthenticationStatus()
        if (isLoggedIn) {
            onAuthenticated()
        } else {
            onNotAuthenticated()
        }
    }
    
    // Splash Screen UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Green500),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = "WiseBite Logo",
                modifier = Modifier.size(120.dp),
                tint = Color.White
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Name
            Text(
                text = "WiseBite",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 32.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Giảm thiểu lãng phí thực phẩm",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Loading indicator
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
        }
    }
}