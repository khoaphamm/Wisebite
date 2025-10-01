package com.example.wisebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisebite.data.model.CreateOrderRequest
import com.example.wisebite.data.model.Order
import com.example.wisebite.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
            
            orderRepository.getUserOrders()
                .onSuccess { orders ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        orders = orders,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load orders"
                    )
                }
        }
    }

    fun createOrder(orderRequest: CreateOrderRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingOrder = true, errorMessage = null)
            
            orderRepository.createOrder(orderRequest)
                .onSuccess { order ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrder = false,
                        errorMessage = null
                    )
                    // Refresh the orders list
                    loadOrders()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrder = false,
                        errorMessage = exception.message ?: "Failed to create order"
                    )
                }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            orderRepository.cancelOrder(orderId)
                .onSuccess { cancelledOrder ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                    // Refresh the orders list
                    loadOrders()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to cancel order"
                    )
                }
        }
    }

    fun getOrderById(orderId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            orderRepository.getOrder(orderId)
                .onSuccess { order ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedOrder = order,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to fetch order details"
                    )
                }
        }
    }

    fun clearSelectedOrder() {
        _uiState.value = _uiState.value.copy(selectedOrder = null)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class OrderUiState(
    val isLoading: Boolean = false,
    val isCreatingOrder: Boolean = false,
    val orders: List<Order> = emptyList(),
    val selectedOrder: Order? = null,
    val errorMessage: String? = null
)