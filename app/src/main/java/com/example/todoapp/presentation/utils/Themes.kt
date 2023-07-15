package com.example.todoapp.presentation.utils

import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class MyColors(
    val colorSupportSeparator: Color,
    val colorSupportOverlay: Color,
    val colorPrimary: Color,
    val colorSecondary: Color,
    val colorTertiary: Color,
    val colorDisable: Color,
    val colorRed: Color,
    val colorGreen: Color,
    val colorBlue: Color,
    val colorGray: Color,
    val colorGrayLight: Color,
    val colorWhite: Color,
    val colorBackPrimary: Color,
    val colorBackSecondary: Color,
    val colorBackElevated: Color
)

data class Typography(
    val h1: Typography,
    val body1: Typography,
    val subtitle1: Typography,
    val button: Typography,
    val h2: Typography,

)

object AppTheme {
    val colors: MyColors
        @Composable
        get() = LocalMyColors.current

    val typography: Typography
        @Composable
        get() = LocalMyTypography.current

}

val LocalMyColors = staticCompositionLocalOf<MyColors> {
    error("No colors exist")
}

val LocalMyTypography = staticCompositionLocalOf<Typography> {
    error("No fonts exist")
}
