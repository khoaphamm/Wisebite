package com.example.wisebite.ui.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import com.example.wisebite.ui.components.SmallAsyncImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wisebite.data.model.Store
import com.example.wisebite.data.model.SurpriseBag
import com.example.wisebite.data.repository.SurpriseBagRepository
import com.example.wisebite.ui.component.WisebiteHeader
import com.example.wisebite.ui.theme.*
import com.example.wisebite.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    onNavigateToBagDetails: (String) -> Unit = {},
    onNavigateToOrderDebug: () -> Unit = {},
    onNavigateToSurpriseBagList: () -> Unit = {},
    onNavigateToStoreBags: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val repository = SurpriseBagRepository.getInstance(context)
    val viewModel: HomeViewModel = viewModel { HomeViewModel(repository) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Show error dialog
    uiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearErrorMessage() },
            title = { Text("Lỗi") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearErrorMessage() }) {
                    Text("OK")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp)
    ) {
        // Consistent header
        WisebiteHeader(
            title = "Chào mừng đến với WiseBite!",
            subtitle = "Save the food up 🌱 • ${uiState.selectedCity}",
            showWiseBiteLogo = true
        )
        
        // Temporary debug button for development
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Orange100),
            onClick = onNavigateToOrderDebug
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🐛 Debug Order System",
                    fontSize = 14.sp,
                    color = Orange600,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "DEV",
                    fontSize = 10.sp,
                    color = Orange600,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Promotional banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { onNavigateToSurpriseBagList() },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF2E7D32).copy(alpha = 0.8f),
                                Color(0xFF4CAF50).copy(alpha = 0.8f)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Giảm lãng phí thực phẩm",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Cứu lấy thực phẩm ngon với giá ưu đãi",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Xem tất cả →",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Placeholder for food images
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Food",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
        
        // Category filters
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.categories) { category ->
                val isSelected = category == uiState.selectedCategory
                
                FilterChip(
                    onClick = { viewModel.selectCategory(category) },
                    label = {
                        Text(
                            text = category,
                            fontSize = 14.sp,
                            color = if (isSelected) Color.White else WarmGrey700
                        )
                    },
                    selected = isSelected,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Green500,
                        selectedLabelColor = Color.White,
                        containerColor = WarmGrey200,
                        labelColor = WarmGrey700
                    )
                )
            }
        }
        
        // Featured surprise bags section
        if (uiState.featuredSurpriseBags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Surprise Bags nổi bật",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = "Xem tất cả",
                    fontSize = 14.sp,
                    color = Green500,
                    modifier = Modifier.clickable { onNavigateToSurpriseBagList() }
                )
            }
            
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.featuredSurpriseBags) { bag ->
                    FeaturedSurpriseBagCard(
                        bag = bag,
                        onClick = {
                            if (!bag.id.isNullOrBlank()) {
                                onNavigateToBagDetails(bag.id)
                            } else {
                                // Optional: Log an error or show a toast to the user
                                Log.e("HomeScreen", "Attempted to navigate with a null or blank bag ID.")
                            }
                        }
                    )
                }
            }
        }
        
        // Section header for stores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Đối tác nổi bật",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Text(
                text = "Xem tất cả",
                fontSize = 14.sp,
                color = Green500,
                modifier = Modifier.clickable { /* TODO: Navigate to all stores */ }
            )
        }
        
        // Store cards
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green500)
            }
        } else if (uiState.stores.isNotEmpty()) {
            uiState.stores.take(3).forEach { store ->
                StoreCard(
                    store = store,
                    onClick = { onNavigateToStoreBags(store.id) }
                )
            }
        } else {
            // Show loading error or retry option
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { viewModel.refreshData() },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (uiState.errorMessage != null) {
                            "Lỗi tải dữ liệu: ${uiState.errorMessage}"
                        } else {
                            "Không có cửa hàng nào. Nhấn để thử lại."
                        },
                        fontSize = 14.sp,
                        color = if (uiState.errorMessage != null) Red500 else WarmGrey700,
                        textAlign = TextAlign.Center
                    )
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nhấn để thử lại",
                            fontSize = 12.sp,
                            color = Green500
                        )
                    }
                }
            }
        }
        
        // Add some bottom spacing
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun FeaturedSurpriseBagCard(
    bag: SurpriseBag,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() }
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Hero Image Section with real images
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                SmallAsyncImage(
                    imageUrl = bag.imageUrl,
                    contentDescription = bag.name,
                    size = 120.dp,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Discount badge in top right
                if (bag.discountPercentage > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                Color(0xFFFF6B35),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "-${bag.formattedDiscountPercentage}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Store logo circle in bottom left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(
                            Color.White,
                            CircleShape
                        )
                        .shadow(2.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // Content Section
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Store name with enhanced styling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bag.store?.name ?: "Unknown Store",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Store address with location icon
                        bag.store?.address?.let { address ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = WarmGrey500,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = address,
                                    fontSize = 9.sp,
                                    color = WarmGrey500,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Category badge
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .background(
                            Orange600.copy(alpha = 0.1f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = bag.categoryDisplayName,
                        fontSize = 10.sp,
                        color = Orange600,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bag name with better typography
                Text(
                    text = bag.name,
                    fontSize = 13.sp,
                    color = WarmGrey800,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Enhanced price section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bag.formattedDiscountedPrice,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = bag.formattedOriginalPrice,
                                fontSize = 10.sp,
                                color = WarmGrey500,
                                style = androidx.compose.ui.text.TextStyle(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            )
                        }
                        
                        // Savings indicator
                        if (bag.discountPercentage > 0) {
                            val savingsAmount = bag.originalValue - bag.discountedPrice
                            Text(
                                text = "Tiết kiệm ${String.format("%,.0f", savingsAmount)}đ",
                                fontSize = 9.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Availability indicator with icon
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = WarmGrey500,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = bag.pickupTimeDisplay,
                                fontSize = 9.sp,
                                color = WarmGrey600
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = bag.quantityDisplay,
                            fontSize = 10.sp,
                            color = if (bag.quantityAvailable > 0) Color(0xFF2E7D32) else Red500,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoreCard(
    store: Store,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = store.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = WarmGrey600,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = store.displayAddress,
                    fontSize = 13.sp,
                    color = WarmGrey600
                )
            }
            
            store.description?.let { description ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = WarmGrey700,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Helper function to determine if a surprise bag is eco-friendly
 * For demo purposes, all bags are now eco-friendly
 */
