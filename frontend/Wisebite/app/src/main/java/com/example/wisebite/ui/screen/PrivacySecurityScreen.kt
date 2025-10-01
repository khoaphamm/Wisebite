package com.example.wisebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wisebite.ui.theme.*
import android.content.Intent
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(
    onNavigateBack: () -> Unit = {}
) {
    var dataPrivacyOptOut by remember { mutableStateOf(false) }
    var marketingOptOut by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Quyền riêng tư & Bảo mật",
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
            // Security Section
            SecuritySection(
                onChangePasswordClick = { showChangePasswordDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy Section
            PrivacySection(
                dataPrivacyOptOut = dataPrivacyOptOut,
                onDataPrivacyToggle = { dataPrivacyOptOut = it },
                marketingOptOut = marketingOptOut,
                onMarketingToggle = { marketingOptOut = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Legal Documents Section
            LegalDocumentsSection()

            Spacer(modifier = Modifier.height(100.dp)) // Bottom padding
        }
    }
    
    // Change Password Dialog
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { currentPassword, newPassword ->
                // TODO: Implement password change logic
                showChangePasswordDialog = false
            }
        )
    }
}

@Composable
fun SecuritySection(
    onChangePasswordClick: () -> Unit
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
                text = "Bảo mật",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey800
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Change Password
            SecurityActionItem(
                icon = Icons.Default.Lock,
                title = "Đổi mật khẩu",
                subtitle = "Cập nhật mật khẩu tài khoản của bạn",
                onClick = onChangePasswordClick
            )
        }
    }
}

@Composable
fun PrivacySection(
    dataPrivacyOptOut: Boolean,
    onDataPrivacyToggle: (Boolean) -> Unit,
    marketingOptOut: Boolean,
    onMarketingToggle: (Boolean) -> Unit
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
                text = "Quyền riêng tư",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey800
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Data Privacy
            SecurityToggleItem(
                icon = Icons.Default.Visibility,
                title = "Chia sẻ dữ liệu sử dụng",
                subtitle = "Cho phép WiseBite phân tích dữ liệu để cải thiện dịch vụ",
                isEnabled = !dataPrivacyOptOut,
                onToggle = { onDataPrivacyToggle(!it) }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = WarmGrey200
            )
            
            // Marketing Communications
            SecurityToggleItem(
                icon = Icons.Default.Visibility,
                title = "Nhận email marketing",
                subtitle = "Nhận thông tin khuyến mãi và ưu đãi qua email",
                isEnabled = !marketingOptOut,
                onToggle = { onMarketingToggle(!it) }
            )
        }
    }
}

@Composable
fun LegalDocumentsSection() {
    val context = LocalContext.current
    
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
                text = "Tài liệu pháp lý",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey800
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LegalDocumentItem(
                title = "Điều khoản sử dụng",
                subtitle = "Quy định và điều khoản khi sử dụng WiseBite",
                onClick = { showLegalDialog(context, "terms") }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = WarmGrey200
            )
            
            LegalDocumentItem(
                title = "Chính sách bảo mật",
                subtitle = "Cách chúng tôi thu thập và sử dụng dữ liệu",
                onClick = { showLegalDialog(context, "privacy") }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = WarmGrey200
            )
            
            LegalDocumentItem(
                title = "Chính sách cookie",
                subtitle = "Thông tin về việc sử dụng cookie",
                onClick = { showLegalDialog(context, "cookies") }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = WarmGrey200
            )
            
            LegalDocumentItem(
                title = "Liên hệ hỗ trợ pháp lý",
                subtitle = "legal@wisebite.com",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:legal@wisebite.com")
                        putExtra(Intent.EXTRA_SUBJECT, "WiseBite - Vấn đề pháp lý")
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun SecurityToggleItem(
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
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = WarmGrey600,
            modifier = Modifier.size(24.dp)
        )
        
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
fun SecurityActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = WarmGrey600,
            modifier = Modifier.size(24.dp)
        )
        
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
                contentColor = Orange600
            )
        ) {
            Text("Thay đổi")
        }
    }
}

@Composable
fun LegalDocumentItem(
    title: String,
    subtitle: String = "",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = title,
            tint = Orange600,
            modifier = Modifier.size(24.dp)
        )
        
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
            
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = WarmGrey600,
                    lineHeight = 18.sp
                )
            }
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Open",
            tint = WarmGrey400,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (currentPassword: String, newPassword: String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Đổi mật khẩu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGrey800
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Current Password
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { 
                        currentPassword = it
                        errorMessage = ""
                    },
                    label = { Text("Mật khẩu hiện tại") },
                    visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                            Icon(
                                imageVector = if (currentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // New Password
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { 
                        newPassword = it
                        errorMessage = ""
                    },
                    label = { Text("Mật khẩu mới") },
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        errorMessage = ""
                    },
                    label = { Text("Xác nhận mật khẩu mới") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword
                )
                
                // Password requirements
                Text(
                    text = "• Mật khẩu phải có ít nhất 8 ký tự\n• Bao gồm chữ hoa, chữ thường và số\n• Không chứa thông tin cá nhân",
                    fontSize = 12.sp,
                    color = WarmGrey600,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                // Error message
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Hủy")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            when {
                                currentPassword.isEmpty() -> errorMessage = "Vui lòng nhập mật khẩu hiện tại"
                                newPassword.isEmpty() -> errorMessage = "Vui lòng nhập mật khẩu mới"
                                newPassword.length < 8 -> errorMessage = "Mật khẩu mới phải có ít nhất 8 ký tự"
                                newPassword != confirmPassword -> errorMessage = "Mật khẩu xác nhận không khớp"
                                else -> {
                                    isLoading = true
                                    onConfirm(currentPassword, newPassword)
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Orange600
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Đổi mật khẩu")
                        }
                    }
                }
            }
        }
    }
}

private fun showLegalDialog(context: android.content.Context, type: String) {
    val content = when (type) {
        "terms" -> """
            ĐIỀU KHOẢN SỬ DỤNG WISEBITE
            
            1. CHẤP NHẬN ĐIỀU KHOẢN
            Khi sử dụng ứng dụng WiseBite, bạn đồng ý tuân thủ các điều khoản sau đây.
            
            2. DỊCH VỤ
            WiseBite là nền tảng kết nối người tiêu dùng với các cửa hàng thực phẩm để giảm lãng phí thức ăn thông qua "surprise bags".
            
            3. TRÁCH NHIỆM NGƯỜI DÙNG
            - Cung cấp thông tin chính xác khi đăng ký
            - Sử dụng dịch vụ một cách hợp pháp và phù hợp
            - Thanh toán đúng hạn cho các đơn hàng
            
            4. CHÍNH SÁCH HOÀN TIỀN
            - Hoàn tiền 100% nếu hủy trước khi cửa hàng xác nhận
            - Không hoàn tiền sau khi cửa hàng đã chuẩn bị đơn hàng
            
            5. GIỚI HẠN TRÁCH NHIỆM
            WiseBite không chịu trách nhiệm về chất lượng thực phẩm từ cửa hàng đối tác.
            
            Liên hệ: legal@wisebite.com
        """.trimIndent()
        
        "privacy" -> """
            CHÍNH SÁCH BẢO MẬT WISEBITE
            
            1. THÔNG TIN THU THẬP
            - Thông tin cá nhân: họ tên, email, số điện thoại
            - Thông tin vị trí để tìm cửa hàng gần nhất
            - Lịch sử giao dịch và sở thích mua sắm
            
            2. MỤC ĐÍCH SỬ DỤNG
            - Cung cấp và cải thiện dịch vụ
            - Xử lý đơn hàng và thanh toán
            - Gửi thông báo về đơn hàng và khuyến mãi
            - Phân tích để tối ưu trải nghiệm người dùng
            
            3. BẢO MẬT THÔNG TIN
            - Sử dụng mã hóa SSL/TLS cho tất cả giao dịch
            - Lưu trữ dữ liệu trên server bảo mật tại Việt Nam
            - Không chia sẻ thông tin với bên thứ ba khi chưa có sự đồng ý
            
            4. QUYỀN NGƯỜI DÙNG
            - Xem, cập nhật hoặc xóa thông tin cá nhân
            - Từ chối nhận email marketing
            - Yêu cầu xuất dữ liệu cá nhân
            
            5. COOKIE VÀ TRACKING
            - Sử dụng cookie để cải thiện trải nghiệm
            - Theo dõi hành vi sử dụng để tối ưu ứng dụng
            
            Cập nhật lần cuối: 01/01/2025
        """.trimIndent()
        
        "cookies" -> """
            CHÍNH SÁCH COOKIE WISEBITE
            
            1. COOKIE LÀ GÌ?
            Cookie là các file nhỏ được lưu trữ trên thiết bị để ghi nhớ thông tin về phiên làm việc của bạn.
            
            2. LOẠI COOKIE CHÚNG TÔI SỬ DỤNG
            
            Cookie thiết yếu:
            - Đăng nhập và xác thực
            - Giỏ hàng và thanh toán
            - Cài đặt bảo mật
            
            Cookie phân tích:
            - Google Analytics để hiểu cách sử dụng
            - Thống kê truy cập và tương tác
            
            Cookie tiếp thị:
            - Quảng cáo được cá nhân hóa
            - Theo dõi hiệu quả chiến dịch
            
            3. QUẢN LÝ COOKIE
            Bạn có thể:
            - Xóa cookie trong cài đặt trình duyệt
            - Chặn cookie (có thể ảnh hưởng đến chức năng)
            - Chọn loại cookie muốn chấp nhận
            
            4. COOKIE BÊN THỨ BA
            - Facebook Pixel: Quảng cáo trên Facebook
            - Google Ads: Quảng cáo trên Google
            - Zalo Analytics: Phân tích người dùng Việt Nam
            
            5. THỜI GIAN LƯU TRỮ
            - Cookie phiên: Xóa khi đóng ứng dụng
            - Cookie lâu dài: Tối đa 2 năm
            
            Để tắt cookie, vào Cài đặt > Quyền riêng tư
        """.trimIndent()
        
        else -> "Nội dung không tìm thấy"
    }
    
    // For now, we'll use a simple approach. In a real app, you'd want a proper dialog
    val intent = Intent(Intent.ACTION_SEND).apply {
        setType("text/plain")
        putExtra(Intent.EXTRA_TEXT, content)
        putExtra(Intent.EXTRA_SUBJECT, "WiseBite - Tài liệu pháp lý")
    }
    context.startActivity(Intent.createChooser(intent, "Chia sẻ tài liệu"))
}

@Preview(showBackground = true)
@Composable
fun PrivacySecurityScreenPreview() {
    WisebiteTheme {
        PrivacySecurityScreen()
    }
}