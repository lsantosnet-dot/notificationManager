package com.leo.painelnotificacoes.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.leo.painelnotificacoes.PainelNotificacoesApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NotificationCaptureService : NotificationListenerService() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private val repository by lazy { (application as PainelNotificacoesApp).container.notificationRepository }
    private val mapper by lazy { NotificationMapper(applicationContext) }

    /**
     * Runs whenever the listener (re)binds — including after the app or system UI restarts.
     * The notification shade may already hold notifications posted while we were disconnected,
     * so we scan [getActiveNotifications] and backfill anything not already in Room.
     */
    override fun onListenerConnected() {
        super.onListenerConnected()
        serviceScope.launch {
            runCatching { catchUp() }
                .onFailure { Log.e(TAG, "Catch-up failed", it) }
        }
    }

    private suspend fun catchUp() {
        val active = activeNotifications ?: return
        val knownKeys = repository.knownActiveKeys()
        active
            .filter { it.key !in knownKeys && shouldCapture(it) }
            .forEach { repository.captureNotification(mapper.map(it)) }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!shouldCapture(sbn)) return
        // Mapping touches disk (icon cache) and Room, so hop off the listener's thread.
        serviceScope.launch {
            runCatching { repository.captureNotification(mapper.map(sbn)) }
                .onFailure { Log.e(TAG, "Failed to persist notification", it) }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Intentional no-op: this app's purpose is to keep a local history of notifications
        // after the system tray clears them. Deletion only happens from the UI (user action)
        // or the retention worker — never as a side effect of the system dismissing a toast.
    }

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }

    /** Filters out grouped "N new messages" summary shells and empty/system notifications. */
    private fun shouldCapture(sbn: StatusBarNotification): Boolean {
        val notification = sbn.notification
        if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return false
        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)
        return !title.isNullOrBlank() || !text.isNullOrBlank()
    }

    companion object {
        private const val TAG = "NotificationCapture"
    }
}
