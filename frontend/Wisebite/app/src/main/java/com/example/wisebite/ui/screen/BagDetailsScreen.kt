package com.example.wisebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.AccessTime
import com.example.wisebite.ui.components.ProfessionalAsyncImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import com.example.wisebite.data.model.CreateOrderItemRequest
import com.example.wisebite.data.model.CreateOrderRequest
import com.example.wisebite.data.model.SurpriseBag
import com.example.wisebite.data.repository.ApiResult
import com.example.wisebite.data.repository.SurpriseBagRepository
import com.example.wisebite.ui.theme.*
import com.example.wisebite.ui.component.TimePickerDialog
import com.example.wisebite.ui.viewmodel.OrderViewModel
import com.example.wisebite.util.PickupTimeValidator
import com.example.wisebite.util.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePlaceholder(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.ImageSearch,
    backgroundColor: Color = Color(0xFFF5F5F5),
    iconColor: Color = Color(0xFFBDBDBD),
    cornerRadius: Int = 12
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .clip(RoundedCornerShape(cornerRadius.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Image placeholder",
            modifier = Modifier.size(48.dp),
            tint = iconColor
        )
    }
}

@Composable
fun ProfessionalBadge(
    text: String,
    backgroundColor: Color = Red500.copy(alpha = 0.1f),
    textColor: Color = Red500,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .background(
                backgroundColor,
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = textColor
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    text: String,
    iconColor: Color = WarmGrey600,
    textColor: Color = WarmGrey700
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = textColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BagDetailsScreen(
    bagId: String,
    onBackClick: () -> Unit = {},
    onOrderSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val orderViewModel: OrderViewModel = viewModel(
        factory = ViewModelFactory.getInstance(context)
    )
    val surpriseBagRepository = SurpriseBagRepository.getInstance(context)
    
    val orderUiState by orderViewModel.uiState.collectAsState()
    
    // Local state for surprise bag details and order configuration
    var surpriseBag by remember { mutableStateOf<SurpriseBag?>(null) }
    var isLoadingBag by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf(1) }
    var selectedPickupTime by remember { mutableStateOf("") }
    var selectedPickupTimeFormatted by remember { mutableStateOf<Date?>(null) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var pickupTimeError by remember { mutableStateOf<String?>(null) }
    var deliveryAddress by remember { mutableStateOf("") }
    var orderNotes by remember { mutableStateOf("") }
    
    // Load surprise bag details
    LaunchedEffect(bagId) {
        isLoadingBag = true
        errorMessage = null
        try {
            val result = surpriseBagRepository.getSurpriseBagDetails(bagId)
            
            // Only update state if the coroutine is still active (not cancelled)
            if (isActive) {
                when (result) {
                    is ApiResult.Success -> {
                        // Enhance bag with image if needed
                        val enhancedBag = enhanceBagWithImage(result.data)
                        surpriseBag = enhancedBag
                        selectedPickupTime = enhancedBag.pickupTimeDisplay
                        deliveryAddress = enhancedBag.store?.displayAddress ?: ""
                        
                        // DEMO MODE: Set a default pickup time for easier testing
                        // Set it to the current time + 1 hour for demo purposes
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.HOUR_OF_DAY, 1)
                        selectedPickupTimeFormatted = calendar.time
                        selectedPickupTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
                        
                        isLoadingBag = false
                    }
                    is ApiResult.Error -> {
                        errorMessage = "Lỗi tải dữ liệu: ${result.message}"
                        isLoadingBag = false
                    }
                    else -> {
                        errorMessage = "Không thể tải dữ liệu surprise bag"
                        isLoadingBag = false
                    }
                }
            }
        } catch (e: Exception) {
            // Only update error state if coroutine is still active
            if (isActive && e !is CancellationException) {
                errorMessage = "Lỗi không xác định: ${e.message}"
                isLoadingBag = false
            }
        }
    }
    
    // Show loading dialog when creating order
    if (orderUiState.isCreatingOrder) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Đang tạo đơn hàng") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Green500
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Vui lòng đợi trong khi chúng tôi xử lý đơn hàng của bạn...")
                }
            },
            confirmButton = { }
        )
    }

    // Show error dialog if order creation fails
    orderUiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { orderViewModel.clearErrorMessage() },
            title = { Text("Đặt hàng thất bại") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { orderViewModel.clearErrorMessage() }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Show general error dialog
    errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Lỗi") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    // Monitor order creation success - only trigger when user explicitly creates an order
    var hasTriggeredOrderCreation by remember { mutableStateOf(false) }
    
    // Monitor order creation state changes
    LaunchedEffect(orderUiState.isCreatingOrder, orderUiState.errorMessage) {
        // If we previously triggered order creation and now it's complete with no error
        if (hasTriggeredOrderCreation && !orderUiState.isCreatingOrder && orderUiState.errorMessage == null) {
            // Small delay to ensure UI state is stable before navigating
            delay(500)
            onOrderSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Chi tiết Surprise Bag",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Green700,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Green700
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Content
        when {
            isLoadingBag -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Green500)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Đang tải dữ liệu...",
                            fontSize = 14.sp,
                            color = WarmGrey600
                        )
                    }
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "❌ Có lỗi xảy ra",
                            fontSize = 18.sp,
                            color = Red500,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Lỗi không xác định",
                            fontSize = 14.sp,
                            color = WarmGrey600,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            surpriseBag == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không tìm thấy Surprise Bag",
                        fontSize = 16.sp,
                        color = WarmGrey600
                    )
                }
            }
            else -> {
                val bag = surpriseBag!! // Safe to use !! here since we checked for null above
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Professional Surprise Bag Info Card with Visual Elements
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = Color.Black.copy(alpha = 0.1f)
                            ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column {
                            // Hero Image Section with Overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                ) {
                                    // Real Image with fallback
                                    ProfessionalAsyncImage(
                                        imageUrl = bag.imageUrl,
                                        contentDescription = bag.name,
                                        height = 200.dp
                                    )                                // Top-right discount badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(16.dp)
                                ) {
                                    ProfessionalBadge(
                                        text = "-${bag.formattedDiscountPercentage}",
                                        backgroundColor = Red500,
                                        textColor = Color.White,
                                        icon = Icons.Default.LocalOffer
                                    )
                                }
                                
                                // Bottom-left store info overlay
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Store logo placeholder
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(
                                                    Color.White,
                                                    CircleShape
                                                )
                                                .padding(8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Storefront,
                                                contentDescription = "Store logo",
                                                modifier = Modifier.size(24.dp),
                                                tint = Green600
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(12.dp))
                                        
                                        Column {
                                            Text(
                                                text = bag.store?.name ?: "Unknown Store",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                style = androidx.compose.ui.text.TextStyle(
                                                    shadow = androidx.compose.ui.graphics.Shadow(
                                                        color = Color.Black.copy(alpha = 0.5f),
                                                        offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                                        blurRadius = 2f
                                                    )
                                                )
                                            )
                                            ProfessionalBadge(
                                                text = bag.categoryDisplayName,
                                                backgroundColor = Orange600.copy(alpha = 0.9f),
                                                textColor = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Content Section
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                // Bag Title and Description
                                Text(
                                    text = bag.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    lineHeight = 26.sp
                                )
                                
                                bag.description?.let { desc ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = desc,
                                        fontSize = 14.sp,
                                        color = WarmGrey600,
                                        lineHeight = 20.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                // Price Section with Enhanced Design
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Green50
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Giá ưu đãi",
                                                fontSize = 12.sp,
                                                color = Green700,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = bag.formattedDiscountedPrice,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Green600
                                            )
                                        }
                                        
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Giá gốc",
                                                fontSize = 12.sp,
                                                color = WarmGrey600
                                            )
                                            Text(
                                                text = bag.formattedOriginalPrice,
                                                fontSize = 16.sp,
                                                color = WarmGrey500,
                                                style = androidx.compose.ui.text.TextStyle(
                                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                                )
                                            )
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            
                                            Text(
                                                text = "Tiết kiệm ${String.format("%,.0f", bag.originalValue - bag.discountedPrice)}đ",
                                                fontSize = 11.sp,
                                                color = Green600,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier
                                                    .background(
                                                        Green100,
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                // Enhanced Info Rows
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFAFBFC)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        InfoRow(
                                            icon = Icons.Default.LocationOn,
                                            text = bag.store?.displayAddress ?: "Địa chỉ không có",
                                            iconColor = Red500
                                        )
                                        
                                        InfoRow(
                                            icon = Icons.Default.AccessTime,
                                            text = "Nhận hàng: ${bag.pickupTimeDisplay}",
                                            iconColor = Orange600
                                        )
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            InfoRow(
                                                icon = Icons.Default.Star,
                                                text = bag.quantityDisplay,
                                                iconColor = if (bag.quantityAvailable > 0) Green600 else Red500,
                                                textColor = if (bag.quantityAvailable > 0) WarmGrey700 else Red500
                                            )
                                            
                                            if (bag.quantityAvailable <= 5 && bag.quantityAvailable > 0) {
                                                ProfessionalBadge(
                                                    text = "Sắp hết!",
                                                    backgroundColor = Orange100,
                                                    textColor = Orange600
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Enhanced Quantity Selector
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Số lượng",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                
                                Text(
                                    text = "Tối đa: ${bag.maxPerCustomer}",
                                    fontSize = 12.sp,
                                    color = WarmGrey600,
                                    modifier = Modifier
                                        .background(
                                            WarmGrey100,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Quantity Selector with Enhanced Design
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF8F9FA)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Decrease Button
                                    Card(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clickable(enabled = quantity > 1) { 
                                                if (quantity > 1) quantity-- 
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (quantity > 1) Color(0xFF4CAF50) else WarmGrey200
                                        ),
                                        shape = CircleShape,
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = if (quantity > 1) 4.dp else 0.dp
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Decrease quantity",
                                                tint = if (quantity > 1) Color.White else WarmGrey400,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    
                                    // Quantity Display with better spacing
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color.White,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 24.dp, vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = quantity.toString(),
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = "túi",
                                                fontSize = 12.sp,
                                                color = WarmGrey600,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    
                                    // Increase Button
                                    val canIncrease = quantity < bag.maxPerCustomer && quantity < bag.quantityAvailable
                                    Card(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clickable(enabled = canIncrease) { 
                                                if (canIncrease) quantity++ 
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (canIncrease) Color(0xFF4CAF50) else WarmGrey200
                                        ),
                                        shape = CircleShape,
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = if (canIncrease) 4.dp else 0.dp
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Increase quantity",
                                                tint = if (canIncrease) Color.White else WarmGrey400,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Total Price Preview
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Green50
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tổng tiền:",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Green700
                                    )
                                    Text(
                                        text = String.format("%,.0f", bag.discountedPrice * quantity) + "đ",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Green600
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Pickup time selector
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Thời gian nhận hàng mong muốn",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showTimePickerDialog = true
                                    },
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, WarmGrey300)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (selectedPickupTime.isNotEmpty()) {
                                            "Nhận lúc: $selectedPickupTime hôm nay"
                                        } else if (selectedPickupTimeFormatted != null) {
                                            "Nhận lúc: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedPickupTimeFormatted)} hôm nay"
                                        } else {
                                            "Chọn thời gian nhận hàng"
                                        },
                                        fontSize = 16.sp,
                                        color = if (selectedPickupTime.isNotEmpty() || selectedPickupTimeFormatted != null) {
                                            Color.Black
                                        } else {
                                            WarmGrey600
                                        }
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Pick time",
                                        tint = Green500
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Khung giờ có thể nhận: ${bag.pickupTimeDisplay}",
                                fontSize = 12.sp,
                                color = WarmGrey600
                            )
                            
                            // Show pickup time validation error
                            pickupTimeError?.let { error ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = error,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Order notes
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Ghi chú đơn hàng (tùy chọn)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = orderNotes,
                                onValueChange = { orderNotes = it },
                                placeholder = { Text("Thêm ghi chú cho đơn hàng...") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Green500,
                                    unfocusedBorderColor = WarmGrey300
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Enhanced Order Button
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (bag.isAvailable && !orderUiState.isCreatingOrder) 
                                Green600 else WarmGrey300
                        )
                    ) {
                        Button(
                            onClick = {
                                val pickupTimeIso = selectedPickupTimeFormatted?.let { pickupTime ->
                                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(pickupTime)
                                }
                                
                                val orderRequest = CreateOrderRequest(
                                    items = listOf(
                                        CreateOrderItemRequest(
                                            surpriseBagId = bagId,
                                            foodItemId = null,
                                            quantity = quantity
                                        )
                                    ),
                                    deliveryAddress = deliveryAddress,
                                    notes = orderNotes.takeIf { it.isNotBlank() } ?: "Đặt từ chi tiết Surprise Bag",
                                    preferredPickupTime = pickupTimeIso
                                )
                                hasTriggeredOrderCreation = true
                                orderViewModel.createOrder(orderRequest)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            enabled = bag.isAvailable && !orderUiState.isCreatingOrder,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            if (orderUiState.isCreatingOrder) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Đang xử lý...",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (bag.isAvailable) "Đặt ngay" else "Không khả dụng",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        if (bag.isAvailable) {
                                            Text(
                                                text = "$quantity túi • Nhận ${bag.pickupTimeDisplay}",
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.9f)
                                            )
                                        }
                                    }
                                    
                                    if (bag.isAvailable) {
                                        Text(
                                            text = String.format("%,.0f", bag.discountedPrice * quantity) + "đ",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        
        // Time Picker Dialog
        surpriseBag?.let { bag ->
            TimePickerDialog(
                title = "Chọn thời gian nhận hàng",
                isVisible = showTimePickerDialog,
                pickupStartTime = bag.pickupStartTime,
                pickupEndTime = bag.pickupEndTime,
                selectedTime = selectedPickupTime,
                onTimeSelected = { time ->
                    pickupTimeError = null
                    
                    // Validate the selected time
                    val validationResult = PickupTimeValidator.validatePickupTime(
                        selectedTime = time,
                        pickupStartTime = bag.pickupStartTime,
                        pickupEndTime = bag.pickupEndTime
                    )
                    
                    if (validationResult.isValid) {
                        // Also validate it's not in the past
                        val pastValidation = PickupTimeValidator.validateNotInPast(time)
                        if (pastValidation.isValid) {
                            selectedPickupTime = time
                            // Create a Date object for the selected time (today's date with selected time)
                            try {
                                val today = Calendar.getInstance()
                                val timeParts = time.split(":")
                                today.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                                today.set(Calendar.MINUTE, timeParts[1].toInt())
                                selectedPickupTimeFormatted = today.time
                            } catch (e: Exception) {
                                selectedPickupTimeFormatted = null
                            }
                            showTimePickerDialog = false
                        } else {
                            pickupTimeError = pastValidation.errorMessage
                        }
                    } else {
                        pickupTimeError = validationResult.errorMessage
                    }
                },
                onDismiss = {
                    showTimePickerDialog = false
                }
            )
        }
    }
}

// Enhanced function to add professional food images to a single surprise bag
private fun enhanceBagWithImage(bag: SurpriseBag): SurpriseBag {
    // If bag already has an image, keep it
    if (!bag.imageUrl.isNullOrBlank()) {
        return bag
    }

    val foodImages = mapOf(
        // Vietnamese Food Categories
        "combo" to listOf(
            "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Mixed food platter
            "https://images.unsplash.com/photo-1504674900247-0877df9cc836?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Delicious meal
            "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Food bowl
            "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80"  // Healthy food combo
        ),
        "thịt/cá" to listOf(
            "https://images.unsplash.com/photo-1546833999-b9f581a1996d?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Grilled meat
            "https://images.unsplash.com/photo-1544943910-4c1dc44aab44?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Fresh fish
            "https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Meat platter
            "https://images.unsplash.com/photo-1551218808-94e220e084d2?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80"  // Seafood
        ),
        "rau/củ" to listOf(
            "https://images.unsplash.com/photo-1540420773420-3366772f4999?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Fresh vegetables
            "https://images.unsplash.com/photo-1590779033100-9f60a05a013d?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Vegetable salad
            "https://images.unsplash.com/photo-1574316071802-0d684efa7bf5?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Root vegetables
            "https://images.unsplash.com/photo-1506976785307-8732e854ad03?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80"  // Mixed vegetables
        ),
        "trái cây" to listOf(
            "https://images.unsplash.com/photo-1619566636858-adf3ef46400b?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Tropical fruits
            "https://images.unsplash.com/photo-1610832958506-aa56368176cf?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Fresh fruit mix
            "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Citrus fruits
            "https://images.unsplash.com/photo-1464207687429-7505649dae38?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80"  // Berry mix
        ),
        "bánh mì" to listOf(
            "https://images.unsplash.com/photo-1516684732162-798a0062be99?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Vietnamese bánh mì
            "https://images.unsplash.com/photo-1549931319-a545dcf3bc73?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Fresh bread
            "https://images.unsplash.com/photo-1509440159596-0249088772ff?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Artisan bread
            "https://images.unsplash.com/photo-1565895405307-2da9c1b9b7b0?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80"  // Vietnamese sandwich
        ),
        "default" to listOf(
            "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Gourmet food
            "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Mixed cuisine
            "https://images.unsplash.com/photo-1576618148400-f54bed99fcfd?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Restaurant meal
            "https://images.unsplash.com/photo-1565958011703-44f9829ba187?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80"  // Food platter
        )
    )

    // Determine category for image selection using bagType
    val category = when {
        bag.bagType.lowercase().contains("combo") -> "combo"
        bag.bagType.lowercase().contains("thịt") || bag.bagType.lowercase().contains("cá") -> "thịt/cá"
        bag.bagType.lowercase().contains("rau") || bag.bagType.lowercase().contains("củ") -> "rau/củ"
        bag.bagType.lowercase().contains("trái") || bag.bagType.lowercase().contains("cây") -> "trái cây"
        bag.bagType.lowercase().contains("bánh") || bag.bagType.lowercase().contains("mì") -> "bánh mì"
        else -> "default"
    }

    val categoryImages = foodImages[category] ?: foodImages["default"]!!
    // Use first image from category for consistency in details view
    val imageUrl = categoryImages[0]

    android.util.Log.d("BagDetailsScreen", "Assigning image to bag '${bag.name}' (bagType: ${bag.bagType}): $imageUrl")

    return bag.copy(imageUrl = imageUrl)
}