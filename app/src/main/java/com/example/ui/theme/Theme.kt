package com.example.ui.theme

import android.os.Build
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.LocalTextStyle

private val MidnightBlackDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF5F5F7),
    secondary = Color(0xFF9E9E9E),
    tertiary = Color(0xFF616161),
    background = Color(0xFF0A0A0A),
    surface = Color(0xFF16161A),
    onBackground = TextWhiteStatic,
    onSurface = TextWhiteStatic,
    surfaceVariant = Color(0xFF24242B),
    onSurfaceVariant = TextMutedStatic
)

private val OceanBlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D2FF),
    secondary = Color(0xFF0086FF),
    tertiary = Color(0xFF00FFC4),
    background = Color(0xFF030E1A),
    surface = Color(0xFF071B2F),
    onBackground = TextWhiteStatic,
    onSurface = TextWhiteStatic,
    surfaceVariant = Color(0xFF0C2945),
    onSurfaceVariant = TextMutedStatic
)

private val RoyalPurpleDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFF9C27B0),
    tertiary = Color(0xFFE040FB),
    background = Color(0xFF0C0518),
    surface = Color(0xFF190D2D),
    onBackground = TextWhiteStatic,
    onSurface = TextWhiteStatic,
    surfaceVariant = Color(0xFF281845),
    onSurfaceVariant = TextMutedStatic
)

private val EmeraldGreenDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00FF87),
    secondary = Color(0xFF60EFA0),
    tertiary = Color(0xFF00FFD1),
    background = Color(0xFF020F08),
    surface = Color(0xFF061F12),
    onBackground = TextWhiteStatic,
    onSurface = TextWhiteStatic,
    surfaceVariant = Color(0xFF0B331E),
    onSurfaceVariant = TextMutedStatic
)

private val CrimsonRedDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF3366),
    secondary = Color(0xFFFF0D43),
    tertiary = Color(0xFFFFB300),
    background = Color(0xFF0D0204),
    surface = Color(0xFF1F060A),
    onBackground = TextWhiteStatic,
    onSurface = TextWhiteStatic,
    surfaceVariant = Color(0xFF320D14),
    onSurfaceVariant = TextMutedStatic
)

private val SunsetOrangeDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF6B35),
    secondary = Color(0xFFF7B731),
    tertiary = Color(0xFFFD2D1F),
    background = Color(0xFF0F0802),
    surface = Color(0xFF1F1206),
    onBackground = TextWhiteStatic,
    onSurface = TextWhiteStatic,
    surfaceVariant = Color(0xFF331E0C),
    onSurfaceVariant = TextMutedStatic
)

private val RosePinkDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF69B4),
    secondary = Color(0xFFFFB6C1),
    tertiary = Color(0xFFBA55D3),
    background = Color(0xFF0E0308),
    surface = Color(0xFF220915),
    onBackground = TextWhiteStatic,
    onSurface = TextWhiteStatic,
    surfaceVariant = Color(0xFF381427),
    onSurfaceVariant = TextMutedStatic
)

private val NeonCyberDarkColorScheme = darkColorScheme(
    primary = Color(0xFF39FF14),
    secondary = Color(0xFF00FFFF),
    tertiary = Color(0xFFFF007F),
    background = Color(0xFF05000A),
    surface = Color(0xFF0E0118),
    onBackground = TextWhiteStatic,
    onSurface = TextWhiteStatic,
    surfaceVariant = Color(0xFF1E0331),
    onSurfaceVariant = TextMutedStatic
)

private val GalaxyGradientDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8A2BE2),
    secondary = Color(0xFFFF1493),
    tertiary = Color(0xFF00FFFF),
    background = Color(0xFF04020F),
    surface = Color(0xFF0D0725),
    onBackground = TextWhiteStatic,
    onSurface = TextWhiteStatic,
    surfaceVariant = Color(0xFF1E114D),
    onSurfaceVariant = TextMutedStatic
)

private val RockThemeDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE5FF00),
    secondary = Color(0xFF8C969E),
    tertiary = Color(0xFFFF6200),
    background = Color(0xFF08090A),
    surface = Color(0xFF141619),
    onBackground = TextWhiteStatic,
    onSurface = TextWhiteStatic,
    surfaceVariant = Color(0xFF22252A),
    onSurfaceVariant = TextMutedStatic
)

private val MidnightBlackLightColorScheme = lightColorScheme(
    primary = Color(0xFF1D1D1F),
    secondary = Color(0xFF6E6E73),
    tertiary = Color(0xFF86868B),
    background = Color(0xFFF5F5F7),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1D1D1F),
    onSurface = Color(0xFF1D1D1F),
    surfaceVariant = Color(0xFFEAEAEA),
    onSurfaceVariant = Color(0xFF6E6E73)
)

private val OceanBlueLightColorScheme = lightColorScheme(
    primary = Color(0xFF0066CC),
    secondary = Color(0xFF00A2FF),
    tertiary = Color(0xFF00B388),
    background = Color(0xFFF0F6FC),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF030E1A),
    onSurface = Color(0xFF030E1A),
    surfaceVariant = Color(0xFFD6E4F0),
    onSurfaceVariant = Color(0xFF0066CC)
)

private val RoyalPurpleLightColorScheme = lightColorScheme(
    primary = Color(0xFF7A22B4),
    secondary = Color(0xFFB359E9),
    tertiary = Color(0xFFFF4081),
    background = Color(0xFFFAF4FC),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0C0518),
    onSurface = Color(0xFF0C0518),
    surfaceVariant = Color(0xFFEADDF3),
    onSurfaceVariant = Color(0xFF7A22B4)
)

private val EmeraldGreenLightColorScheme = lightColorScheme(
    primary = Color(0xFF007A3E),
    secondary = Color(0xFF2ECC71),
    tertiary = Color(0xFF009688),
    background = Color(0xFFF1F8F4),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF020F08),
    onSurface = Color(0xFF020F08),
    surfaceVariant = Color(0xFFD4E6DC),
    onSurfaceVariant = Color(0xFF007A3E)
)

private val CrimsonRedLightColorScheme = lightColorScheme(
    primary = Color(0xFFC00032),
    secondary = Color(0xFFFF4060),
    tertiary = Color(0xFFFF9800),
    background = Color(0xFFFCF0F2),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0D0204),
    onSurface = Color(0xFF0D0204),
    surfaceVariant = Color(0xFFF3D2D7),
    onSurfaceVariant = Color(0xFFC00032)
)

private val SunsetOrangeLightColorScheme = lightColorScheme(
    primary = Color(0xFFE65100),
    secondary = Color(0xFFF57C00),
    tertiary = Color(0xFFD84315),
    background = Color(0xFFFFF7F2),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F0802),
    onSurface = Color(0xFF0F0802),
    surfaceVariant = Color(0xFFF9E2D2),
    onSurfaceVariant = Color(0xFFE65100)
)

private val RosePinkLightColorScheme = lightColorScheme(
    primary = Color(0xFFD81B60),
    secondary = Color(0xFFF48FB1),
    tertiary = Color(0xFF9C27B0),
    background = Color(0xFFFFF2F6),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0E0308),
    onSurface = Color(0xFF0E0308),
    surfaceVariant = Color(0xFFFAD1DE),
    onSurfaceVariant = Color(0xFFD81B60)
)

private val NeonCyberLightColorScheme = lightColorScheme(
    primary = Color(0xFF00A300),
    secondary = Color(0xFF008B8B),
    tertiary = Color(0xFFC71585),
    background = Color(0xFFF7F2FA),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF05000A),
    onSurface = Color(0xFF05000A),
    surfaceVariant = Color(0xFFE5D5F0),
    onSurfaceVariant = Color(0xFF00A300)
)

private val GalaxyGradientLightColorScheme = lightColorScheme(
    primary = Color(0xFF4A00E0),
    secondary = Color(0xFF8E2DE2),
    tertiary = Color(0xFFFF007F),
    background = Color(0xFFF5F3FF),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF04020F),
    onSurface = Color(0xFF04020F),
    surfaceVariant = Color(0xFFE2DCF7),
    onSurfaceVariant = Color(0xFF4A00E0)
)

private val RockThemeLightColorScheme = lightColorScheme(
    primary = Color(0xFF1A1D20),
    secondary = Color(0xFFFF6200),
    tertiary = Color(0xFF495057),
    background = Color(0xFFF1F3F5),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF08090A),
    onSurface = Color(0xFF08090A),
    surfaceVariant = Color(0xFFDEE2E6),
    onSurfaceVariant = Color(0xFF1A1D20)
)

val LocalCustomFontFamily = compositionLocalOf { FontFamily.Monospace }
val LocalCustomFontWeight = compositionLocalOf { FontWeight.Bold }

data class GlassEffectConfig(
    val enabled: Boolean = true,
    val blurStrength: Float = 15f,
    val transparency: Float = 0.15f,
    val borderThickness: Float = 1f
)

val LocalGlassEffectConfig = compositionLocalOf { GlassEffectConfig() }

@Composable
fun LiquidGlassSurface(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    val config = LocalGlassEffectConfig.current
    Box(modifier = modifier) {
        val radius = cornerRadius
        val trans = config.transparency
        val borderThick = config.borderThickness
        val blurVal = config.blurStrength

        if (config.enabled) {
            // Sibling 1: background layer that performs actual RenderEffect blur on Android 12+ (API 31+)
            Box(
                modifier = androidx.compose.ui.Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(radius))
                    .graphicsLayer {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurVal > 0f) {
                            val blur = RenderEffect.createBlurEffect(
                                blurVal,
                                blurVal,
                                Shader.TileMode.DECAL
                            )
                            renderEffect = blur.asComposeRenderEffect()
                        }
                    }
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = (trans * 0.22f).coerceIn(0f, 1f)),
                                Color.White.copy(alpha = (trans * 0.05f).coerceIn(0f, 1f))
                            )
                        )
                    )
            )

            // Sibling 2: shiny metallic/neon border
            Box(
                modifier = androidx.compose.ui.Modifier
                    .matchParentSize()
                    .border(
                        width = borderThick.dp,
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                borderColor.copy(alpha = (trans * 0.7f).coerceIn(0f, 1f)),
                                borderColor.copy(alpha = (trans * 0.15f).coerceIn(0f, 1f))
                            )
                        ),
                        shape = RoundedCornerShape(radius)
                    )
            )
        } else {
            // Non-glass fallback theme background
            Box(
                modifier = androidx.compose.ui.Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(radius))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(radius)
                    )
            )
        }

        // Sibling 3: Crisp, perfectly readable foreground content container
        Box(modifier = androidx.compose.ui.Modifier) {
            content()
        }
    }
}

@Composable
fun ThemedText(
    text: String,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (androidx.compose.ui.text.TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight?.let {
            if (it == FontWeight.Bold || it == FontWeight.Medium || it == FontWeight.SemiBold || it == FontWeight.ExtraBold) {
                LocalCustomFontWeight.current
            } else it
        } ?: LocalCustomFontWeight.current,
        fontFamily = fontFamily ?: LocalCustomFontFamily.current,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}

@Composable
fun MyApplicationTheme(
  themeMode: String = "Dark",
  isDarkThemeOverride: Boolean? = null,
  dynamicColor: Boolean = false,
  displaySize: String = "Standard",
  fontFamily: String = "Monospace",
  fontWeight: String = "Bold",
  colorProfile: String = "Rock Theme",
  enableMonochrome: Boolean = false,
  enableAmoledMode: Boolean = false,
  enableGlassEffect: Boolean = true,
  glassBlurStrength: Float = 15f,
  glassTransparency: Float = 0.15f,
  glassBorderThickness: Float = 1f,
  content: @Composable () -> Unit,
) {
  val darkTheme = isDarkThemeOverride ?: when (themeMode) {
      "Light" -> false
      "Dark" -> true
      "Auto (Time-based)" -> {
          val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
          hour !in 5..16
      }
      else -> isSystemInDarkTheme()
  }

  val context = LocalContext.current
  val dynamicScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
  } else {
      null
  }

  val baseScheme = dynamicScheme ?: if (enableMonochrome) {
      if (darkTheme) {
          darkColorScheme(
              primary = Color(0xFFF5F5F7),
              secondary = Color(0xFFCCCCCC),
              tertiary = Color(0xFF999999),
              background = Color(0xFF000000),
              surface = Color(0xFF16161A),
              onBackground = Color(0xFFF5F5F7),
              onSurface = Color(0xFFF5F5F7),
              surfaceVariant = Color(0xFF24242B),
              onSurfaceVariant = Color(0xFFAAAAAA)
          )
      } else {
          lightColorScheme(
              primary = Color(0xFF1D1D1F),
              secondary = Color(0xFF555555),
              tertiary = Color(0xFF888888),
              background = Color(0xFFFAFAFA),
              surface = Color(0xFFFFFFFF),
              onBackground = Color(0xFF1D1D1F),
              onSurface = Color(0xFF1D1D1F),
              surfaceVariant = Color(0xFFEEEEEE),
              onSurfaceVariant = Color(0xFF666666)
          )
      }
  } else if (darkTheme) {
      when (colorProfile) {
          "Midnight Black" -> MidnightBlackDarkColorScheme
          "Ocean Blue" -> OceanBlueDarkColorScheme
          "Royal Purple" -> RoyalPurpleDarkColorScheme
          "Emerald Green" -> EmeraldGreenDarkColorScheme
          "Crimson Red" -> CrimsonRedDarkColorScheme
          "Sunset Orange" -> SunsetOrangeDarkColorScheme
          "Rose Pink" -> RosePinkDarkColorScheme
          "Neon Cyber" -> NeonCyberDarkColorScheme
          "Galaxy Gradient" -> GalaxyGradientDarkColorScheme
          "Rock Theme" -> RockThemeDarkColorScheme
          else -> RockThemeDarkColorScheme
      }
  } else {
      when (colorProfile) {
          "Midnight Black" -> MidnightBlackLightColorScheme
          "Ocean Blue" -> OceanBlueLightColorScheme
          "Royal Purple" -> RoyalPurpleLightColorScheme
          "Emerald Green" -> EmeraldGreenLightColorScheme
          "Crimson Red" -> CrimsonRedLightColorScheme
          "Sunset Orange" -> SunsetOrangeLightColorScheme
          "Rose Pink" -> RosePinkLightColorScheme
          "Neon Cyber" -> NeonCyberLightColorScheme
          "Galaxy Gradient" -> GalaxyGradientLightColorScheme
          "Rock Theme" -> RockThemeLightColorScheme
          else -> RockThemeLightColorScheme
      }
  }

  val colorScheme = if (darkTheme && enableAmoledMode) {
      baseScheme.copy(
          background = Color.Black,
          surface = Color.Black,
          surfaceVariant = Color(0xFF121212)
      )
  } else {
      baseScheme
  }

  val localDensity = LocalDensity.current
  val fontScaleMult = when (displaySize) {
      "Large" -> 1.3f
      "Small" -> 0.85f
      else -> 1.0f
  }
  
  val ff = when (fontFamily) {
      "Sans-Serif" -> FontFamily.SansSerif
      "Serif" -> FontFamily.Serif
      else -> FontFamily.Monospace
  }
  
  val fw = when (fontWeight) {
      "Normal" -> FontWeight.Normal
      "SemiBold" -> FontWeight.SemiBold
      "Bold" -> FontWeight.Bold
      else -> FontWeight.Bold
  }

  CompositionLocalProvider(
      LocalDensity provides Density(density = localDensity.density, fontScale = localDensity.fontScale * fontScaleMult),
      LocalCustomFontFamily provides ff,
      LocalCustomFontWeight provides fw,
      LocalGlassEffectConfig provides GlassEffectConfig(
          enabled = enableGlassEffect,
          blurStrength = glassBlurStrength,
          transparency = glassTransparency,
          borderThickness = glassBorderThickness
      )
  ) {
      MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
