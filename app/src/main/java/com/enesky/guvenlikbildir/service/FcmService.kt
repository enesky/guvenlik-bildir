package com.enesky.guvenlikbildir.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // TODO(developer): Handle FCM messages here.

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null)
            Log.d("FCM", "Message Notification Body: " + remoteMessage.notification!!.body)

    }

}
