package com.example.wisebite.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
                        onClick = { onNavigateToBagDetails(bag.id) }
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
            // Fallback store card if no data loaded
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onNavigateToSurpriseBagList() },
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
                            text = "Cơm Tấm Sài Gòn",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "4.5",
                                fontSize = 14.sp,
                                color = WarmGrey700
                            )
                        }
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
                            text = "45 Lê Lợi, Quận 1",
                            fontSize = 13.sp,
                            color = WarmGrey600
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Cơm tấm sườn nướng, bì, chả trứng đặc biệt. Cơm đéo, sườn thơm ngon...",
                        fontSize = 13.sp,
                        color = WarmGrey700,
                        lineHeight = 18.sp
                    )
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
            .width(200.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Store name and category
            Text(
                text = bag.store?.name ?: "Unknown Store",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = bag.categoryDisplayName,
                fontSize = 12.sp,
                color = Orange600,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Bag name
            Text(
                text = bag.name,
                fontSize = 13.sp,
                color = WarmGrey700,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Price
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bag.formattedDiscountedPrice,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green600
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = bag.formattedOriginalPrice,
                    fontSize = 11.sp,
                    color = WarmGrey500,
                    style = androidx.compose.ui.text.TextStyle(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Availability and pickup time
            Text(
                text = bag.quantityDisplay,
                fontSize = 11.sp,
                color = if (bag.quantityAvailable > 0) WarmGrey600 else Red500
            )
            
            Text(
                text = bag.pickupTimeDisplay,
                fontSize = 11.sp,
                color = WarmGrey600
            )
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
                
                store.rating?.let { rating ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = store.displayRating,
                            fontSize = 14.sp,
                            color = WarmGrey700
                        )
                    }
                }
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