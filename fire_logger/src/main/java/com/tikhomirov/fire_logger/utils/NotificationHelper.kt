package com.tikhomirov.fire_logger.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tikhomirov.fire_logger.FireLoggerActivity
import com.tikhomirov.fire_logger.R
import com.tikhomirov.fire_logger.repository.DeviceIdRepository

internal object NotificationHelper {

    private const val CHANNEL_ID = "7854"
    private const val FIRE_LOGGER_CHANNEL = "fire_logger"
    private const val TRANSACTION_NOTIFICATION_ID = 1786
    private const val FIRE_LOGGER_TITLE = "FireLogger"
    private const val YOUR_SESSION = "Your session:"

    private lateinit var context: Context
    private lateinit var builder: NotificationCompat.Builder
    private val notificationManager by lazy { NotificationManagerCompat.from(context) }

    fun init(context: Context) {
        NotificationHelper.context = context

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        createNotification()
    }

    fun updateNotificationTitle() {
        val deviceId = DeviceIdRepository.getDeviceId()
        val sessionId = DeviceIdRepository.getSessionId()
        builder.setContentText("$YOUR_SESSION $sessionId - $deviceId")
        notificationManager.notify(TRANSACTION_NOTIFICATION_ID, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                FIRE_LOGGER_CHANNEL,
                NotificationManager.IMPORTANCE_LOW
            )
        )
    }

    private fun createNotification() {
        val deviceId = DeviceIdRepository.getDeviceId()
        val sessionId = DeviceIdRepository.getSessionId()
        builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    TRANSACTION_NOTIFICATION_ID,
                    getLaunchIntent(context),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setLocalOnly(true)
            .setOngoing(true)
            .setContentTitle(FIRE_LOGGER_TITLE)
            .setContentText("$YOUR_SESSION $sessionId - $deviceId")
            .setSmallIcon(R.drawable.ic_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        builder.setAutoCancel(false)

        notificationManager.notify(
            TRANSACTION_NOTIFICATION_ID,
            builder.build()
        )
    }

    private fun getLaunchIntent(context: Context): Intent {
        return Intent(context, FireLoggerActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }


}