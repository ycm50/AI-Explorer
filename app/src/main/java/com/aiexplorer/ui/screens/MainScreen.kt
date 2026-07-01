package com.aiexplorer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiexplorer.data.LlmClient
import com.aiexplorer.data.SettingsManager
import com.aiexplorer.ui.utils.BrowserUtils
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val settings = remember { SettingsManager(context) }
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    color = MiuixTheme.colorScheme.onSecondary,
                )
            }

            Spacer(Modifier.height(16.dp))

            // 浏览器搜索按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { BrowserUtils.searchDefault(context, searchQuery) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MiuixTheme.colorScheme.primary,
                        contentColor = MiuixTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text("默认浏览器搜索", fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = { BrowserUtils.searchWithChooser(context, searchQuery) },
                    modifier = Modifier.weight(1f).height(44.dp),
                ) {
                    Text("选择浏览器", color = MiuixTheme.colorScheme.onBackground, fontSize = 14.sp)
                }
            }
        }
    }
}
