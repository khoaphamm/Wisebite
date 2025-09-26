package com.example.wisebite.ui.screen

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.data.repository.ImageUploadRepository
import com.example.wisebite.ui.component.ImagePicker
import com.example.wisebite.ui.component.ImagePickerShape
import com.example.wisebite.ui.component.ImagePickerSize
import com.example.wisebite.ui.component.SimpleHeader
import com.example.wisebite.ui.theme.Green500
import kotlinx.coroutines.launch

@Composable
fun ImageUploadTestScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imageUploadRepository = remember { ImageUploadRepository.getInstance(context) }
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SimpleHeader(
            title = "Test Image Upload"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Text(
                text = "Test Avatar Upload",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Avatar upload test
            ImagePicker(
                currentImageUrl = uploadedImageUrl,
                onImageSelected = { uri ->
                    selectedImageUri = uri
                    scope.launch {
                        isUploading = true
                        message = null
                        
                        imageUploadRepository.uploadAvatar(uri)
                            .onSuccess { response ->
                                uploadedImageUrl = response.imageUrl
                                message = "✅ Upload successful!"
                                isUploading = false
                            }
                            .onFailure { error ->
                                message = "❌ Upload failed: ${error.message}"
                                isUploading = false
                            }
                    }
                },
                shape = ImagePickerShape.Circle,
                size = ImagePickerSize.Large,
                isLoading = isUploading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Show status message
            message?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.contains("✅")) {
                            Green500.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(16.dp),
                        color = if (msg.contains("✅")) {
                            Green500
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Upload info
            Text(
                text = "Instructions:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = """
                    1. Tap on the image picker above
                    2. Select an image from your gallery
                    3. The image will be uploaded to Cloudinary
                    4. You'll see the uploaded image URL in the response
                    5. The image picker will show the uploaded image
                """.trimIndent(),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (uploadedImageUrl != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Uploaded Image URL:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = uploadedImageUrl ?: "",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}