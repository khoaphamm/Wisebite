package com.example.wisebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
                        surpriseBag = result.data
                        selectedPickupTime = result.data.pickupTimeDisplay
                        deliveryAddress = result.data.store?.displayAddress ?: ""
                        
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
                    // Surprise Bag Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Store and category
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bag.store?.name ?: "Unknown Store",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = bag.categoryDisplayName,
                                        fontSize = 14.sp,
                                        color = Orange600,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                // Discount badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Red500.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "-${bag.formattedDiscountPercentage}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Red500
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Bag name and description
                            Text(
                                text = bag.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
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
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Price information
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Giá ưu đãi",
                                        fontSize = 12.sp,
                                        color = WarmGrey600
                                    )
                                    Text(
                                        text = bag.formattedDiscountedPrice,
                                        fontSize = 20.sp,
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
                                        fontSize = 14.sp,
                                        color = WarmGrey500,
                                        style = androidx.compose.ui.text.TextStyle(
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                        )
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Location and pickup time
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    modifier = Modifier.size(16.dp),
                                    tint = WarmGrey600
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = bag.store?.displayAddress ?: "Địa chỉ không có",
                                    fontSize = 14.sp,
                                    color = WarmGrey700
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Pickup time",
                                    modifier = Modifier.size(16.dp),
                                    tint = WarmGrey600
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Nhận hàng: ${bag.pickupTimeDisplay}",
                                    fontSize = 14.sp,
                                    color = WarmGrey700
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = bag.quantityDisplay,
                                fontSize = 14.sp,
                                color = if (bag.quantityAvailable > 0) WarmGrey700 else Red500,
                                fontWeight = if (bag.quantityAvailable > 0) FontWeight.Normal else FontWeight.Medium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Quantity selector
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
                                text = "Số lượng",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = { if (quantity > 1) quantity-- },
                                    enabled = quantity > 1
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease quantity",
                                        tint = if (quantity > 1) Green600 else WarmGrey400
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Text(
                                    text = quantity.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                IconButton(
                                    onClick = { 
                                        if (quantity < (bag.maxPerCustomer) && 
                                            quantity < (bag.quantityAvailable)) {
                                            quantity++
                                        }
                                    },
                                    enabled = quantity < (bag.maxPerCustomer) && 
                                             quantity < (bag.quantityAvailable)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase quantity",
                                        tint = if (quantity < (bag.maxPerCustomer) && 
                                                  quantity < (bag.quantityAvailable)) 
                                               Green600 else WarmGrey400
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Tối đa: ${bag.maxPerCustomer} túi/khách hàng",
                                fontSize = 12.sp,
                                color = WarmGrey600,
                                modifier = Modifier.fillMaxWidth()
                            )
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
                    
                    // Order button
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
                            .height(50.dp),
                        enabled = bag.isAvailable && !orderUiState.isCreatingOrder,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green600,
                            disabledContainerColor = WarmGrey300
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (orderUiState.isCreatingOrder) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = if (bag.isAvailable) {
                                    "Đặt ngay • ${String.format("%,.0f", bag.discountedPrice * quantity)}đ"
                                } else {
                                    "Không khả dụng"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
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