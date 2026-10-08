package com.accountbook.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AA4FF),
    onPrimary = Color(0xFF0B1230),
    secondary = Color(0xFF4DE1C1),
    background = Color(0xFF0B0F1A),
    onBackground = Color(0xFFE8ECF8),
    surface = Color(0xFF121827),
    onSurface = Color(0xFFE8ECF8),
    surfaceVariant = Color(0xFF1B2338),
    onSurfaceVariant = Color(0xFFA9B3CC),
    error = Color(0xFFFF7A8A)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF3B5BDB),
    onPrimary = Color.White,
    secondary = Color(0xFF0CA789),
    background = Color(0xFFF4F6FF),
    onBackground = Color(0xFF111628),
    surface = Color.White,
    onSurface = Color(0xFF111628),
    surfaceVariant = Color(0xFFE9EDFC),
    onSurfaceVariant = Color(0xFF50587A),
    error = Color(0xFFD6334A)
)

val Green = Color(0xFF2FBF71)
val Red = Color(0xFFE5484D)

@Composable
fun AccountTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp)
        ),
        content = content
    )
}
