package com.example.wisebitemerchant.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.wisebitemerchant.ui.component.MerchantPhoneInputField
import com.example.wisebitemerchant.ui.theme.*
import com.example.wisebitemerchant.ui.viewmodel.SignupViewModel
import com.example.wisebitemerchant.util.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onNavigateBack: () -> Unit,
    onSignupSuccess: () -> Unit,
    viewModel: SignupViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSignupSuccessful by viewModel.isSignupSuccessful.collectAsStateWithLifecycle()
    
    // Handle successful signup
    LaunchedEffect(isSignupSuccessful) {
        if (isSignupSuccessful) {
            onSignupSuccess()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Header Section
        SignupHeader()

        Spacer(modifier = Modifier.height(32.dp))

        // Form Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MerchantInputField(
                label = "Họ và tên",
                value = uiState.fullName,
                onValueChange = viewModel::updateFullName,
                placeholder = "Nhập họ và tên của bạn",
                errorMessage = uiState.fullNameError
            )

            MerchantInputField(
                label = "Địa chỉ email",
                value = uiState.email,
                onValueChange = viewModel::updateEmail,
                placeholder = "Nhập địa chỉ email của bạn",
                keyboardType = KeyboardType.Email,
                errorMessage = uiState.emailError
            )

            MerchantPhoneInputField(
                label = "Số điện thoại",
                phoneNumber = uiState.phoneNumber,
                onPhoneNumberChange = viewModel::updatePhoneNumber,
                selectedCountry = uiState.selectedCountry,
                onCountryChange = viewModel::updateCountry,
                errorMessage = uiState.phoneNumberError
            )

            MerchantInputField(
                label = "Mật khẩu",
                value = uiState.password,
                onValueChange = viewModel::updatePassword,
                placeholder = "Tạo mật khẩu",
                isPassword = true,
                errorMessage = uiState.passwordError
            )

            MerchantInputField(
                label = "Xác nhận mật khẩu",
                value = uiState.confirmPassword,
                onValueChange = viewModel::updateConfirmPassword,
                placeholder = "Xác nhận mật khẩu của bạn",
                isPassword = true,
                errorMessage = uiState.confirmPasswordError
            )
        }

        // Display general error messages
        uiState.errorMessage?.let { errorMessage ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Signup Button
        Button(
            onClick = viewModel::signup,
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Orange600,
                contentColor = Color.White
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Đăng ký",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Spacer(modifier = Modifier.weight(1f))

        // Login Navigation
        LoginNavigation(
            onNavigateToLogin = onNavigateBack,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SignupHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // App Logo - Store icon for merchant app
        Icon(
            imageVector = Icons.Default.Store,
            contentDescription = "WiseBite Merchant Logo",
            modifier = Modifier.size(80.dp),
            tint = Orange600
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tạo tài khoản",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tham gia cộng đồng giảm lãng phí thực phẩm",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoginNavigation(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable(onClick = onNavigateToLogin)
    ) {
        Text(
            "Đã có tài khoản? ",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Text(
            "Đăng nhập",
            color = Orange600,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    WisebiteMerchantTheme {
        SignupScreen(
            onNavigateBack = {},
            onSignupSuccess = {}
        )
    }
}