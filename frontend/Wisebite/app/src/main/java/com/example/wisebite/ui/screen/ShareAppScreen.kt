 package com.example.wisebite.ui.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.ui.component.SimpleHeader
import com.example.wisebite.ui.theme.*

@Composable
fun ShareAppScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        SimpleHeader(
            title = "Chia sẻ ứng dụng",
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share app",
                modifier = Modifier.size(80.dp),
                tint = Green500
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Mời bạn bè tham gia WiseBite",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Cùng nhau giảm thiểu lãng phí thực phẩm và bảo vệ môi trường. Chia sẻ WiseBite với bạn bè và gia đình của bạn!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Green500.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🌱 Tham gia phong trào giảm thiểu lãng phí thực phẩm",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green700,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "💚 Khám phá những món ăn ngon với giá ưu đãi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green700,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "🎁 Nhận WiseToken miễn phí mỗi ngày",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green700,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { shareApp(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chia sẻ WiseBite",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Cảm ơn bạn đã giúp chúng tôi lan tỏa thông điệp tích cực!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

private fun shareApp(context: Context) {
    val shareText = """
        🌱 Tham gia WiseBite - Ứng dụng giảm thiểu lãng phí thực phẩm!
        
        💚 Khám phá những món ăn ngon với giá ưu đãi
        🎁 Nhận WiseToken miễn phí mỗi ngày
        🌍 Cùng bảo vệ môi trường
        
        Tải WiseBite ngay: [Link sẽ được cập nhật]
        
        #WiseBite #GiảmLãngPhí #MôiTrường #ĂnNgon
    """.trimIndent()
    
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "Tham gia WiseBite cùng tôi!")
    }
    
    context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ WiseBite qua"))
}