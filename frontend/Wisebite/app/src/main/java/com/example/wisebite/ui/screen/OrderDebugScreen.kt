package com.example.wisebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wisebite.data.model.CreateOrderItemRequest
import com.example.wisebite.data.model.CreateOrderRequest
import com.example.wisebite.ui.theme.*
import com.example.wisebite.ui.viewmodel.OrderViewModel
import com.example.wisebite.data.repository.TokenManager
import com.example.wisebite.data.repository.AuthRepository
import com.example.wisebite.util.ViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

@Composable
fun OrderDebugScreen() {
    val context = LocalContext.current
    val orderViewModel: OrderViewModel = viewModel(
        factory = ViewModelFactory.getInstance(context)
    )
    val authRepository = AuthRepository.getInstance(context)
    val uiState by orderViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    var tokenInfo by remember { mutableStateOf("Loading...") }
    var userInfo by remember { mutableStateOf("Not tested") }
    var debugLog by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        scope.launch {
            val tokenManager = TokenManager.getInstance(context)
            val token = tokenManager.getToken().first()
            tokenInfo = if (token.isNullOrEmpty()) {
                "❌ No token found - User not logged in!"
            } else {
                "✅ Token found: ${token.take(20)}..."
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Order System Debug",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = WarmGrey800
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Token Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Cream100)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Authentication Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGrey800
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tokenInfo,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = WarmGrey700
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "User API Test:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = WarmGrey800
                )
                Text(
                    text = userInfo,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = WarmGrey700
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // API Endpoints Test
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Cream100)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "API Endpoints",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGrey800
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Expected URLs:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = WarmGrey800
                )
                Text(
                    text = "POST: /api/v1/orders/\nGET: /api/v1/orders/me\nGET: /api/v1/orders/{id}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = WarmGrey700
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Test Buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Cream100)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Test Operations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGrey800
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Test getCurrentUser
                Button(
                    onClick = {
                        debugLog += "\\n👤 Testing getCurrentUser()..."
                        scope.launch {
                            authRepository.getCurrentUser()
                                .onSuccess { user ->
                                    userInfo = "✅ User: ${user.email}"
                                    debugLog += "\\n✅ getCurrentUser success: ${user.email}"
                                }
                                .onFailure { error ->
                                    userInfo = "❌ Error: ${error.message}"
                                    debugLog += "\\n❌ getCurrentUser error: ${error.message}"
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGrey600)
                ) {
                    Text("Test Get Current User")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Test Load Orders
                Button(
                    onClick = {
                        debugLog += "\\n📥 Testing getUserOrders()..."
                        orderViewModel.loadOrders()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Green500)
                ) {
                    Text("Test Load Orders")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Test Create Order
                Button(
                    onClick = {
                        debugLog += "\\n📤 Testing createOrder() with valid UUID..."
                        val testOrder = CreateOrderRequest(
                            items = listOf(
                                CreateOrderItemRequest(
                                    surpriseBagId = "550e8400-e29b-41d4-a716-446655440000",
                                    foodItemId = null,
                                    quantity = 1
                                )
                            ),
                            deliveryAddress = "Test Address",
                            notes = "Debug test order with valid UUID"
                        )
                        orderViewModel.createOrder(testOrder)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange600)
                ) {
                    Text("Test Create Order (Valid UUID)")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Test Create Order with invalid UUID format
                Button(
                    onClick = {
                        debugLog += "\\n📤 Testing createOrder() with invalid UUID format..."
                        val testOrder = CreateOrderRequest(
                            items = listOf(
                                CreateOrderItemRequest(
                                    surpriseBagId = "invalid-uuid-format",
                                    foodItemId = null,
                                    quantity = 1
                                )
                            ),
                            deliveryAddress = "Test Address",
                            notes = "Debug test order with invalid UUID"
                        )
                        orderViewModel.createOrder(testOrder)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Red600)
                ) {
                    Text("Test Create Order (Invalid UUID)")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Current State
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Cream100)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Current State",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGrey800
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Loading: ${uiState.isLoading}",
                    fontSize = 14.sp,
                    color = WarmGrey700
                )
                Text(
                    text = "Creating Order: ${uiState.isCreatingOrder}",
                    fontSize = 14.sp,
                    color = WarmGrey700
                )
                Text(
                    text = "Orders Count: ${uiState.orders.size}",
                    fontSize = 14.sp,
                    color = WarmGrey700
                )
                Text(
                    text = "Error: ${uiState.errorMessage ?: "None"}",
                    fontSize = 14.sp,
                    color = if (uiState.errorMessage != null) Red600 else WarmGrey700
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Debug Log
        if (debugLog.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = WarmGrey100)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Debug Log",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmGrey800
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = debugLog,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = WarmGrey700
                    )
                    
                    TextButton(onClick = { debugLog = "" }) {
                        Text("Clear Log")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}