package com.defname.unlimitedondemandautoreply


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlin.random.Random

class MyNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val smsApp = prefs.getString("sms_app", "")
        val titleMatch = prefs.getString("title_match", "")
        val bodyMatch = prefs.getString("body_match", "")
        val number = prefs.getString("number", "")
        val answer = prefs.getString("answer", "")
        val minDelay = prefs.getString("min_delay", "5")?.toLong() ?: 5
        val maxDelay = prefs.getString("max_delay", "30")?.toLong() ?: 30
        val delay = Random.nextLong(minDelay*1000 , maxDelay*1000)

        if (packageName.equals(smsApp) && (title != null && title.contains(titleMatch.toString()))
            && (text != null && text.contains(bodyMatch.toString()))) {
            Log.d("NotifListener", "Notification found")
            Log.d("NotifListener", "from: $packageName")
            Log.d("NotifListener", "title: $title")
            Log.d("NotifListener", "text: $text")

            showNotification(getString(R.string.notification_title_matched), "from: ${sbn.packageName}\ntitle: ${title}\nwaiting for ${delay/1000}s before replying")

            Handler(Looper.getMainLooper()).postDelayed({
                sendSMS(number.toString(), answer.toString())
                showNotification(getString(R.string.notification_title_send), "Automatic answer \"${answer}\" send to ${number} after ${delay/1000}s")
            }, delay)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {

    }

    private fun showNotification(title: String, message: String) {
        val channelId = "unlimitedondemandautoreply_notification_channel"
        val channelName = "UnlimitedOnDemand Auto Reply"

        // check permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // Channel erstellen (nur nötig ab API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications from UnlimitedOnDemand Auto Reply"
            }

            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Notification erstellen
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Verwende ein eigenes Icon in produktiver App
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Notification anzeigen
        with(NotificationManagerCompat.from(applicationContext)) {

            notify(1, builder.build()) // ID = 1, kann beliebig gewählt werden
        }
    }

    private fun sendSMS(number: String, msg: String) {
        val phone = number.filter { it.isDigit() || it == '+' }
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phone, null, msg, null, null)
    }
}