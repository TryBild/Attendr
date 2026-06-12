package com.trybild.attendr.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AttendrColorScheme = lightColorScheme(
    primary = AttendrNavy,
    onPrimary = Color.White,
    background = AttendrBackground,
    surface = AttendrSurface,
    onSurface = AttendrTextPrimary,
    onSurfaceVariant = AttendrTextSecondary,
    error = AttendrError,
    outline = AttendrBorder
)

@Composable
fun AttendrTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AttendrColorScheme,
        typography = AttendrTypography,
        content = content
    )
}
