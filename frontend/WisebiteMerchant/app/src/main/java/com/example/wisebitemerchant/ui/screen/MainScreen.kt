package com.example.wisebitemerchant.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wisebitemerchant.data.repository.FoodItemRepository
import com.example.wisebitemerchant.navigation.Routes
import com.example.wisebitemerchant.navigation.merchantBottomNavItems
import com.example.wisebitemerchant.ui.viewmodel.AddFoodItemViewModel
import com.example.wisebitemerchant.ui.viewmodel.AddFoodItemViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                merchantBottomNavItems.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MainNavHost(
                navController = navController,
                onLogout = onLogout
            )
        }
    }
}

@Composable
fun MainNavHost(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SURPRISE_BAGS // Changed from STORAGE_MANAGEMENT per client request
    ) {
        // Storage Management temporarily hidden per client request
        /*
        composable(Routes.STORAGE_MANAGEMENT) {
            val context = LocalContext.current
            val repository = FoodItemRepository.getInstance(context)
            val viewModel: StorageManagementViewModel = viewModel(
                factory = StorageManagementViewModelFactory(repository)
            )
            
            // Refresh data when returning to this screen
            LaunchedEffect(Unit) {
                viewModel.refreshData()
            }
            
            StorageManagementScreen(
                viewModel = viewModel,
                onNavigateToAddItem = {
                    navController.navigate(Routes.ADD_FOOD_ITEM)
                }
            )
        }
        
        composable(Routes.ADD_FOOD_ITEM) {
            val context = LocalContext.current
            val repository = FoodItemRepository.getInstance(context)
            val viewModel: AddFoodItemViewModel = viewModel(
                factory = AddFoodItemViewModelFactory(repository)
            )
            
            AddFoodItemScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSave = {
                    navController.popBackStack()
                }
            )
        }
        */
        
        composable(Routes.SURPRISE_BAGS) {
            SurpriseBagsScreen()
        }
        
        composable(Routes.ORDERS) {
            OrdersScreen()
        }
        
        composable(Routes.PROFILE) {
            ProfileScreen(
                onLogout = onLogout,
                onNavigateToEditProfile = {
                    navController.navigate(Routes.EDIT_PROFILE)
                }
            )
        }
        
        composable(Routes.EDIT_PROFILE) {
            MerchantEditProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}