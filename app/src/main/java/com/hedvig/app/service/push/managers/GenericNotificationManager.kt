package com.hedvig.app.service.push.managers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.R
import com.hedvig.app.SplashActivity
import com.hedvig.app.service.push.setupNotificationChannel
import java.util.concurrent.atomic.AtomicInteger

object GenericNotificationManager {
    fun sendGenericNotification(context: Context, remoteMessage: RemoteMessage) {
        createChannel(context)

        val pendingIntent = TaskStackBuilder
            .create(context)
            .run {
                addNextIntentWithParentStack(
                    Intent(
                        context,
                        SplashActivity::class.java
                    )
                )
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        val notification = NotificationCompat
            .Builder(
                context,
                GENERIC_CHANNEL_ID
            )
            .setSmallIcon(R.drawable.ic_hedvig_symbol_android)
            .setContentTitle("TODO Copy")
            .setContentText("TODO Copy")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setChannelId(GENERIC_CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(
                id.getAndIncrement(),
                notification
            )
    }

    private fun createChannel(context: Context) {
        setupNotificationChannel(
            context,
            GENERIC_CHANNEL_ID,
            "TODO Copy",
            "TODO Copy"
        )
    }

    private val id = AtomicInteger(100)

    private const val GENERIC_CHANNEL_ID = "hedvig-generic"
}
