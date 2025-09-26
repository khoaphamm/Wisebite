package com.example.wisebite.ui.component

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.ui.theme.*

@Composable
fun ProfileCardWithImageUpload(
    userName: String = "Nguyen Van A",
    userEmail: String = "nguyenvana@example.com",
    avatarUrl: String? = null,
    isUploadingAvatar: Boolean = false,
    onEditClick: () -> Unit = {},
    onAvatarSelected: (Uri) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with image upload
            ImagePicker(
                currentImageUrl = avatarUrl,
                onImageSelected = onAvatarSelected,
                shape = ImagePickerShape.Circle,
                size = ImagePickerSize.Small,
                showAddIcon = avatarUrl == null, // Only show add icon if no avatar
                isLoading = isUploadingAvatar,
                defaultImageRes = com.example.wisebite.R.drawable.man,
                modifier = Modifier.size(60.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = userEmail,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isUploadingAvatar) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Đang tải ảnh lên...",
                        fontSize = 12.sp,
                        color = Green500
                    )
                }
            }
            
            IconButton(
                onClick = onEditClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Green100
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = Green500
                )
            }
        }
    }
}