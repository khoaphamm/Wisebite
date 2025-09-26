package com.example.wisebite.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wisebite.R
import com.example.wisebite.data.repository.AuthRepository
import com.example.wisebite.data.repository.ImageUploadRepository
import com.example.wisebite.ui.component.ImagePicker
import com.example.wisebite.ui.component.ImagePickerShape
import com.example.wisebite.ui.component.ImagePickerSize
import com.example.wisebite.ui.component.WisebiteInputField
import com.example.wisebite.ui.theme.*
import com.example.wisebite.ui.viewmodel.EditProfileViewModel
import com.example.wisebite.ui.viewmodel.EditProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val authRepository = AuthRepository.getInstance(context)
    val viewModel: EditProfileViewModel = viewModel(
        factory = EditProfileViewModelFactory(authRepository, context)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle successful update
    LaunchedEffect(uiState.isUpdateSuccessful) {
        if (uiState.isUpdateSuccessful) {
            onNavigateBack()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Custom Header with back button
        TopAppBar(
            title = { 
                Text(
                    "Chỉnh sửa hồ sơ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Profile Picture Section with ImagePicker
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ImagePicker(
                    currentImageUrl = uiState.user?.avatarUrl,
                    onImageSelected = { uri -> viewModel.uploadAvatar(uri) },
                    shape = ImagePickerShape.Circle,
                    size = ImagePickerSize.Large,
                    showAddIcon = true,
                    isLoading = uiState.isUploadingAvatar,
                    defaultImageRes = R.drawable.man,
                    modifier = Modifier.size(120.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (uiState.isUploadingAvatar) "Đang tải ảnh lên..." 
                           else "Chạm để thay đổi ảnh đại diện",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                
                // Show upload success message
                uiState.uploadSuccess?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Form Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WisebiteInputField(
                    label = "Họ và tên",
                    value = uiState.fullName,
                    onValueChange = viewModel::updateFullName,
                    placeholder = "Nhập họ và tên của bạn"
                )
                
                WisebiteInputField(
                    label = "Email",
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    placeholder = "Nhập địa chỉ email của bạn",
                    keyboardType = KeyboardType.Email
                )
                
                WisebiteInputField(
                    label = "Số điện thoại",
                    value = uiState.phoneNumber,
                    onValueChange = viewModel::updatePhoneNumber,
                    placeholder = "Nhập số điện thoại của bạn",
                    keyboardType = KeyboardType.Phone
                )
            }
            
            // Show error message if any
            uiState.errorMessage?.let { errorMessage ->
                Spacer(modifier = Modifier.height(16.dp))
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Save Button
            Button(
                onClick = viewModel::updateProfile,
                enabled = !uiState.isLoading && viewModel.hasChanges(),
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
                        text = "Lưu thay đổi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
