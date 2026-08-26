package com.leo.painelnotificacoes.notification

import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.service.notification.StatusBarNotification
import android.util.Log
import com.leo.painelnotificacoes.data.local.NotificationEntity
import java.io.File
import java.io.FileOutputStream

/** Converts a system [StatusBarNotification] into our persisted [NotificationEntity]. */
class NotificationMapper(private val context: Context) {

    private val packageManager = context.packageManager
    private val iconCacheDir: File by lazy {
        File(context.filesDir, "app_icons").apply { mkdirs() }
    }

    fun map(sbn: StatusBarNotification): NotificationEntity {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()
        return NotificationEntity(
            statusBarKey = sbn.key,
            packageName = sbn.packageName,
            appName = resolveAppName(sbn.packageName),
            appIconUri = cacheAppIcon(sbn.packageName),
            title = title,
            text = text,
            timestamp = sbn.postTime,
        )
    }

    private fun resolveAppName(packageName: String): String = try {
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(appInfo).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        packageName
    }

    /** Caches each app's icon to disk once, keyed by package name, and returns a file:// URI. */
    private fun cacheAppIcon(packageName: String): String? {
        val cachedFile = File(iconCacheDir, "$packageName.png")
        if (cachedFile.exists()) return Uri.fromFile(cachedFile).toString()
        return try {
            val bitmap = packageManager.getApplicationIcon(packageName).toBitmap()
            FileOutputStream(cachedFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Uri.fromFile(cachedFile).toString()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cache icon for $packageName", e)
            null
        }
    }

    private fun Drawable.toBitmap(): Bitmap {
        val width = intrinsicWidth.coerceAtLeast(1)
        val height = intrinsicHeight.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }

    companion object {
        private const val TAG = "NotificationMapper"
    }
}
