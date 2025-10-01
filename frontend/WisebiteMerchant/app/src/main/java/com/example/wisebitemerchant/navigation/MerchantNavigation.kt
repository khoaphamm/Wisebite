package com.example.wisebitemerchant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.wisebitemerchant.data.repository.FoodItemRepository
import com.example.wisebitemerchant.ui.screen.DashboardScreen
import com.example.wisebitemerchant.ui.screen.LoginScreen
import com.example.wisebitemerchant.ui.screen.MainScreen
import com.example.wisebitemerchant.ui.screen.SignupScreen
import kotlinx.coroutines.launch

@Composable
fun MerchantNavigation(
    navController: NavHostController,
    startDestination: String = Routes.LOGIN
) {
    val context = LocalContext.current
    val repository = FoodItemRepository.getInstance(context)
    val coroutineScope = rememberCoroutineScope()
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToSignup = {
                    navController.navigate(Routes.SIGNUP)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.SIGNUP) {
            SignupScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSignupSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.MAIN) {
            MainScreen(
                onLogout = {
                    coroutineScope.launch {
                        // Clear authentication token before navigating
                        repository.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable(Routes.DASHBOARD) {
            DashboardScreen()
        }
    }
}