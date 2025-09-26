package com.example.wisebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.data.model.SignupUiState
import com.example.wisebite.ui.component.PhoneNumberInput
import com.example.wisebite.ui.component.WisebiteInputField
import com.example.wisebite.ui.theme.*
import com.example.wisebite.ui.viewmodel.SignupViewModel

// This is the main, stateful composable that connects to the ViewModel
@Composable
fun SignupScreen(
    viewModel: SignupViewModel,
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSignupSuccessful by viewModel.isSignupSuccessful.collectAsState()

    // Handle signup success navigation
    LaunchedEffect(isSignupSuccessful) {
        if (isSignupSuccessful) {
            onSignupSuccess()
        }
    }

    // Call the stateless composable with data from the ViewModel
    SignupContent(
        uiState = uiState,
        onFullNameChange = viewModel::updateFullName,
        onEmailChange = viewModel::updateEmail,
        onPhoneNumberChange = viewModel::updatePhoneNumber,
        onCountryChange = viewModel::updateCountry,
        onPasswordChange = viewModel::updatePassword,
        onConfirmPasswordChange = viewModel::updateConfirmPassword,
        onAddressChange = viewModel::updateAddress,
        onSignupClick = viewModel::signup,
        onNavigateToLogin = onNavigateToLogin
    )
}

// This is the stateless composable for the UI, perfect for previews
@Composable
fun SignupContent(
    uiState: SignupUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onCountryChange: (com.example.wisebite.ui.component.CountryCode) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onSignupClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
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
            WisebiteInputField(
                label = "Họ và tên",
                value = uiState.fullName,
                onValueChange = onFullNameChange,
                placeholder = "Nhập họ và tên của bạn"
            )

            WisebiteInputField(
                label = "Địa chỉ email",
                value = uiState.email,
                onValueChange = onEmailChange,
                placeholder = "Nhập địa chỉ email của bạn",
                keyboardType = KeyboardType.Email
            )

            PhoneNumberInput(
                label = "Số điện thoại",
                phoneNumber = uiState.phoneNumber,
                onPhoneNumberChange = onPhoneNumberChange,
                selectedCountry = uiState.selectedCountry,
                onCountryChange = onCountryChange,
                errorMessage = uiState.phoneNumberError
            )

            WisebiteInputField(
                label = "Mật khẩu",
                value = uiState.password,
                onValueChange = onPasswordChange,
                placeholder = "Tạo mật khẩu",
                isPassword = true
            )

            WisebiteInputField(
                label = "Xác nhận mật khẩu",
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = "Xác nhận mật khẩu của bạn",
                isPassword = true
            )

            WisebiteInputField(
                label = "Địa chỉ (Tùy chọn)",
                value = uiState.address,
                onValueChange = onAddressChange,
                placeholder = "Nhập địa chỉ của bạn"
            )
        }

        // Display general error messages from the ViewModel
        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }


        Spacer(modifier = Modifier.height(32.dp))

        // Signup Button
        Button(
            onClick = onSignupClick,
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
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

        // Login Navigation
        LoginNavigation(
            onNavigateToLogin = onNavigateToLogin,
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
        // App Logo - Fork and knife icon
        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = "WiseBite Logo",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tham gia WiseBite hôm nay!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Giúp giảm lãng phí thực phẩm và tiết kiệm tiền",
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
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Preview now calls the stateless content composable with dummy data
@Preview(showBackground = true, backgroundColor = 0xFFFFFBF0)
@Composable
fun SignupScreenPreview() {
    WisebiteTheme {
        SignupContent(
            uiState = SignupUiState(), // Default empty state
            onFullNameChange = {},
            onEmailChange = {},
            onPhoneNumberChange = {},
            onCountryChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onAddressChange = {},
            onSignupClick = {},
            onNavigateToLogin = {}
        )
    }
}