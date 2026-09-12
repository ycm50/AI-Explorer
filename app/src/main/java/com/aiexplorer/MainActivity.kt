package com.aiexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiexplorer.data.SearchEngine
import com.aiexplorer.ui.screens.MainScreen
import com.aiexplorer.ui.screens.SettingsScreen
import com.aiexplorer.ui.screens.WebViewScreen
import com.aiexplorer.ui.theme.AiExplorerTheme
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class Screen { MAIN, SETTINGS, WEBVIEW }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiExplorerTheme {
                AppContent()
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun AppContent() {
    var screen by remember { mutableStateOf(Screen.MAIN) }
    // 应用内 WebView 搜索的目标（进入 WEBVIEW 页时使用）
    var webEngine by remember { mutableStateOf<SearchEngine?>(null) }
    var webQuery by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as ComponentActivity

    // 返回键处理：设置页/WebView 页 → 主页，主页 → 退出
    BackHandler(enabled = true) {
        if (screen == Screen.MAIN) {
            activity.finish()
        } else {
            screen = Screen.MAIN
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(260.dp),
            ) {
                // 头部
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MiuixTheme.colorScheme.primary)
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                ) {
                    Text(
                        "AI Explorer",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.onPrimary,
                    )
                }

                Spacer(Modifier.height(8.dp))

                // 主页
                DrawerItem(
                    label = "主页",
                    icon = "🏠",
                    isSelected = screen == Screen.MAIN,
                    onClick = {
                        screen = Screen.MAIN
                        scope.launch { drawerState.close() }
                    },
                )

                // 设置
                DrawerItem(
                    label = "设置",
                    icon = "⚙",
                    isSelected = screen == Screen.SETTINGS,
                    onClick = {
                        screen = Screen.SETTINGS
                        scope.launch { drawerState.close() }
                    },
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(MiuixTheme.colorScheme.background)
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Text("☰", fontSize = 22.sp, color = MiuixTheme.colorScheme.onBackground)
                        }
                        Text(
                            text = when (screen) {
                                Screen.MAIN -> "AI Explorer"
                                Screen.SETTINGS -> "设置"
                                Screen.WEBVIEW -> "搜索结果"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                }
                HorizontalDivider(color = MiuixTheme.colorScheme.dividerLine)
            },
            containerColor = MiuixTheme.colorScheme.background,
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                Crossfade(
                    targetState = screen,
                    animationSpec = tween(durationMillis = 500),
                ) { currentScreen ->
                    when (currentScreen) {
                        Screen.MAIN -> MainScreen(
                            onOpenWebView = { engine, query ->
                                webEngine = engine
                                webQuery = query
                                screen = Screen.WEBVIEW
                            },
                        )

                        Screen.SETTINGS -> SettingsScreen(
                            onNavigateBack = { screen = Screen.MAIN },
                        )

                        Screen.WEBVIEW -> {
                            // 进入该页前必定已赋值 engine，这里仅作兜底
                            webEngine?.let { engine ->
                                WebViewScreen(
                                    engine = engine,
                                    query = webQuery,
                                    onClose = { screen = Screen.MAIN },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)
                else Color.Transparent
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MiuixTheme.colorScheme.primary
            else MiuixTheme.colorScheme.onBackground,
        )
    }
}
