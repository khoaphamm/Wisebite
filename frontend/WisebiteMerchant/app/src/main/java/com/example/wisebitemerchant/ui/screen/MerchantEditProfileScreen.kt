package com.example.wisebitemerchant.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wisebitemerchant.ui.theme.*
import com.example.wisebitemerchant.ui.viewmodel.MerchantEditProfileViewModel
import com.example.wisebitemerchant.util.ViewModelFactory
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantEditProfileScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToImagePicker: (isStoreImage: Boolean) -> Unit = { _ -> }
) {
    val context = LocalContext.current
    val viewModel: MerchantEditProfileViewModel = viewModel(
        factory = ViewModelFactory.getInstance(context)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle success message
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            delay(2000)
            viewModel.clearMessages()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chỉnh sửa hồ sơ",
                        fontWeight = FontWeight.Bold
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
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveProfile(onSuccess = {
                                // Profile saved successfully
                            })
                        },
                        enabled = !uiState.isSaving && viewModel.hasChanges()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Lưu",
                                fontWeight = FontWeight.Medium,
                                color = if (viewModel.hasChanges()) Orange500 else Color.Gray
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Orange500
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Error Message
                    if (uiState.errorMessage != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Red100),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage!!,
                                modifier = Modifier.padding(16.dp),
                                color = Red700,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    // Success Message
                    if (uiState.successMessage != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Green100),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = uiState.successMessage!!,
                                modifier = Modifier.padding(16.dp),
                                color = Green600,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    // Personal Information Section
                    PersonalInfoSection(
                        uiState = uiState,
                        onToggleExpansion = viewModel::togglePersonalInfoExpansion,
                        onFullNameChange = viewModel::updateFullName,
                        onEmailChange = viewModel::updateEmail,
                        onPhoneChange = viewModel::updatePhoneNumber,
                        onImageClick = { onNavigateToImagePicker(false) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Store Information Section
                    StoreInfoSection(
                        uiState = uiState,
                        onToggleExpansion = viewModel::toggleStoreInfoExpansion,
                        onStoreNameChange = viewModel::updateStoreName,
                        onDescriptionChange = viewModel::updateStoreDescription,
                        onAddressChange = viewModel::updateStoreAddress,
                        onImageClick = { onNavigateToImagePicker(true) }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Save Button (Full Width)
                    Button(
                        onClick = {
                            viewModel.saveProfile(onSuccess = {
                                // Profile saved successfully
                            })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isSaving && viewModel.hasChanges(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Orange500,
                            disabledContainerColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Lưu thay đổi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PersonalInfoSection(
    uiState: com.example.wisebitemerchant.ui.viewmodel.MerchantEditProfileUiState,
    onToggleExpansion: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onImageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Orange50),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpansion() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Personal Info",
                    tint = Orange600,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Thông tin cá nhân",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Orange800,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (uiState.isPersonalInfoExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = Orange600
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Profile Image
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Orange200)
                        .clickable { onImageClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.profileImageUrl.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Orange600,
                            modifier = Modifier.size(40.dp)
                        )
                    } else {
                        // TODO: Load image from URL
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Orange600,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                // Camera Icon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Orange500)
                        .clickable { onImageClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Change Photo",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Expandable Content
            AnimatedVisibility(
                visible = uiState.isPersonalInfoExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    // Full Name Field
                    OutlinedTextField(
                        value = uiState.fullName,
                        onValueChange = onFullNameChange,
                        label = { Text("Họ và tên *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Orange500,
                            focusedLabelColor = Orange500
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Email Field
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        label = { Text("Email *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Orange500,
                            focusedLabelColor = Orange500
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Phone Number Field
                    OutlinedTextField(
                        value = uiState.phoneNumber,
                        onValueChange = onPhoneChange,
                        label = { Text("Số điện thoại *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Orange500,
                            focusedLabelColor = Orange500
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StoreInfoSection(
    uiState: com.example.wisebitemerchant.ui.viewmodel.MerchantEditProfileUiState,
    onToggleExpansion: () -> Unit,
    onStoreNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onImageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Blue100),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpansion() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = "Store Info",
                    tint = Blue600,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Thông tin cửa hàng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blue800,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (uiState.isStoreInfoExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = Blue600
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Store Image
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Blue200)
                        .clickable { onImageClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.storeImageUrl.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = "Store",
                            tint = Blue600,
                            modifier = Modifier.size(40.dp)
                        )
                    } else {
                        // TODO: Load image from URL
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = "Store",
                            tint = Blue600,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                // Camera Icon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Blue500)
                        .clickable { onImageClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Change Photo",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Expandable Content
            AnimatedVisibility(
                visible = uiState.isStoreInfoExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    // Store Name Field
                    OutlinedTextField(
                        value = uiState.storeName,
                        onValueChange = onStoreNameChange,
                        label = { Text("Tên cửa hàng *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            focusedLabelColor = Blue500
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Store Description Field
                    OutlinedTextField(
                        value = uiState.storeDescription,
                        onValueChange = onDescriptionChange,
                        label = { Text("Mô tả cửa hàng") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            focusedLabelColor = Blue500
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Store Address Field
                    OutlinedTextField(
                        value = uiState.storeAddress,
                        onValueChange = onAddressChange,
                        label = { Text("Địa chỉ cửa hàng *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            focusedLabelColor = Blue500
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MerchantEditProfileScreenPreview() {
    WisebiteMerchantTheme {
        MerchantEditProfileScreen()
    }
}