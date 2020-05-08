package com.enesky.guvenlikbildir.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.ui.activity.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class FcmService : FirebaseMessagingService() {

    private var notificationId = 0

    override fun onNewToken(p0: String) = super.onNewToken(p0)

    /**
     * Called when message is received and application is running.
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.notification != null) {
            Timber.tag("FCM").d("Message Notification Body: %s", remoteMessage.notification!!.body)

            var earthquakeID: String? = ""

            if (remoteMessage.data.containsKey(Constants.NOTIFICATION_EARTHQUAKE_ID))
                earthquakeID = remoteMessage.data[Constants.NOTIFICATION_EARTHQUAKE_ID]

            showLocalNotification(
                remoteMessage.notification!!.title!!,
                remoteMessage.notification!!.body!!,
                earthquakeID
                )
        }

    }

    private fun showLocalNotification(
        title: String,
        message: String,
        earthquakeID: String?
    ) {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        if (!earthquakeID.isNullOrEmpty())
            intent.putExtra(Constants.NOTIFICATION_EARTHQUAKE_ID, earthquakeID)

        val pIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, Constants.appName)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                .setColor(Color.GRAY)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId++, notification.build())
    }

}
