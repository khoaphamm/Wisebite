package com.example.wisebite.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wisebite.data.repository.AuthRepository
import com.example.wisebite.ui.screen.AuthCheckScreen
import com.example.wisebite.ui.screen.BagDetailsScreen
import com.example.wisebite.ui.screen.HomeScreen
import com.example.wisebite.ui.screen.LoginScreen
import com.example.wisebite.ui.screen.MainScreen
import com.example.wisebite.ui.screen.SignupScreen
import com.example.wisebite.ui.viewmodel.LoginViewModel
import com.example.wisebite.ui.viewmodel.SignupViewModel
import com.example.wisebite.util.ViewModelFactory

@Composable
fun WisebiteNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.AUTH_CHECK
) {
    val context = LocalContext.current
    val authRepository = AuthRepository.getInstance(context)
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Authentication Check Screen
        composable(Routes.AUTH_CHECK) {
            AuthCheckScreen(
                onAuthenticated = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH_CHECK) { inclusive = true }
                    }
                },
                onNotAuthenticated = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.AUTH_CHECK) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.LOGIN) {
            val viewModel: LoginViewModel = viewModel(
                factory = ViewModelFactory.createLoginViewModelFactory(authRepository)
            )
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Routes.SIGNUP)
                }
            )
        }
        
        composable(Routes.SIGNUP) {
            val viewModel: SignupViewModel = viewModel(
                factory = ViewModelFactory.createSignupViewModelFactory(authRepository)
            )
            SignupScreen(
                viewModel = viewModel,
                onSignupSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SIGNUP) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Routes.HOME) {
            MainScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToBagDetails = {
                    navController.navigate(Routes.BAG_DETAILS)
                }
            )
        }
        
        composable(Routes.BAG_DETAILS) {
            BagDetailsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onOrderClick = {
                    // Handle order placement - could navigate to order confirmation
                    // For now, just go back to home
                    navController.popBackStack()
                }
            )
        }
    }
}