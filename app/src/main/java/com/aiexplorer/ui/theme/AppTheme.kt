package com.aiexplorer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme as miuixDarkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme as miuixLightColorScheme

/**
 * 应用主题。
 *
 * 本项目同时使用了两套组件：Miuix（小米 HyperOS 风格）和 androidx Material3。
 * [MiuixTheme] 只提供 `MiuixTheme.colorScheme`，它**不会**设置 Material3 的
 * `LocalColorScheme`。因此若不额外套一层 [MaterialTheme]，所有 Material3 组件
 * （ModalDrawerSheet / OutlinedTextField / HorizontalDivider 等）都会退回 Material3
 * 的默认浅色配色 —— 在系统深色模式下就会出现「深色底 + 浅色卡片」的错乱。
 *
 * 这里把 Miuix 的颜色角色映射给 Material3，让两套组件在同一套配色下渲染，
 * 并跟随系统的深浅色设置。
 */
@Composable
fun AiExplorerTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val miuixColors = if (dark) miuixDarkColorScheme() else miuixLightColorScheme()

    MiuixTheme(colors = miuixColors) {
        MaterialTheme(
            colorScheme = materialFromMiuix(miuixColors, dark),
            content = content,
        )
    }
}

/**
 * 将 Miuix 的 [Colors] 映射为 Material3 的 [ColorScheme]。
 *
 * 只覆盖本项目实际会用到的角色，其余角色由 Material3 的默认值填充。[dark] 决定用
 * 浅色还是深色 builder 打底，否则深色模式下未被覆盖的角色会是错误的浅色。
 */
private fun materialFromMiuix(c: Colors, dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = c.primary,
        onPrimary = c.onPrimary,
        primaryContainer = c.primaryContainer,
        onPrimaryContainer = c.onPrimaryContainer,
        secondary = c.secondary,
        onSecondary = c.onSecondary,
        secondaryContainer = c.secondaryContainer,
        onSecondaryContainer = c.onSecondaryContainer,
        tertiaryContainer = c.tertiaryContainer,
        onTertiaryContainer = c.onTertiaryContainer,
        background = c.background,
        onBackground = c.onBackground,
        surface = c.surface,
        onSurface = c.onSurface,
        outline = c.outline,
        error = c.error,
        onError = c.onError,
        errorContainer = c.errorContainer,
        onErrorContainer = c.onErrorContainer,
    )
}
