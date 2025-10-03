package com.example.wisebitemerchant.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wisebitemerchant.data.model.MerchantOrderStatus
import com.example.wisebitemerchant.data.model.Order
import com.example.wisebitemerchant.data.repository.ApiResult
import com.example.wisebitemerchant.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrderUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val errorMessage: String? = null,
    val isUpdatingOrder: Boolean = false
)

class OrderViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = orderRepository.getMyOrders()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        orders = result.data,
                        errorMessage = null
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is ApiResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun acceptOrder(orderId: String) {
        updateOrderStatus(orderId) { orderRepository.acceptOrder(orderId) }
    }

    fun rejectOrder(orderId: String) {
        updateOrderStatus(orderId) { orderRepository.rejectOrder(orderId) }
    }

    fun markOrderReady(orderId: String) {
        updateOrderStatus(orderId) { orderRepository.markOrderReady(orderId) }
    }

    fun markOrderCompleted(orderId: String) {
        updateOrderStatus(orderId) { orderRepository.markOrderCompleted(orderId) }
    }

    private fun updateOrderStatus(orderId: String, updateAction: suspend () -> ApiResult<Order>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingOrder = true, errorMessage = null)
            
            when (val result = updateAction()) {
                is ApiResult.Success -> {
                    // Update the specific order in the list
                    val updatedOrders = _uiState.value.orders.map { order ->
                        if (order.id == orderId) result.data else order
                    }
                    _uiState.value = _uiState.value.copy(
                        isUpdatingOrder = false,
                        orders = updatedOrders,
                        errorMessage = null
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdatingOrder = false,
                        errorMessage = result.message
                    )
                }
                is ApiResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isUpdatingOrder = true)
                }
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Helper methods to filter orders by status
    fun getOrdersByStatus(status: MerchantOrderStatus): List<Order> {
        return _uiState.value.orders.filter { it.merchantOrderStatus == status }
    }

    fun getNewOrders(): List<Order> = getOrdersByStatus(MerchantOrderStatus.NEW)
    
    fun getProcessingOrders(): List<Order> = 
        getOrdersByStatus(MerchantOrderStatus.CONFIRMED) + 
        getOrdersByStatus(MerchantOrderStatus.PREPARING)
    
    fun getReadyOrders(): List<Order> = getOrdersByStatus(MerchantOrderStatus.READY)
    
    fun getCompletedOrders(): List<Order> = getOrdersByStatus(MerchantOrderStatus.COMPLETED)
}

class OrderViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderViewModel(
                OrderRepository.getInstance(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}