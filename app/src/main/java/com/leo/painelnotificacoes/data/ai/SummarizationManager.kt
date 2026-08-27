package com.leo.painelnotificacoes.data.ai

import android.content.Context
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizationRequest
import com.google.mlkit.genai.summarization.Summarizer
import com.google.mlkit.genai.summarization.SummarizerOptions
import com.leo.painelnotificacoes.data.local.NotificationEntity
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Thin coroutine wrapper around the ML Kit GenAI Summarization API (Gemini Nano, fully on-device —
 * no notification content ever leaves the phone).
 *
 * The GenAI APIs are still in beta at the time of writing; class/method names below follow
 * https://developers.google.com/ml-kit/genai/summarization/android as of this writing. If a
 * future artifact bump renames anything, this is the only file that needs to change.
 */
class SummarizationManager(private val context: Context) {

    /** Cheap check for the "hide the AI card entirely" case (e.g. non-Pixel/Galaxy AI hardware). */
    suspend fun isFeaturePossible(): Boolean = withSummarizer { summarizer ->
        summarizer.checkFeatureStatus().await() != FeatureStatus.UNAVAILABLE
    }.getOrDefault(false)

    suspend fun summarizeGroup(notifications: List<NotificationEntity>): Result<String> {
        if (notifications.isEmpty()) return Result.failure(IllegalArgumentException("Nothing to summarize"))
        return withSummarizer { summarizer ->
            val status = summarizer.checkFeatureStatus().await()
            if (status == FeatureStatus.UNAVAILABLE) {
                throw SummarizationUnavailableException()
            }
            if (status == FeatureStatus.DOWNLOADABLE || status == FeatureStatus.DOWNLOADING) {
                awaitDownload(summarizer)
            }
            val request = SummarizationRequest.builder(buildNotificationSummaryInput(notifications)).build()
            summarizer.runInference(request).await().summary
        }
    }

    private suspend fun <T> withSummarizer(block: suspend (Summarizer) -> T): Result<T> {
        val summarizer = Summarization.getClient(
            SummarizerOptions.builder(context)
                .setInputType(SummarizerOptions.InputType.CONVERSATION)
                .setOutputType(SummarizerOptions.OutputType.THREE_BULLETS)
                .build()
        )
        return try {
            Result.success(block(summarizer))
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            summarizer.close()
        }
    }

    private suspend fun awaitDownload(summarizer: Summarizer): Unit = suspendCancellableCoroutine { cont ->
        summarizer.downloadFeature(object : DownloadCallback {
            override fun onDownloadStarted(bytesToDownload: Long) = Unit

            override fun onDownloadProgress(bytesDownloaded: Long) = Unit

            override fun onDownloadCompleted() {
                if (cont.isActive) cont.resume(Unit)
            }

            override fun onDownloadFailed(e: GenAiException) {
                if (cont.isActive) cont.resumeWithException(e)
            }
        })
    }
}

class SummarizationUnavailableException :
    Exception("O resumo por IA local não está disponível neste dispositivo")
