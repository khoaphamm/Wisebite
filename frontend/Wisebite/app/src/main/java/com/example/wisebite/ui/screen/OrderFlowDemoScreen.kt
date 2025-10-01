package com.example.wisebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.ui.theme.*

@Composable
fun OrderFlowDemoScreen() {
    var currentStep by remember { mutableStateOf("bag_details") }
    var simulatedOrderId by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Order Flow Demo",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = WarmGrey800,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        when (currentStep) {
            "bag_details" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Step 1: Bag Details Screen",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmGrey800
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "User views surprise bag details and clicks 'Order Now'",
                            fontSize = 14.sp,
                            color = WarmGrey600,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                simulatedOrderId = "ORD${System.currentTimeMillis().toString().takeLast(6)}"
                                currentStep = "order_confirmation"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Green500)
                        ) {
                            Text("Simulate Order Creation")
                        }
                    }
                }
            }
            
            "order_confirmation" -> {
                OrderConfirmationScreen(
                    orderId = simulatedOrderId,
                    onNavigateToOrders = { currentStep = "orders_list" },
                    onNavigateToHome = { currentStep = "bag_details" }
                )
            }
            
            "orders_list" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Step 3: Orders Screen",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmGrey800
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "User can now see their order: $simulatedOrderId",
                            fontSize = 14.sp,
                            color = WarmGrey600,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = "bag_details" },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Green500)
                            ) {
                                Text("Reset Demo")
                            }
                            
                            Button(
                                onClick = { currentStep = "order_details" },
                                colors = ButtonDefaults.buttonColors(containerColor = Green500)
                            ) {
                                Text("View Order Details")
                            }
                        }
                    }
                }
            }
            
            "order_details" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Step 4: Order Details Screen",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmGrey800
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Detailed view of order: $simulatedOrderId\nStatus: Pending\nTotal: 18,000đ",
                            fontSize = 14.sp,
                            color = WarmGrey600,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = "orders_list" },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Green500)
                            ) {
                                Text("Back to Orders")
                            }
                            
                            Button(
                                onClick = { currentStep = "bag_details" },
                                colors = ButtonDefaults.buttonColors(containerColor = Green500)
                            ) {
                                Text("Reset Demo")
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Flow explanation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Cream100)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Order Flow Explanation:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGrey800
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = """
                    1. User views surprise bag in BagDetailsScreen
                    2. User clicks "Đặt ngay" to place order
                    3. OrderViewModel creates order via API
                    4. Success → OrderConfirmationScreen
                    5. User can view orders in OrdersScreen
                    6. Tap order → OrderDetailsScreen with full info
                    
                    This creates the complete order lifecycle!
                    """.trimIndent(),
                    fontSize = 14.sp,
                    color = WarmGrey700,
                    lineHeight = 20.sp
                )
            }
        }
    }
}