package com.enesky.guvenlikbildir.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.Html
import androidx.core.app.NotificationCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.extensions.formatDateTime
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.ui.activity.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class FcmService : FirebaseMessagingService() {

    companion object {

        fun showLocalNotification(
            context: Context,
            title: String = "",
            message: String = "",
            earthquakeID: String? = "",
            earthquake: Earthquake? = null
        ) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (!earthquakeID.isNullOrEmpty())
                intent.putExtra(Constants.NOTIFICATION_EARTHQUAKE_ID, earthquakeID)

            val pIntent = PendingIntent.getActivity(
                context.applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notificationBuilder = NotificationCompat.Builder(context, Constants.appName)
                .setContentTitle(title)
                .setContentText(message) // Content text is ignored when big text is set at BigTextStyle!
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_round))
                .setContentIntent(pIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setColor(Color.GRAY)
                .setAutoCancel(true)

            if (earthquake != null) {
                val contentTitle = "Deprem oldu! <u>(${earthquake.dateTime.formatDateTime()})</u>"
                val desc = "<b>${earthquake.location}</b> bölgesinde <b><u>${earthquake.magML}</u></b> büyüklüğünde deprem oldu."
                val styledDesc = Html.fromHtml(desc, FROM_HTML_MODE_LEGACY)

                val bigTextStyle = NotificationCompat.BigTextStyle()
                    .setBigContentTitle(contentTitle)
                    .bigText(styledDesc)
                    .setSummaryText("Anlık Deprem Bildirimi")

                notificationBuilder.setContentTitle(contentTitle)
                notificationBuilder.setStyle(bigTextStyle)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = notificationBuilder.build()
            val notificationId = System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, notification)
        }

    }

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
                context =  this,
                title = remoteMessage.notification!!.title!!,
                message = remoteMessage.notification!!.body!!,
                earthquakeID = earthquakeID
            )
        }
    }

}
