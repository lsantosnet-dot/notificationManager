package com.leo.painelnotificacoes.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat

/** Whether the user has granted this app the special notification-listener permission. */
fun isNotificationAccessGranted(context: Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
