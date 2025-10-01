package com.example.wisebitemerchant.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Orange400,
    onPrimary = WarmGrey900,
    primaryContainer = Orange700,
    onPrimaryContainer = Orange100,
    
    secondary = WarmGrey400,
    onSecondary = WarmGrey900,
    secondaryContainer = WarmGrey700,
    onSecondaryContainer = WarmGrey100,
    
    tertiary = Blue400,
    onTertiary = WarmGrey900,
    tertiaryContainer = Blue700,
    onTertiaryContainer = Blue100,
    
    error = Red400,
    onError = WarmGrey900,
    errorContainer = Red700,
    onErrorContainer = Red100,
    
    background = WarmGrey900,
    onBackground = WarmGrey100,
    surface = WarmGrey900,
    onSurface = WarmGrey100,
    surfaceVariant = WarmGrey800,
    onSurfaceVariant = WarmGrey300,
    
    outline = WarmGrey500
)

private val LightColorScheme = lightColorScheme(
    primary = Orange600,
    onPrimary = Color.White,
    primaryContainer = Orange100,
    onPrimaryContainer = Orange900,
    
    secondary = WarmGrey600,
    onSecondary = Color.White,
    secondaryContainer = WarmGrey100,
    onSecondaryContainer = WarmGrey900,
    
    tertiary = Blue600,
    onTertiary = Color.White,
    tertiaryContainer = Blue100,
    onTertiaryContainer = Blue900,
    
    error = Red600,
    onError = Color.White,
    errorContainer = Red100,
    onErrorContainer = Red900,
    
    background = WarmGrey50,
    onBackground = WarmGrey900,
    surface = Color.White,
    onSurface = WarmGrey900,
    surfaceVariant = WarmGrey100,
    onSurfaceVariant = WarmGrey700,
    
    outline = WarmGrey400
)

@Composable
fun WisebiteMerchantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}