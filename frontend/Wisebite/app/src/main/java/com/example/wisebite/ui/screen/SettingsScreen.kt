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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var locationEnabled by remember { mutableStateOf(true) }
    var autoDownloadImages by remember { mutableStateOf(true) }
    var darkThemeEnabled by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("Tiếng Việt") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Cài đặt",
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
            // General Settings
            GeneralSettingsSection(
                notificationsEnabled = notificationsEnabled,
                onNotificationsToggle = { notificationsEnabled = it },
                locationEnabled = locationEnabled,
                onLocationToggle = { locationEnabled = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Preferences
            AppPreferencesSection(
                autoDownloadImages = autoDownloadImages,
                onAutoDownloadToggle = { autoDownloadImages = it },
                darkThemeEnabled = darkThemeEnabled,
                onThemeToggle = { darkThemeEnabled = it },
                selectedLanguage = selectedLanguage,
                onLanguageClick = { /* Show language picker */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Storage & Data
            StorageDataSection()

            Spacer(modifier = Modifier.height(24.dp))

            // App Information
            AppInformationSection()

            Spacer(modifier = Modifier.height(100.dp)) // Bottom padding
        }
    }
}

@Composable
fun GeneralSettingsSection(
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    locationEnabled: Boolean,
    onLocationToggle: (Boolean) -> Unit
) {
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
                text = "Cài đặt chung",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey800
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Notifications
            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "Thông báo",
                subtitle = "Nhận thông báo về đơn hàng và khuyến mãi",
                isEnabled = notificationsEnabled,
                onToggle = onNotificationsToggle
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = WarmGrey200
            )
            
            // Location Services
            SettingsToggleItem(
                icon = Icons.Default.LocationOn,
                title = "Dịch vụ vị trí",
                subtitle = "Tìm cửa hàng gần bạn nhất",
                isEnabled = locationEnabled,
                onToggle = onLocationToggle
            )
        }
    }
}

@Composable
fun AppPreferencesSection(
    autoDownloadImages: Boolean,
    onAutoDownloadToggle: (Boolean) -> Unit,
    darkThemeEnabled: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    selectedLanguage: String,
    onLanguageClick: () -> Unit
) {
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
                text = "Tùy chọn ứng dụng",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey800
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Auto download images
            SettingsToggleItem(
                icon = Icons.Default.Image,
                title = "Tự động tải hình ảnh",
                subtitle = "Tải hình ảnh khi kết nối WiFi",
                isEnabled = autoDownloadImages,
                onToggle = onAutoDownloadToggle
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = WarmGrey200
            )
            
            // Dark theme
            SettingsToggleItem(
                icon = Icons.Default.DarkMode,
                title = "Chế độ tối",
                subtitle = "Sử dụng giao diện tối",
                isEnabled = darkThemeEnabled,
                onToggle = onThemeToggle
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = WarmGrey200
            )
            
            // Language
            SettingsActionItem(
                icon = Icons.Default.Language,
                title = "Ngôn ngữ",
                subtitle = selectedLanguage,
                onClick = onLanguageClick
            )
        }
    }
}

@Composable
fun StorageDataSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Orange100),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Lưu trữ & Dữ liệu",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey800
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Cache size info
            StorageInfoItem(
                icon = Icons.Default.Storage,
                title = "Bộ nhớ cache",
                subtitle = "45.2 MB",
                actionText = "Xóa",
                onClick = { /* Clear cache */ }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = WarmGrey200
            )
            
            // Downloaded images
            StorageInfoItem(
                icon = Icons.Default.Image,
                title = "Hình ảnh đã tải",
                subtitle = "128.5 MB",
                actionText = "Xóa",
                onClick = { /* Clear downloaded images */ }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = WarmGrey200
            )
            
            // Data usage
            SettingsActionItem(
                icon = Icons.Default.DataUsage,
                title = "Sử dụng dữ liệu",
                subtitle = "Xem chi tiết sử dụng data",
                onClick = { /* Show data usage details */ }
            )
        }
    }
}

@Composable
fun AppInformationSection() {
    val context = LocalContext.current
    
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
                text = "Thông tin ứng dụng",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey800
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App version
            AppInfoItem(
                title = "Phiên bản",
                value = "1.0.0"
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = WarmGrey200
            )
            
            // Build number
            AppInfoItem(
                title = "Build",
                value = "2025.01.001"
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = WarmGrey200
            )
            
            // Last updated
            AppInfoItem(
                title = "Cập nhật lần cuối",
                value = "15/01/2025"
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = WarmGrey200
            )
            
            // Check for updates
            SettingsActionItem(
                icon = Icons.Default.SystemUpdate,
                title = "Kiểm tra cập nhật",
                subtitle = "Tìm kiếm phiên bản mới",
                onClick = { /* Check for updates */ }
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Orange200,
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
        
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Green500,
                checkedTrackColor = Green200
            )
        )
    }
}

@Composable
fun SettingsActionItem(
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
                    color = Orange200,
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
fun StorageInfoItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Orange200,
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
        
        TextButton(
            onClick = onClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = Red600
            )
        ) {
            Text(actionText)
        }
    }
}

@Composable
fun AppInfoItem(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = WarmGrey800
        )
        
        Text(
            text = value,
            fontSize = 16.sp,
            color = WarmGrey600
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    WisebiteTheme {
        SettingsScreen()
    }
}