package com.example.wisebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.ui.component.CountryCode
import com.example.wisebite.ui.component.PhoneNumberInput
import com.example.wisebite.ui.component.TitleRender
import com.example.wisebite.ui.component.WisebiteTextField
import com.example.wisebite.ui.viewmodel.SignupViewModel

@Composable
fun SignupScreen(
    viewModel: SignupViewModel,
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSignupSuccessful by viewModel.isSignupSuccessful.collectAsState()
    
    // Handle signup success
    LaunchedEffect(isSignupSuccessful) {
        if (isSignupSuccessful) {
            onSignupSuccess()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 0.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TitleRender(
            title = "Sign Up",
            backgroundColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // App Header
        SignupHeader()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Form fields
        SignupForm(
            uiState = uiState,
            onFullNameChange = viewModel::updateFullName,
            onEmailChange = viewModel::updateEmail,
            onPhoneNumberChange = viewModel::updatePhoneNumber,
            onCountryChange = viewModel::updateCountry,
            onPasswordChange = viewModel::updatePassword,
            onConfirmPasswordChange = viewModel::updateConfirmPassword,
            onAddressChange = viewModel::updateAddress,
            onPasswordVisibilityToggle = viewModel::togglePasswordVisibility,
            onConfirmPasswordVisibilityToggle = viewModel::toggleConfirmPasswordVisibility,
            onSignupClick = viewModel::signup,
            onErrorDismiss = viewModel::clearErrorMessage
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Login navigation
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
        // App logo/icon
        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = "WiseBite Logo",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Join WiseBite today!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Help reduce food waste while saving money",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun SignupForm(
    uiState: com.example.wisebite.data.model.SignupUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onCountryChange: (CountryCode) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onConfirmPasswordVisibilityToggle: () -> Unit,
    onSignupClick: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Full Name
        Text("Full Name", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
        WisebiteTextField(
            label = "Enter your full name",
            value = uiState.fullName,
            onValueChange = onFullNameChange,
            errorMessage = uiState.fullNameError,
            keyboardType = KeyboardType.Text
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Email
        Text("Email Address", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
        WisebiteTextField(
            label = "Enter your email",
            value = uiState.email,
            onValueChange = onEmailChange,
            errorMessage = uiState.emailError,
            keyboardType = KeyboardType.Email
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Phone Number
        Text("Phone Number", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
        PhoneNumberInput(
            label = "Enter your phone number",
            phoneNumber = uiState.phoneNumber,
            onPhoneNumberChange = onPhoneNumberChange,
            selectedCountry = uiState.selectedCountry,
            onCountryChange = onCountryChange,
            errorMessage = uiState.phoneNumberError
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Password
        Text("Password", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
        WisebiteTextField(
            label = "Create a password",
            value = uiState.password,
            onValueChange = onPasswordChange,
            errorMessage = uiState.passwordError,
            isPassword = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Confirm Password
        Text("Confirm Password", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
        WisebiteTextField(
            label = "Confirm your password",
            value = uiState.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            errorMessage = uiState.confirmPasswordError,
            isPassword = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Address (Optional)
        Text("Address (Optional)", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
        WisebiteTextField(
            label = "Enter your address",
            value = uiState.address,
            onValueChange = onAddressChange,
            keyboardType = KeyboardType.Text
        )
        
        // Show error message if any
        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onErrorDismiss() }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Signup Button
        Button(
            onClick = onSignupClick,
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (uiState.isLoading) {
                            Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        } else {
                            Modifier.background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Sign Up",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Terms and conditions text
        Text(
            text = "By signing up, you agree to our Terms of Service and Privacy Policy",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun LoginNavigation(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onNavigateToLogin,
        modifier = modifier
    ) {
        Row {
            Text(
                "Already have an account? ",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Text(
                "Log In",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}