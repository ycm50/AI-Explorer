package com.aiexplorer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiexplorer.data.LlmClient
import com.aiexplorer.data.ModelInfo
import com.aiexplorer.data.SearchEngine
import com.aiexplorer.data.SettingsManager
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val settings = remember { SettingsManager(context) }

    var baseUrl by remember { mutableStateOf(settings.baseUrl) }
    var apiKey by remember { mutableStateOf(settings.apiKey) }
    var model by remember { mutableStateOf(settings.model) }
    var promptTemplate by remember { mutableStateOf(settings.promptTemplate) }
    var searchEngine by remember { mutableStateOf(settings.searchEngine) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "LLM 设置",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(20.dp))

        SectionLabel("Base URL")
        OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            placeholder = { Text("https://api.openai.com/v1") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
            colors = inputFieldColors(),
        )

        Spacer(Modifier.height(12.dp))

        SectionLabel("API Key")
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            placeholder = { Text("sk-...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = inputFieldColors(),
        )

        Spacer(Modifier.height(12.dp))

        // --- 状态：模型获取 ---
        var models by remember { mutableStateOf<List<ModelInfo>>(emptyList()) }
        var isLoadingModels by remember { mutableStateOf(false) }
        var modelError by remember { mutableStateOf<String?>(null) }
        var showModelDropdown by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        SectionLabel("模型")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                placeholder = { Text("gpt-4o-mini") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = inputFieldColors(),
            )

            // 获取模型按钮
            Button(
                onClick = {
                    if (apiKey.isBlank()) {
                        modelError = "请先输入 API Key"
                        return@Button
                    }
                    isLoadingModels = true
                    modelError = null
                    showModelDropdown = false
                    scope.launch {
                        val result = LlmClient.listModels(baseUrl, apiKey)
                        result.fold(
                            onSuccess = { list ->
                                models = list
                                showModelDropdown = list.isNotEmpty()
                            },
                            onFailure = { e ->
                                modelError = "获取失败: ${e.message}"
                            },
                        )
                        isLoadingModels = false
                    }
                },
                enabled = !isLoadingModels,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MiuixTheme.colorScheme.primary,
                    contentColor = MiuixTheme.colorScheme.onPrimary,
                ),
            ) {
                if (isLoadingModels) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MiuixTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("获取列表", fontSize = 13.sp)
                }
            }
        }

        // 模型下拉列表
        if (showModelDropdown && models.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MiuixTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .heightIn(max = 200.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Column {
                    models.forEachIndexed { index, info ->
                        val isSelected = info.id == model
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    model = info.id
                                    showModelDropdown = false
                                }
                                .background(
                                    if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else Color.Transparent
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = info.id,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            if (info.ownedBy.isNotBlank()) {
                                Text(
                                    text = info.ownedBy,
                                    fontSize = 11.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceSecondary,
                                )
                            }
                        }
                        if (index < models.lastIndex) {
                            HorizontalDivider(
                                color = MiuixTheme.colorScheme.dividerLine,
                                thickness = 0.5.dp,
                            )
                        }
                    }
                }
            }
        }

        // 模型错误提示
        modelError?.let { err ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = err,
                color = MiuixTheme.colorScheme.error,
                fontSize = 12.sp,
            )
        }

        // 选中模型后隐藏列表
        Spacer(Modifier.height(12.dp))

        // --- 默认搜索引擎（应用内 WebView 使用）---
        SectionLabel("默认搜索引擎")
        Column(modifier = Modifier.fillMaxWidth()) {
            SearchEngine.BUILT_IN.forEachIndexed { index, engine ->
                val isSelected = engine.id == searchEngine.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { searchEngine = engine }
                        .background(
                            if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else Color.Transparent
                        )
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = engine.displayName,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MiuixTheme.colorScheme.primary
                        else MiuixTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f),
                    )
                    if (isSelected) {
                        Text(
                            text = "✓",
                            fontSize = 15.sp,
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

        Spacer(Modifier.height(12.dp))

        SectionLabel("提示词（System Prompt）")
        OutlinedTextField(
            value = promptTemplate,
            onValueChange = { promptTemplate = it },
            modifier = Modifier.fillMaxWidth().height(220.dp),
            minLines = 8,
            maxLines = 12,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            colors = inputFieldColors(),
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                settings.baseUrl = baseUrl
                settings.apiKey = apiKey
                settings.model = model
                settings.promptTemplate = promptTemplate
                settings.searchEngineId = searchEngine.id
                onNavigateBack()
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MiuixTheme.colorScheme.primary,
                contentColor = MiuixTheme.colorScheme.onPrimary,
            ),
        ) {
            Text("保存并返回", fontSize = 16.sp)
        }

        Spacer(Modifier.height(8.dp))

        // 恢复默认：把表单字段还原为内置默认值。
        // 刻意不动 API Key —— 那是用户的凭据，误触清空代价太高。
        OutlinedButton(
            onClick = {
                baseUrl = SettingsManager.DEFAULT_BASE_URL
                model = SettingsManager.DEFAULT_MODEL
                promptTemplate = SettingsManager.DEFAULT_PROMPT
                searchEngine = SearchEngine.findById(SearchEngine.DEFAULT_ID)
                showModelDropdown = false
                modelError = null
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text("恢复默认", color = MiuixTheme.colorScheme.onBackground, fontSize = 16.sp)
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text("返回", color = MiuixTheme.colorScheme.onBackground, fontSize = 16.sp)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MiuixTheme.colorScheme.onBackgroundVariant,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}

@Composable
private fun inputFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MiuixTheme.colorScheme.onBackground,
    unfocusedTextColor = MiuixTheme.colorScheme.onBackground,
    cursorColor = MiuixTheme.colorScheme.primary,
    focusedBorderColor = MiuixTheme.colorScheme.primary,
    unfocusedBorderColor = MiuixTheme.colorScheme.outline,
)
