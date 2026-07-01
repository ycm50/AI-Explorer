package com.aiexplorer.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * 单个模型信息，从 GET /models 返回的数据中解析。
 */
data class ModelInfo(
    val id: String,
    val ownedBy: String,
)

/**
 * 调用 OpenAI 兼容的 chat/completions API。
 * 将用户搜索意图 + 系统提示词送入 LLM，返回生成的搜索引擎查询字符串。
 */
class LlmClient(
    private val baseUrl: String,
    private val apiKey: String,
    private val model: String,
) {

    /**
     * @param userQuery  用户输入的搜索意图
     * @param systemPrompt  系统指令（提示词模板）
     * @return Result 包裹 LLM 返回的查询字符串
     */
    suspend fun generateSearchQuery(
        userQuery: String,
        systemPrompt: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val endpoint = "${baseUrl.trimEnd('/')}/chat/completions"
            val connection = URL(endpoint).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.doOutput = true
            connection.connectTimeout = 30_000
            connection.readTimeout = 30_000

            val requestBody = JSONObject().apply {
                put("model", model)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userQuery)
                    })
                })
                put("temperature", 0.3)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorBody = connection.errorStream?.let { stream ->
                    BufferedReader(InputStreamReader(stream)).readText()
                } ?: "Unknown error"
                return@withContext Result.failure(
                    Exception("API $responseCode: $errorBody")
                )
            }

            val body = BufferedReader(InputStreamReader(connection.inputStream)).readText()
            val json = JSONObject(body)
            val content = json
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()

            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        /**
         * 调用 GET {baseUrl}/models 获取可用的模型 ID 列表。
         */
        suspend fun listModels(
            baseUrl: String,
            apiKey: String,
        ): Result<List<ModelInfo>> = withContext(Dispatchers.IO) {
            try {
                val endpoint = "${baseUrl.trimEnd('/')}/models"
                val connection = URL(endpoint).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    val errorBody = connection.errorStream?.let { stream ->
                        BufferedReader(InputStreamReader(stream)).readText()
                    } ?: "Unknown error"
                    return@withContext Result.failure(
                        Exception("API $responseCode: $errorBody")
                    )
                }

                val body = BufferedReader(InputStreamReader(connection.inputStream)).readText()
                val json = JSONObject(body)
                val data = json.getJSONArray("data")
                val models = mutableListOf<ModelInfo>()
                for (i in 0 until data.length()) {
                    val item = data.getJSONObject(i)
                    models.add(ModelInfo(
                        id = item.getString("id"),
                        ownedBy = item.optString("owned_by", ""),
                    ))
                }
                Result.success(models)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
