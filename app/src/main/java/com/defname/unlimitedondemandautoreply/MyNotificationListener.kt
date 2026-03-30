/*
 * Copyright (C) 2025 defname
 *
 * This file is part of UnlimitedOnDemand Auto Reply.
 *
 * UnlimitedOnDemand_Auto_Reply is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnlimitedOnDemand Auto Reply is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnlimitedOnDemand_Auto_Reply. If not, see <https://www.gnu.org/licenses/>.
 */

package com.defname.unlimitedondemandautoreply


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlin.random.Random

/**
 * NotificationService that runs in the background and listens for incoming notifications.
 * If a notification matches the specified criteria, it will send an SMS to the specified number
 * after a random delay.
 */
class MyNotificationListenerService : NotificationListenerService() {
    /**
     * Handle incoming notifications.
     */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // extract the information of the notification
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")

        // read the settings from SharedPreferences
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val smsApp = prefs.getString("sms_app", "")
        val titleMatch = prefs.getString("title_match", "")
        val bodyMatch = prefs.getString("body_match", "")
        val number = prefs.getString("number", "")
        val answer = prefs.getString("answer", "")
        val minDelay = prefs.getString("min_delay", "5")?.toLongOrNull() ?: 5
        val maxDelay = prefs.getString("max_delay", "30")?.toLongOrNull() ?: 30

        // calculate a random delay within the specified range (ensure max > min)
        val minMillis = minDelay * 1000
        val maxMillis = maxOf(minMillis + 1000, maxDelay * 1000)
        val delay = Random.nextLong(minMillis, maxMillis)

        Log.d("NotifListener", "onNotificationPosted")
        Log.d("NotifListener", "from: $packageName")
        Log.d("NotifListener", "title: $title")
        Log.d("NotifListener", "text: $text")

        // check if the notification matches the specified criteria
        if (packageName.equals(smsApp) && (title != null && title.contains(titleMatch ?: ""))
            && (text != null && text.contains(bodyMatch.toString()))) {
            Log.d("NotifListener", "Notification matched")
            LogManager.addLog("Notification matched. Waiting for ${delay/1000}s...")

            // send the SMS after the specified delay
            Handler(Looper.getMainLooper()).postDelayed({
                sendSMS(number.toString(), answer.toString())
            }, delay)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
    }

    /**
     * Show a notification with the given title and message.
     */
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

        // Create channel (>= API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications from UnlimitedOnDemand Auto Reply"
            }

            val notificationManager: NotificationManager =
                applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // create notification
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Verwende ein eigenes Icon in produktiver App
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // post notification
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    /**
     * Send an SMS to the given number with the given message.
     */
    private fun sendSMS(number: String, msg: String) {
        try {
            val phone = number.filter { it.isDigit() || it == '+' }

            if (phone.isEmpty()) {
                throw IllegalArgumentException("Phone number is empty after filtering")
            }

            val smsManager = getSystemService(SmsManager::class.java)
            if (smsManager == null) {
                throw IllegalStateException("SmsManager service not available")
            }

            val intent = android.content.Intent("SMS_SENT")
            intent.setPackage(packageName)

            val sentIntent = android.app.PendingIntent.getBroadcast(
                this,
                0,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    android.app.PendingIntent.FLAG_IMMUTABLE
                else 0
            )

            smsManager.sendTextMessage(phone, null, msg, sentIntent, null)
            Log.d("NotifListener", "SMS process started for $phone")

        } catch (e: Exception) {
            Log.e("NotifListener", "Error sending SMS: ${e.message}")
            // Fehler als Toast (Meldung unten) anzeigen
            Toast.makeText(this, "SMS Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            LogManager.addLog("SMS Error: ${e.localizedMessage}")
        }
    }

    private val smsStatusReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            val code = resultCode
            val message = when (code) {
                android.app.Activity.RESULT_OK -> "SMS sent successfully!"
                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "Error: Generic failure (maybe no balance?)"
                SmsManager.RESULT_ERROR_NO_SERVICE -> "Error: No service (no network)"
                SmsManager.RESULT_ERROR_NULL_PDU -> "Error: PDU empty"
                SmsManager.RESULT_ERROR_RADIO_OFF -> "Error: Airplane mode active"
                // This case usually happens if permission is denied or revoked
                else -> "SMS delivery failed (Code: $code). Permission missing?"
            }

            Log.d("NotifListener", "Receiver Result: $code -> $message")
            LogManager.addLog(message)
            // Optional: Benachrichtigung anzeigen
            showNotification("SMS Status", message)
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Den Receiver beim Start des Service registrieren
        val filter = android.content.IntentFilter("SMS_SENT")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(smsStatusReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(smsStatusReceiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Wichtig: Wieder abmelden
        unregisterReceiver(smsStatusReceiver)
    }
}
