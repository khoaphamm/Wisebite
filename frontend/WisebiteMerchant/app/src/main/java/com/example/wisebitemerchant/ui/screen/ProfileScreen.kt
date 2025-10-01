package com.example.wisebitemerchant.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebitemerchant.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToStoreSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToPrivacySecurity: () -> Unit = {},
    onNavigateToHelpSupport: () -> Unit = {},
    onNavigateToShareApp: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with refresh
        TopAppBar(
            title = {
                Text(
                    text = "Hồ sơ",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { /* TODO: Refresh user data */ }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Merchant Profile Card
            MerchantProfileCard(
                merchantName = "Test Minimart", // TODO: Get from user data
                merchantEmail = "vendor@test.com", // TODO: Get from user data
                storeName = "Test Minimart",
                onEditClick = onNavigateToEditProfile
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Store Performance Card
            StorePerformanceCard(
                onAnalyticsClick = onNavigateToAnalytics
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Merchant Settings Section
            MerchantSettingsSection(
                onLogout = onLogout,
                onEditProfile = onNavigateToEditProfile,
                onStoreSettings = onNavigateToStoreSettings,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToPaymentMethods = onNavigateToPaymentMethods,
                onNavigateToAnalytics = onNavigateToAnalytics,
                onNavigateToPrivacySecurity = onNavigateToPrivacySecurity,
                onNavigateToHelpSupport = onNavigateToHelpSupport,
                onNavigateToShareApp = onNavigateToShareApp,
                onNavigateToSettings = onNavigateToSettings
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Điều khoản sử dụng cho đối tác",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            // Bottom padding for navigation bar
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun MerchantProfileCard(
    merchantName: String = "Test Merchant",
    merchantEmail: String = "merchant@example.com",
    storeName: String = "My Store",
    onEditClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Orange50),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Image Placeholder
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Orange200),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = "Store",
                        tint = Orange600,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = merchantName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = merchantEmail,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Cửa hàng: $storeName",
                        fontSize = 12.sp,
                        color = Orange600,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Button(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Orange600,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = "Chỉnh sửa", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StorePerformanceCard(
    onAnalyticsClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Orange50),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hiệu suất cửa hàng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                TextButton(
                    onClick = onAnalyticsClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Orange600
                    )
                ) {
                    Text("Xem chi tiết")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PerformanceMetric(
                    title = "Đơn hàng hôm nay",
                    value = "12",
                    modifier = Modifier.weight(1f)
                )
                
                PerformanceMetric(
                    title = "Doanh thu tuần",
                    value = "2.5M đ",
                    modifier = Modifier.weight(1f)
                )
                
                PerformanceMetric(
                    title = "Surplus đã bán",
                    value = "8",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PerformanceMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Orange600
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MerchantSettingsSection(
    onLogout: () -> Unit,
    onEditProfile: () -> Unit = {},
    onStoreSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToPrivacySecurity: () -> Unit = {},
    onNavigateToHelpSupport: () -> Unit = {},
    onNavigateToShareApp: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Orange50),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            val menuItems = listOf(
                "Chỉnh sửa hồ sơ" to Icons.Default.Edit,
                "Cài đặt cửa hàng" to Icons.Default.Store,
                "Phân tích & Báo cáo" to Icons.Default.Analytics,
                "Thông báo" to Icons.Default.Notifications,
                "Phương thức thanh toán" to Icons.Default.CreditCard,
                "Quyền riêng tư & Bảo mật" to Icons.Default.Security,
                "Trợ giúp & Hỗ trợ" to Icons.Default.HelpOutline,
                "Chia sẻ ứng dụng" to Icons.Default.Share,
                "Cài đặt" to Icons.Default.Settings,
            )

            menuItems.forEachIndexed { index, (text, icon) ->
                SettingsMenuItem(
                    icon = icon,
                    text = text,
                    onClick = {
                        when (text) {
                            "Chỉnh sửa hồ sơ" -> onEditProfile()
                            "Cài đặt cửa hàng" -> onStoreSettings()
                            "Phân tích & Báo cáo" -> onNavigateToAnalytics()
                            "Thông báo" -> onNavigateToNotifications()
                            "Phương thức thanh toán" -> onNavigateToPaymentMethods()
                            "Quyền riêng tư & Bảo mật" -> onNavigateToPrivacySecurity()
                            "Trợ giúp & Hỗ trợ" -> onNavigateToHelpSupport()
                            "Chia sẻ ứng dụng" -> onNavigateToShareApp()
                            "Cài đặt" -> onNavigateToSettings()
                        }
                    }
                )
                if (index < menuItems.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant,
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            SettingsMenuItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                text = "Đăng xuất",
                textColor = MaterialTheme.colorScheme.error,
                onClick = onLogout
            )
        }
    }
}

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = Orange100,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (textColor == MaterialTheme.colorScheme.error) textColor else Orange600,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    WisebiteMerchantTheme {
        ProfileScreen()
    }
}