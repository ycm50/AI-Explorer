package com.aiexplorer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiexplorer.data.LlmClient
import com.aiexplorer.data.SearchEngine
import com.aiexplorer.data.SettingsManager
import com.aiexplorer.ui.utils.BrowserUtils
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MainScreen(
    onOpenWebView: (engine: SearchEngine, query: String) -> Unit,
) {
    val context = LocalContext.current
    val settings = remember { SettingsManager(context) }
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 搜索引擎选择弹窗
    var showEnginePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        // 输入框（多行）
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                errorMessage = null
            },
            label = { Text("输入搜索意图") },
            placeholder = { Text("例如：在 GitHub 上找 Shizuku 项目\n查找 Kotlin 协程的 PDF 文档") },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            enabled = !isLoading,
            minLines = 3,
            maxLines = 6,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { /* handled by button */ }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MiuixTheme.colorScheme.onBackground,
                unfocusedTextColor = MiuixTheme.colorScheme.onBackground,
                cursorColor = MiuixTheme.colorScheme.primary,
                focusedBorderColor = MiuixTheme.colorScheme.primary,
                unfocusedBorderColor = MiuixTheme.colorScheme.outline,
            ),
        )

        Spacer(Modifier.height(12.dp))

        // 处理按钮
        Button(
            onClick = {
                if (query.isBlank()) {
                    errorMessage = "请输入搜索意图"
                    return@Button
                }
                if (settings.apiKey.isBlank()) {
                    errorMessage = "请先在「设置」中填写 API Key"
                    return@Button
                }
                isLoading = true
                result = null
                errorMessage = null

                val client = LlmClient(
                    baseUrl = settings.baseUrl,
                    apiKey = settings.apiKey,
                    model = settings.model,
                )
                scope.launch {
                    val outcome = client.generateSearchQuery(
                        userQuery = query,
                        systemPrompt = settings.promptTemplate,
                    )
                    outcome.fold(
                        onSuccess = { r -> result = r },
                        onFailure = { e -> errorMessage = "请求失败: ${e.message}" },
                    )
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MiuixTheme.colorScheme.primary,
                contentColor = MiuixTheme.colorScheme.onPrimary,
            ),
        ) {
            Text("处理", fontSize = 16.sp)
        }

        Spacer(Modifier.height(20.dp))

        // 加载中
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = MiuixTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "正在生成搜索查询…",
                        color = MiuixTheme.colorScheme.onBackgroundVariant,
                        fontSize = 14.sp,
                    )
                }
            }
        }

        // 错误信息
        errorMessage?.let { msg ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = msg,
                color = MiuixTheme.colorScheme.error,
                fontSize = 14.sp,
            )
        }

        // 结果
        result?.let { searchQuery ->
            Spacer(Modifier.height(16.dp))
            Text(
                text = "生成的搜索查询",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onBackgroundVariant,
            )
            Spacer(Modifier.height(6.dp))

            // 结果显示区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(12.dp),
            ) {
                Text(
                    text = searchQuery,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSecondaryContainer,
                )
            }

            Spacer(Modifier.height(16.dp))

            // 主操作：在应用内用 WebView 搜索（会先让用户挑搜索引擎）
            Button(
                onClick = { showEnginePicker = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MiuixTheme.colorScheme.primary,
                    contentColor = MiuixTheme.colorScheme.onPrimary,
                ),
            ) {
                Text("应用内搜索", fontSize = 16.sp)
            }

            Spacer(Modifier.height(8.dp))

            // 备选：交给外部浏览器
            OutlinedButton(
                onClick = {
                    val launched = BrowserUtils.searchDefault(context, searchQuery)
                    if (!launched) errorMessage = "未找到可用的浏览器"
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
            ) {
                Text(
                    text = "用默认浏览器搜索",
                    color = MiuixTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val launched = BrowserUtils.searchWithChooser(context, searchQuery)
                    if (!launched) errorMessage = "未找到可用的浏览器"
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
            ) {
                Text(
                    text = "选择浏览器搜索",
                    color = MiuixTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                )
            }
        }
    }

    // --- 搜索引擎选择弹窗 ---
    val searchQuery = result
    if (showEnginePicker && searchQuery != null) {
        // 每次都从设置读当前默认项，避免用户改过设置后这里的记忆值过期
        val currentEngine = settings.searchEngine
        AlertDialog(
            onDismissRequest = { showEnginePicker = false },
            modifier = Modifier.fillMaxWidth(),
            title = { Text("选择搜索引擎") },
            text = {
                // 覆盖 AlertDialog 正文的默认 typography，否则自定义颜色会被盖掉
                ProvideTextStyle(
                    TextStyle(
                        fontSize = 15.sp,
                        color = MiuixTheme.colorScheme.onBackground,
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        SearchEngine.BUILT_IN.forEachIndexed { index, engine ->
                            val isSelected = engine.id == currentEngine.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // 记住本次选择，下次默认用它
                                        settings.searchEngineId = engine.id
                                        showEnginePicker = false
                                        onOpenWebView(engine, searchQuery)
                                    }
                                    .padding(vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = engine.displayName,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold
                                    else FontWeight.Normal,
                                    color = if (isSelected) MiuixTheme.colorScheme.primary
                                    else MiuixTheme.colorScheme.onBackground,
                                    modifier = Modifier.weight(1f),
                                )
                                if (isSelected) {
                                    Text(
                                        text = "✓ 默认",
                                        fontSize = 12.sp,
                                        color = MiuixTheme.colorScheme.primary,
                                    )
                                }
                            }
                            if (index < SearchEngine.BUILT_IN.lastIndex) {
                                HorizontalDivider(
                                    color = MiuixTheme.colorScheme.dividerLine,
                                    thickness = 0.5.dp,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showEnginePicker = false }) {
                    Text("取消", color = MiuixTheme.colorScheme.primary)
                }
            },
        )
    }
}
