package com.example.localization

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R

object LocalizedNotificationManager {

    fun showLocalizedNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        titleResId: Int,
        contentResId: Int
    ) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Need to make sure this exists
            .setContentTitle(context.getString(titleResId))
            .setContentText(context.getString(contentResId))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            // Need runtime permission for POST_NOTIFICATIONS on Android 13+
            notify(notificationId, builder.build())
        }
    }
}
