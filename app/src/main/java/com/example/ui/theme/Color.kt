package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Dark Theme Colors
val ObsidianMainStatic = Color(0xFF000000) // Background Black
val ObsidianSurfaceStatic = Color(0xFF121212) // Surface container
val ObsidianCardStatic = Color(0xFF1E1E1E) // Cards Dark Gray

val NeonLimeStatic = Color(0xFF00E5FF) // Accent Electric Blue 
val NeonTealStatic = Color(0xFF4CAF50) // Success Green
val NeonAmberStatic = Color(0xFFFF9900) // Warming notification alarm accent

// Text Colors
val TextWhiteStatic = Color(0xFFFFFFFF)
val TextMutedStatic = Color(0xFFAAAAAA)
val TextWarningStatic = Color(0xFFFF5F5F)

val TextWhite @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
val TextMuted @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
val TextWarning @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.error

val ObsidianMain @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.background
val ObsidianSurface @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.surface
val ObsidianCard @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant

val NeonLime @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
val NeonTeal @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.secondary
val NeonAmber @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.tertiary
