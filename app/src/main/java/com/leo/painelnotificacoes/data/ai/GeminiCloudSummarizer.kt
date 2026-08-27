package com.leo.painelnotificacoes.data.ai

import com.leo.painelnotificacoes.data.local.NotificationEntity
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Cloud fallback for devices outside ML Kit GenAI's on-device hardware allowlist. Sends
 * notification text to the Gemini API using the user's own API key (configured in Settings) —
 * only used when the on-device summarizer is unavailable and a key is present.
 */
class GeminiCloudSummarizer {

    suspend fun summarize(apiKey: String, notifications: List<NotificationEntity>): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = JSONObject().put(
                    "contents",
                    JSONArray().put(
                        JSONObject().put(
                            "parts",
                            JSONArray().put(JSONObject().put("text", buildPrompt(notifications))),
                        ),
                    ),
                )

                val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("x-goog-api-key", apiKey)
                }

                connection.outputStream.use { it.write(requestBody.toString().toByteArray(Charsets.UTF_8)) }

                val responseCode = connection.responseCode
                val responseBody = (if (responseCode in 200..299) connection.inputStream else connection.errorStream)
                    .bufferedReader()
                    .use { it.readText() }

                if (responseCode !in 200..299) throw GeminiApiException(responseCode, responseBody)

                JSONObject(responseBody)
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
            }
        }

    private fun buildPrompt(notifications: List<NotificationEntity>): String =
        "Resuma as notificações abaixo em até 3 tópicos curtos, em português, focando no que é " +
            "acionável:\n\n${buildNotificationSummaryInput(notifications)}"

    private companion object {
        // "-latest" is a Google-maintained alias that always points at the current Flash model,
        // so this doesn't go stale (and 404) as Google retires specific dated model versions.
        const val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent"
    }
}

class GeminiApiException(httpCode: Int, body: String) : Exception(
    buildString {
        append(
            when (httpCode) {
                401, 403 -> "Chave de API do Gemini inválida ou sem permissão"
                404 -> "Modelo do Gemini não encontrado para esta chave"
                429 -> "Limite de uso gratuito da API do Gemini atingido, tente novamente mais tarde"
                else -> "Erro ao chamar a API do Gemini (código $httpCode)"
            }
        )
        val detail = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
        if (!detail.isNullOrBlank()) append(": $detail")
    }
)
