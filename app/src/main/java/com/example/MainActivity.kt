package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ClockAppUi
import com.example.ui.ClockViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val clockViewModel: ClockViewModel = viewModel()
      val themeMode by clockViewModel.themeMode.collectAsStateWithLifecycle()
      val isAppDarkTheme by clockViewModel.isAppDarkTheme.collectAsStateWithLifecycle()
      
      val displaySize by clockViewModel.displaySize.collectAsStateWithLifecycle()
      val fontFamily by clockViewModel.fontFamilyStr.collectAsStateWithLifecycle()
      val fontWeight by clockViewModel.fontWeightStr.collectAsStateWithLifecycle()
      val colorProfile by clockViewModel.colorProfile.collectAsStateWithLifecycle()
      val enableMonochrome by clockViewModel.enableMonochrome.collectAsStateWithLifecycle()
      val enableDynamicColor by clockViewModel.enableDynamicColor.collectAsStateWithLifecycle()
      val enableAmoledMode by clockViewModel.enableAmoledMode.collectAsStateWithLifecycle()
      
      val enableGlassEffect by clockViewModel.enableGlassEffect.collectAsStateWithLifecycle()
      val glassBlurStrength by clockViewModel.glassBlurStrength.collectAsStateWithLifecycle()
      val glassTransparency by clockViewModel.glassTransparency.collectAsStateWithLifecycle()
      val glassBorderThickness by clockViewModel.glassBorderThickness.collectAsStateWithLifecycle()
      
      MyApplicationTheme(
          themeMode = themeMode,
          isDarkThemeOverride = isAppDarkTheme,
          dynamicColor = enableDynamicColor,
          displaySize = displaySize,
          fontFamily = fontFamily,
          fontWeight = fontWeight,
          colorProfile = colorProfile,
          enableMonochrome = enableMonochrome,
          enableAmoledMode = enableAmoledMode,
          enableGlassEffect = enableGlassEffect,
          glassBlurStrength = glassBlurStrength,
          glassTransparency = glassTransparency,
          glassBorderThickness = glassBorderThickness
      ) {
        ClockAppUi(viewModel = clockViewModel)
      }
    }
  }
}
