package com.example.wisebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.ui.theme.*

@Composable
fun OrderConfirmationScreen(
    orderId: String = "",
    onNavigateToOrders: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success icon
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Order Success",
            modifier = Modifier.size(120.dp),
            tint = Green500
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Success title
        Text(
            text = "Order Placed Successfully!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = WarmGrey800,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Order ID
        if (orderId.isNotEmpty()) {
            Text(
                text = "Order #${orderId.take(8)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Green600,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Success message
        Text(
            text = "Thank you for your order! We'll notify you when it's ready for pickup.",
            fontSize = 16.sp,
            color = WarmGrey600,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Action buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // View Orders button
            Button(
                onClick = onNavigateToOrders,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "View My Orders",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            // Back to Home button
            OutlinedButton(
                onClick = onNavigateToHome,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Green500
                )
            ) {
                Text(
                    text = "Continue Shopping",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}