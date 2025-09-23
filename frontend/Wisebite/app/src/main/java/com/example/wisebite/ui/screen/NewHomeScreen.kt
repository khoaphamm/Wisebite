package com.example.wisebite.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.ui.theme.*

@Composable
fun NewHomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header Section
        HeaderSection()
        
        // Location Section
        LocationSection()
        
        // Hero Card Section
        HeroCardSection()
        
        // Food Categories
        FoodCategoriesSection()
        
        // Featured Section
        FeaturedSection()
        
        // Restaurant Card
        RestaurantCard()
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // WiseBite logo/text
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🍽️",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "WiseBite",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Green500
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Chào mừng đến với WiseBite!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = WarmGrey800
        )
        
        Text(
            text = "Save the food up 🥬",
            fontSize = 14.sp,
            color = WarmGrey600
        )
    }
}

@Composable
private fun LocationSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            tint = WarmGrey600,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "TP. Hồ Chí Minh",
            fontSize = 14.sp,
            color = WarmGrey600
        )
    }
}

@Composable
private fun HeroCardSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D5016) // Dark green background
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background content - you can add images here
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Giảm lãng phí thực phẩm",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cứu lấy thực phẩm ngon với giá ưu đãi",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun FoodCategoriesSection() {
    val categories = listOf(
        CategoryItem("Tất cả", true),
        CategoryItem("Bánh mì", false),
        CategoryItem("Cơm", false),
        CategoryItem("Phở", false),
        CategoryItem("Bún", false)
    )
    
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryChip(category)
        }
    }
}

@Composable
private fun CategoryChip(category: CategoryItem) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (category.isSelected) Green500 else WarmGrey200,
        modifier = Modifier.clickable { }
    ) {
        Text(
            text = category.name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (category.isSelected) Color.White else WarmGrey700,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun FeaturedSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Đối tác nổi bật",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = WarmGrey900
        )
        Text(
            text = "Xem tất cả",
            fontSize = 14.sp,
            color = Green500,
            modifier = Modifier.clickable { }
        )
    }
}

@Composable
private fun RestaurantCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Restaurant image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(WarmGrey200),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🍚🥗🍜",
                    fontSize = 32.sp
                )
            }
            
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Cơm Tấm Sài Gòn",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGrey900
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
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "4.5",
                        fontSize = 14.sp,
                        color = WarmGrey700
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
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
                        fontSize = 12.sp,
                        color = WarmGrey600
                    )
                }
                
                Text(
                    text = "Cơm tấm sườn nướng, bì, chả trứng đặc biệt. Cơm dẻo, sướn thơm ngon, nước mắm...",
                    fontSize = 12.sp,
                    color = WarmGrey700,
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 2
                )
            }
        }
    }
}

data class CategoryItem(
    val name: String,
    val isSelected: Boolean
)