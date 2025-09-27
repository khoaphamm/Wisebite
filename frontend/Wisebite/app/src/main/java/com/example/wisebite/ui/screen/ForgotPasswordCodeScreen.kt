package com.example.wisebite.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wisebite.ui.viewmodel.ForgotPasswordViewModel
import com.example.wisebite.util.ViewModelFactory
import com.example.wisebite.ui.viewmodel.ForgotPasswordStep
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordCodeScreen(
    email: String,
    onNavigateToNewPassword: (String, String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var resetCode by remember { mutableStateOf("") }
    var timeRemaining by remember { mutableStateOf(900) } // 15 minutes
    var canResend by remember { mutableStateOf(false) }

    // Countdown timer
    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        } else {
            canResend = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xác thực mã") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Icon
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // Title and description
            Text(
                text = "Nhập mã xác thực",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Chúng tôi đã gửi mã 6 số đến:",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Text(
                text = email,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Code input
            OutlinedTextField(
                value = resetCode,
                onValueChange = { newCode ->
                    if (newCode.length <= 6 && newCode.all { it.isDigit() }) {
                        resetCode = newCode
                    }
                },
                label = { Text("Mã xác thực") },
                placeholder = { Text("123456") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                isError = uiState.error != null,
                supportingText = {
                    Column {
                        if (uiState.error != null) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (timeRemaining > 0) {
                            val minutes = timeRemaining / 60
                            val seconds = timeRemaining % 60
                            Text(
                                text = "Mã hết hạn sau: ${minutes}:${seconds.toString().padStart(2, '0')}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Mã đã hết hạn",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                enabled = !uiState.isLoading && timeRemaining > 0,
                singleLine = true
            )

            // Resend code button
            if (canResend || timeRemaining <= 0) {
                TextButton(
                    onClick = {
                        viewModel.resendCode(email)
                        timeRemaining = 900
                        canResend = false
                        resetCode = ""
                    },
                    enabled = !uiState.isLoading
                ) {
                    Text("Gửi lại mã")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Verify button
            Button(
                onClick = {
                    viewModel.verifyResetCode(email, resetCode)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading && resetCode.length == 6 && timeRemaining > 0,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Xác thực mã",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Handle success navigation
    LaunchedEffect(uiState.step) {
        if (uiState.step == ForgotPasswordStep.NEW_PASSWORD) {
            onNavigateToNewPassword(email, resetCode)
        }
    }
}