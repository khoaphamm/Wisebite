package com.example.wisebite.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wisebite.navigation.Routes
import com.example.wisebite.navigation.bottomNavItems
import com.example.wisebite.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit = {},
    onNavigateToBagDetails: (String) -> Unit = {},
    onNavigateToOrderDebug: () -> Unit = {},
    onNavigateToSurpriseBagList: () -> Unit = {},
    onNavigateToStoreBags: (String) -> Unit = {},
    onNavigateToPrivacySecurity: () -> Unit = {},
    onNavigateToHelpSupport: () -> Unit = {},
    onNavigateToShareApp: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val navController = rememberNavController()
    
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Green500,
                modifier = Modifier.height(70.dp)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { 
                        it.route == item.route 
                    } == true
                    
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = null,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Green500,
                            unselectedIconColor = WarmGrey600,
                            indicatorColor = Green100
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onLogout = onLogout,
                    onNavigateToBagDetails = onNavigateToBagDetails,
                    onNavigateToOrderDebug = onNavigateToOrderDebug,
                    onNavigateToSurpriseBagList = onNavigateToSurpriseBagList,
                    onNavigateToStoreBags = onNavigateToStoreBags
                )
            }
            
            composable(Routes.ORDERS) {
                OrdersScreen()
            }
            
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onLogout = onLogout,
                    onNavigateToEditProfile = {
                        navController.navigate(Routes.EDIT_PROFILE)
                    },
                    onNavigateToPrivacySecurity = onNavigateToPrivacySecurity,
                    onNavigateToHelpSupport = onNavigateToHelpSupport,
                    onNavigateToShareApp = onNavigateToShareApp,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
            
            composable(Routes.EDIT_PROFILE) {
                EditProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
