package com.example.wisebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.ui.theme.*
import android.content.Intent
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Trợ giúp & Hỗ trợ",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Quick Help Section
            QuickHelpSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Contact Support Section
            ContactSupportSection(
                onEmailClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@wisebite.com")
                        putExtra(Intent.EXTRA_SUBJECT, "WiseBite - Yêu cầu hỗ trợ")
                    }
                    context.startActivity(intent)
                },
                onPhoneClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:+84123456789")
                    }
                    context.startActivity(intent)
                },
                onChatClick = {
                    // TODO: Open in-app chat or external chat service
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // FAQ Section
            FAQSection()

            Spacer(modifier = Modifier.height(100.dp)) // Bottom padding
        }
    }
}

@Composable
fun QuickHelpSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Cream100),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Trợ giúp nhanh",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey800
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val quickHelpItems = listOf(
                Triple(Icons.Default.ShoppingCart, "Làm sao để đặt hàng?", "Hướng dẫn đặt mua surprise bag"),
                Triple(Icons.Default.Payment, "Phương thức thanh toán", "Các hình thức thanh toán được hỗ trợ"),
                Triple(Icons.Default.LocalShipping, "Vận chuyển & Giao hàng", "Thông tin về giao hàng và nhận tại cửa hàng"),
                Triple(Icons.Default.Cancel, "Hủy đơn hàng", "Cách hủy và hoàn tiền"),
                Triple(Icons.Default.Star, "WiseToken & Điểm thưởng", "Cách tích lũy và sử dụng WiseToken")
            )
            
            quickHelpItems.forEachIndexed { index, (icon, title, subtitle) ->
                QuickHelpItem(
                    icon = icon,
                    title = title,
                    subtitle = subtitle,
                    onClick = { /* Navigate to specific help topic */ }
                )
                
                if (index < quickHelpItems.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = WarmGrey200
                    )
                }
            }
        }
    }
}

@Composable
fun ContactSupportSection(
    onEmailClick: () -> Unit,
    onPhoneClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Green100),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Liên hệ hỗ trợ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey800
            )
            
            Text(
                text = "Chúng tôi sẵn sàng hỗ trợ bạn 24/7",
                fontSize = 14.sp,
                color = WarmGrey600,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email Support
            ContactItem(
                icon = Icons.Default.Email,
                title = "Email hỗ trợ",
                subtitle = "support@wisebite.com",
                onClick = onEmailClick
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = WarmGrey200
            )
            
            // Phone Support
            ContactItem(
                icon = Icons.Default.Phone,
                title = "Hotline",
                subtitle = "+84 123 456 789",
                onClick = onPhoneClick
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = WarmGrey200
            )
            
            // Live Chat
            ContactItem(
                icon = Icons.Default.Chat,
                title = "Chat trực tiếp",
                subtitle = "Phản hồi ngay lập tức",
                onClick = onChatClick
            )
        }
    }
}

@Composable
fun FAQSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Cream100),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Câu hỏi thường gặp",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey800
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val faqItems = listOf(
                "Surprise bag là gì?" to "Surprise bag là túi bất ngờ chứa thực phẩm chất lượng với giá ưu đãi từ các cửa hàng đối tác.",
                "Làm sao biết được nội dung trong bag?" to "Bạn sẽ biết loại thực phẩm nhưng không biết chính xác món nào - đó chính là điều bất ngờ!",
                "Có thể hoàn tiền không?" to "Bạn có thể hủy đơn hàng trước khi cửa hàng xác nhận và nhận hoàn tiền đầy đủ.",
                "WiseToken dùng để làm gì?" to "WiseToken có thể đổi quà, giảm giá cho đơn hàng tiếp theo hoặc tham gia các chương trình đặc biệt."
            )
            
            faqItems.forEachIndexed { index, (question, answer) ->
                var isExpanded by remember { mutableStateOf(false) }
                
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = question,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = WarmGrey800,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = WarmGrey600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    if (isExpanded) {
                        Text(
                            text = answer,
                            fontSize = 14.sp,
                            color = WarmGrey600,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                
                if (index < faqItems.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = WarmGrey200
                    )
                }
            }
        }
    }
}

@Composable
fun QuickHelpItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Orange100,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Orange600,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = WarmGrey800
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = WarmGrey600,
                lineHeight = 18.sp
            )
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Navigate",
            tint = WarmGrey400,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun ContactItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Green200,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Green600,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = WarmGrey800
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = WarmGrey600,
                lineHeight = 18.sp
            )
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Navigate",
            tint = WarmGrey400,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HelpSupportScreenPreview() {
    WisebiteTheme {
        HelpSupportScreen()
    }
}