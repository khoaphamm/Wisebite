package com.example.wisebite.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.R
import com.example.wisebite.ui.component.TitleRender
import com.example.wisebite.ui.component.WisebiteTextField
import com.example.wisebite.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoginSuccessful by viewModel.isLoginSuccessful.collectAsState()
    
    // Handle login success
    LaunchedEffect(isLoginSuccessful) {
        if (isLoginSuccessful) {
            onLoginSuccess()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        TitleRender("Login", MaterialTheme.colorScheme.primary, Modifier.padding(bottom = 12.dp))
        
        LoginHeader()
        
        Spacer(modifier = Modifier.height(20.dp))
        
        LoginForm(
            phoneNumber = uiState.phoneNumber,
            password = uiState.password,
            isPasswordVisible = uiState.isPasswordVisible,
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            onPhoneNumberChange = viewModel::updatePhoneNumber,
            onPasswordChange = viewModel::updatePassword,
            onPasswordVisibilityToggle = viewModel::togglePasswordVisibility,
            onLoginClick = viewModel::login,
            onErrorDismiss = viewModel::clearErrorMessage
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        OrDivider()
        
        Spacer(modifier = Modifier.height(14.dp))
        
        SocialLoginButtons()
        
        Spacer(modifier = Modifier.weight(1f))
        
        SignUpNavigation(
            onNavigateToSignup = onNavigateToSignup,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}

@Composable
fun LoginHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, 
        modifier = Modifier.fillMaxWidth(0.85f)
    ) {
        // You can replace this with your app logo
        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = "WiseBite Logo",
            modifier = Modifier.size(90.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Welcome to WiseBite!\nReduce food waste, save money",
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun LoginForm(
    phoneNumber: String,
    password: String,
    isPasswordVisible: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onPhoneNumberChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onLoginClick: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WisebiteTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = "Phone Number",
            keyboardType = KeyboardType.Phone
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        WisebiteTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            isPassword = true
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = "Forgot Password?",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { /* TODO: Implement forgot password */ }
                .padding(vertical = 4.dp, horizontal = 4.dp)
        )
        
        // Show error message if any
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(18.dp))
        
        // Login Button
        Button(
            onClick = onLoginClick,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isLoading) {
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
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Login",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OrDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            thickness = 1.dp,
            modifier = Modifier.weight(1f)
        )
        Text(
            "Or",
            color = MaterialTheme.colorScheme.outline,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            thickness = 1.dp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SocialLoginButtons() {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        SocialLoginButton(
            text = "Google",
            onClick = { /* TODO: Implement Google login */ }
        )
        Spacer(modifier = Modifier.width(18.dp))
        SocialLoginButton(
            text = "Facebook",
            onClick = { /* TODO: Implement Facebook login */ }
        )
    }
}

@Composable
fun SocialLoginButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.width(120.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SignUpNavigation(
    onNavigateToSignup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Text(
            "Not a member?",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            "Sign up",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onNavigateToSignup() }
        )
    }
}