package com.example.wisebitemerchant.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

val merchantBottomNavItems = listOf(
    // BottomNavItem(
    //     title = "Kho hàng",
    //     selectedIcon = Icons.Filled.Storage,
    //     unselectedIcon = Icons.Outlined.Storage,
    //     route = Routes.STORAGE_MANAGEMENT
    // ), // Temporarily hidden per client request
    BottomNavItem(
        title = "Túi bất ngờ",
        selectedIcon = Icons.Filled.WorkspacePremium,
        unselectedIcon = Icons.Outlined.WorkspacePremium,
        route = Routes.SURPRISE_BAGS
    ),
    BottomNavItem(
        title = "Đơn hàng",
        selectedIcon = Icons.Filled.ShoppingCart,
        unselectedIcon = Icons.Outlined.ShoppingCart,
        route = Routes.ORDERS
    ),
    BottomNavItem(
        title = "Hồ sơ",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        route = Routes.PROFILE
    )
)