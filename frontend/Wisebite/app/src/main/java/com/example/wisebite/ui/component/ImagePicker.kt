package com.example.wisebite.ui.component

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.wisebite.R
import com.example.wisebite.ui.theme.Green500

@Composable
fun ImagePicker(
    currentImageUrl: String? = null,
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    shape: ImagePickerShape = ImagePickerShape.Rectangle,
    size: ImagePickerSize = ImagePickerSize.Medium,
    showAddIcon: Boolean = true,
    isLoading: Boolean = false,
    defaultImageRes: Int? = R.drawable.man // Add default image resource
) {
    val context = LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(false) }
    
    // Check camera permission
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            showImageSourceDialog = true
        }
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // You would need to convert bitmap to URI if using camera
        // For now, we'll focus on gallery picker
    }
    
    val containerModifier = when (shape) {
        ImagePickerShape.Circle -> modifier
            .size(size.dp)
            .clip(CircleShape)
        ImagePickerShape.Rectangle -> modifier
            .size(width = size.width, height = size.height)
            .clip(RoundedCornerShape(12.dp))
    }
    
    Box(
        modifier = containerModifier
            .background(Color.Gray.copy(alpha = 0.1f))
            .clickable {
                if (hasPermission) {
                    showImageSourceDialog = true
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    color = Green500,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            currentImageUrl != null -> {
                // Display the uploaded image using AsyncImage from Coil
                AsyncImage(
                    model = currentImageUrl,
                    contentDescription = "Current image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                if (showAddIcon) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Change image",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            defaultImageRes != null -> {
                // Show default image
                Image(
                    painter = painterResource(id = defaultImageRes),
                    contentDescription = "Default avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                if (showAddIcon) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add image",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Add image",
                        tint = Green500,
                        modifier = Modifier.size(32.dp)
                    )
                    if (size != ImagePickerSize.Small) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Thêm ảnh",
                            fontSize = 12.sp,
                            color = Green500,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
    
    // Image source selection dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = {
                Text("Chọn nguồn ảnh")
            },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = Green500
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Chọn từ thư viện",
                                color = Green500
                            )
                        }
                    }
                    
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            // Camera functionality would go here
                            // For now, we'll just use gallery
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Green500
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Chụp ảnh mới",
                                color = Green500
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

enum class ImagePickerShape {
    Circle, Rectangle
}

enum class ImagePickerSize(val dp: androidx.compose.ui.unit.Dp) {
    Small(80.dp),
    Medium(120.dp),
    Large(160.dp);
    
    val width: androidx.compose.ui.unit.Dp get() = when (this) {
        Small -> 80.dp
        Medium -> 120.dp
        Large -> 200.dp
    }
    
    val height: androidx.compose.ui.unit.Dp get() = when (this) {
        Small -> 60.dp
        Medium -> 90.dp
        Large -> 150.dp
    }
}