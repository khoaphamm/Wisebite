package com.example.wisebite.ui.screen

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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wisebite.R
import com.example.wisebite.data.repository.AuthRepository
import com.example.wisebite.ui.component.SimpleHeader
import com.example.wisebite.ui.component.ProfileCardWithImageUpload
import com.example.wisebite.ui.theme.*
import com.example.wisebite.ui.viewmodel.ProfileViewModel
import com.example.wisebite.ui.viewmodel.ProfileViewModelFactory

@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToPrivacySecurity: () -> Unit = {},
    onNavigateToHelpSupport: () -> Unit = {},
    onNavigateToShareApp: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToOrderHistory: () -> Unit = {}
) {
    val context = LocalContext.current
    val authRepository = AuthRepository.getInstance(context)
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(authRepository, context)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Simple header like in the image with refresh option
        SimpleHeader(
            title = "Hồ sơ",
            action = {
                IconButton(onClick = { viewModel.refreshUserData() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Show loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Show error message
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Show success message
            uiState.uploadSuccess?.let { success ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Green100
                    )
                ) {
                    Text(
                        text = success,
                        color = Green500,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            ProfileCardWithImageUpload(
                userName = uiState.user?.fullName ?: "Loading...",
                userEmail = uiState.user?.email ?: "Loading...",
                avatarUrl = uiState.user?.avatarUrl,
                isUploadingAvatar = uiState.isUploadingAvatar,
                onEditClick = onNavigateToEditProfile,
                onAvatarSelected = { uri -> viewModel.uploadAvatar(uri) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            DonationCard()
            Spacer(modifier = Modifier.height(16.dp))
            WiseBitePlusCard()
            Spacer(modifier = Modifier.height(16.dp))
            WiseTokenCard()
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSection(
                onLogout = { viewModel.logout(onLogout) },
                onEditProfile = onNavigateToEditProfile,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToPaymentMethods = onNavigateToPaymentMethods,
                onNavigateToPrivacySecurity = onNavigateToPrivacySecurity,
                onNavigateToHelpSupport = onNavigateToHelpSupport,
                onNavigateToShareApp = onNavigateToShareApp,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToOrderHistory = onNavigateToOrderHistory
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Điều khoản sử dụng",
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
fun ProfileCard(
    userName: String = "Nguyen Van A",
    userEmail: String = "nguyenvana@example.com",
    onEditClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.man), // Updated drawable
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = userEmail,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onEditClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightButtonSecondary,
                    contentColor = WarmGrey800
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = "Chỉnh sửa", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun DonationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Charity",
                    tint = WarmGrey600,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Đóng góp từ thiện",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quỹ từ thiện cứu đói là các tổ chức hoặc quỹ xã hội, từ thiện được thành lập nhằm mục đích hỗ trợ những người gặp khó khăn, đặc biệt là những người bị đói, nghèo, và thiếu thốn lương thực Việt Nam",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Số tiền bạn đã đóng góp:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "900đ",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Mỗi lần bạn mua blind bag, 5% giá trị sẽ được chuyển vào quỹ từ thiện",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WiseBitePlusCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "👑", fontSize = 20.sp) // Crown emoji
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nâng cấp WiseBite+",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Truy cập sớm, voucher cao cấp, 200 WiseToken/ngày và nhiều hơn nữa!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Chỉ 25.000đ/tháng",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Go to upgrade",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun WiseTokenCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "WiseToken",
                    tint = Cream400, // Using a specific color from theme for star
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WiseToken",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Số WiseToken hiện có:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "200",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Button(
                    onClick = { /* Handle gift center */ },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmGrey800,
                        contentColor = White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = "Trung tâm đổi quà", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    onLogout: () -> Unit,
    onEditProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToPrivacySecurity: () -> Unit = {},
    onNavigateToHelpSupport: () -> Unit = {},
    onNavigateToShareApp: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToOrderHistory: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            val menuItems = listOf(
                "Chỉnh sửa hồ sơ" to Icons.Default.Edit,
                "Lịch sử đơn hàng" to Icons.Default.History,
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
                            "Lịch sử đơn hàng" -> onNavigateToOrderHistory()
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
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
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
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = textColor,
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
    WisebiteTheme {
        ProfileScreen()
    }
}