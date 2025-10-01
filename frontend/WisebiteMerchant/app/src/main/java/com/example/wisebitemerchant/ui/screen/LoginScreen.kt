package com.example.wisebitemerchant.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wisebitemerchant.ui.component.MerchantButton
import com.example.wisebitemerchant.ui.component.MerchantInputField
import com.example.wisebitemerchant.ui.theme.*
import com.example.wisebitemerchant.ui.viewmodel.LoginViewModel
import com.example.wisebitemerchant.util.ViewModelFactory
import android.app.Activity

@Composable
fun LoginScreen(
    onNavigateToSignup: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoginSuccessful by viewModel.isLoginSuccessful.collectAsStateWithLifecycle()

    // Lấy context và ép kiểu an toàn sang Activity
    val context = LocalContext.current
    val activity = context as? Activity
    // Handle successful login
    LaunchedEffect(isLoginSuccessful) {
        if (isLoginSuccessful) {
            onLoginSuccess()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Orange500.copy(alpha = 0.1f),
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // App Icon and Title
            Icon(
                imageVector = Icons.Default.Store,
                contentDescription = "WiseBite Merchant",
                modifier = Modifier.size(80.dp),
                tint = Orange600
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "WiseBite",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Orange600,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Merchant",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = WarmGrey600,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Quản lý cửa hàng và giảm thiểu lãng phí thực phẩm",
                fontSize = 14.sp,
                color = WarmGrey500,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Login Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Đăng nhập",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmGrey800,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    MerchantInputField(
                        label = "Số điện thoại",
                        value = uiState.phoneNumber,
                        onValueChange = viewModel::updatePhoneNumber,
                        placeholder = "Nhập số điện thoại của bạn",
                        keyboardType = KeyboardType.Phone
                    )
                    
                    MerchantInputField(
                        label = "Mật khẩu",
                        value = uiState.password,
                        onValueChange = viewModel::updatePassword,
                        placeholder = "Nhập mật khẩu của bạn",
                        isPassword = true
                    )
                    
                    // Display error message if any
                    uiState.errorMessage?.let { errorMessage ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    MerchantButton(
                        onClick = viewModel::login,
                        text = "Đăng nhập",
                        isLoading = uiState.isLoading,
                        containerColor = Orange600
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Divider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(
                            color = WarmGrey300,
                            thickness = 1.dp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "hoặc",
                            fontSize = 12.sp,
                            color = WarmGrey500,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        HorizontalDivider(
                            color = WarmGrey300,
                            thickness = 1.dp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Google Sign-In Button
                    GoogleSignInButton(
                        onClick = {
                            // Kiểm tra activity không null trước khi gọi
                            activity?.let {
                                viewModel.signInWithGoogle(it)
                            }
                        },
                        isLoading = uiState.isLoading
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Sign up link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chưa có tài khoản? ",
                    fontSize = 14.sp,
                    color = WarmGrey600
                )
                
                Text(
                    text = "Đăng ký ngay",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Orange600,
                    modifier = Modifier.clickable { onNavigateToSignup() }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Footer
            Text(
                text = "Phiên bản dành cho đối tác kinh doanh",
                fontSize = 12.sp,
                color = WarmGrey400,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    WisebiteMerchantTheme {
        LoginScreen(
            onNavigateToSignup = {},
            onLoginSuccess = {}
        )
    }
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        border = BorderStroke(1.dp, WarmGrey300)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Orange600
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google Icon (using a simple "G" for now)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            color = Orange600,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Đăng nhập với Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    }
}