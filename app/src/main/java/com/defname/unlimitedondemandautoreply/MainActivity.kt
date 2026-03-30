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

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Telephony
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.defname.unlimitedondemandautoreply.ui.theme.SmsTestAppTheme

private const val REQUEST_CODE_POST_NOTIFICATIONS = 1001

/**
 * MainActivity for the app.
 * Display the status of the permissions with a button to request them.
 * Display the status of the notification listener with a button to enable it.
 * Configure the settings for the app.
 */
class MainActivity : ComponentActivity() {
    /**
     * initialize all ui event handler
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmsTestAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(this)
                }
            }
        }
    }

    fun checkSMSPermissions(): Boolean {
        return checkSelfPermission(android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }

    fun requestSMSPermissions() {
        Log.d("MainActivity", "request permissions")
        requestPermissions(arrayOf(android.Manifest.permission.SEND_SMS), PackageManager.PERMISSION_GRANTED)
    }

    fun checkNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Log.d("SmsTestApp", "SDK < Tiramisu")
            return true
        }

        return (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
        }
    }

    fun checkNotificationServiceEnabled(): Boolean {
        val cn = ComponentName(this, MyNotificationListenerService::class.java)
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(cn.flattenToString()) == true
    }

    fun requestNotificationService() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    fun saveSetting(key: String, value: String) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        prefs.edit() { putString(key, value) }
    }

    fun getSetting(key: String): String {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        return prefs.getString(key, "") ?: ""
    }
}

@Composable
fun SettingsScreen(
    onSaveSetting: (String, String) -> Unit,
    onGetSetting: (String) -> String,
    onRequestSMSPermissions: () -> Unit,
    checkSMSPermissions: () -> Boolean,
    onRequestNotificationPermission: () -> Unit,
    checkNotificationPermission: () -> Boolean,
    onRequestNotificationService: () -> Unit,
    checkNotificationServiceEnabled: () -> Boolean,
    getDefaultSmsPackage: () -> String?
) {
    var smsPermissionGranted by remember { mutableStateOf(checkSMSPermissions()) }
    var notificationPermissionGranted by remember { mutableStateOf(checkNotificationPermission()) }
    var notificationServiceEnabled by remember { mutableStateOf(checkNotificationServiceEnabled()) }

    var smsAppPackage by remember { mutableStateOf(onGetSetting("sms_app")) }
    var titleMatch by remember { mutableStateOf(onGetSetting("title_match")) }
    var bodyMatch by remember { mutableStateOf(onGetSetting("body_match")) }
    var number by remember { mutableStateOf(onGetSetting("number")) }
    var answer by remember { mutableStateOf(onGetSetting("answer")) }
    var minDelay by remember { mutableStateOf(onGetSetting("min_delay")) }
    var maxDelay by remember { mutableStateOf(onGetSetting("max_delay")) }

    // Aktualisieren Sie den Status, wenn die Composable-Funktion neu zusammengesetzt wird (z. B. nach onResume)
    LaunchedEffect(Unit) {
        smsPermissionGranted = checkSMSPermissions()
        notificationPermissionGranted = checkNotificationPermission()
        notificationServiceEnabled = checkNotificationServiceEnabled()
        // Laden Sie die Einstellungen neu, falls sie extern geändert wurden
        smsAppPackage = onGetSetting("sms_app")
        titleMatch = onGetSetting("title_match")
        bodyMatch = onGetSetting("body_match")
        number = onGetSetting("number")
        answer = onGetSetting("answer")
        minDelay = onGetSetting("min_delay")
        maxDelay = onGetSetting("max_delay")
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // SMS Permission Status
            Text(
                text = if (smsPermissionGranted) stringResource(R.string.sms_permission_granted)
                else stringResource(R.string.sms_permission_denied),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            androidx.compose.material3.Button(
                onClick = {
                    onRequestSMSPermissions()
                },
                enabled = !smsPermissionGranted
            ) {
                Text(stringResource(R.string.request_sms_permission_btn))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notification Permission Status
            Text(
                text = if (notificationPermissionGranted) stringResource(R.string.notification_permission_granted)
                else stringResource(R.string.notification_permission_denied),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            androidx.compose.material3.Button(
                onClick = {
                    onRequestNotificationPermission()
                },
                enabled = !notificationPermissionGranted
            ) {
                Text(stringResource(R.string.request_notification_permission_btn))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notification Listener Status
            Text(
                text = if (notificationServiceEnabled) stringResource(R.string.notification_listener_active)
                else stringResource(R.string.notification_listener_inactive),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            androidx.compose.material3.Button(onClick = {
                onRequestNotificationService()
            }) {
                Text(stringResource(R.string.enable_notification_listener_btn))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input: App Package
            OutlinedTextField(
                value = smsAppPackage,
                onValueChange = {
                    smsAppPackage = it
                    onSaveSetting("sms_app", it)
                },
                label = { Text(stringResource(R.string.edit_app_package_caption)) },
                placeholder = { Text(stringResource(R.string.edit_app_package_hint)) },
                modifier = Modifier.fillMaxWidth()
            )
            androidx.compose.material3.Button(onClick = {
                getDefaultSmsPackage()?.let {
                    smsAppPackage = it
                    onSaveSetting("sms_app", it)
                }
            }) {
                Text(stringResource(R.string.button_sms_app_caption))
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Input: Title to match
            OutlinedTextField(
                value = titleMatch,
                onValueChange = {
                    titleMatch = it
                    onSaveSetting("title_match", it)
                },
                label = { Text(stringResource(R.string.edit_title_caption)) },
                placeholder = { Text(stringResource(R.string.edit_title_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input: Content to match
            OutlinedTextField(
                value = bodyMatch,
                onValueChange = {
                    bodyMatch = it
                    onSaveSetting("body_match", it)
                },
                label = { Text(stringResource(R.string.edit_content_caption)) },
                placeholder = { Text(stringResource(R.string.edit_content_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input: Number
            OutlinedTextField(
                value = number,
                onValueChange = {
                    number = it
                    onSaveSetting("number", it)
                },
                label = { Text(stringResource(R.string.edit_number_caption)) },
                placeholder = { Text(stringResource(R.string.edit_number_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input: Answer
            OutlinedTextField(
                value = answer,
                onValueChange = {
                    answer = it
                    onSaveSetting("answer", it)
                },
                label = { Text(stringResource(R.string.edit_answer_caption)) },
                placeholder = { Text(stringResource(R.string.edit_answer_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.edit_delay_caption), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 16.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = minDelay,
                    onValueChange = {
                        minDelay = it
                        onSaveSetting("min_delay", it)
                    },
                    label = { Text("Min (s)") },
                    placeholder = { Text("5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                OutlinedTextField(
                    value = maxDelay,
                    onValueChange = {
                        maxDelay = it
                        onSaveSetting("max_delay", it)
                    },
                    label = { Text("Max (s)") },
                    placeholder = { Text("30") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MainScreen(activity: MainActivity) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Einstellungen", "Logs")

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Nutzt Safe Drawing Insets, um Notch/Kamera auszuweichen
            .padding(WindowInsets.safeDrawing.asPaddingValues())
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> SettingsScreen(
                onSaveSetting = activity::saveSetting,
                onGetSetting = activity::getSetting,
                onRequestSMSPermissions = activity::requestSMSPermissions,
                checkSMSPermissions = activity::checkSMSPermissions,
                onRequestNotificationPermission = activity::requestNotificationPermission,
                checkNotificationPermission = activity::checkNotificationPermission,
                onRequestNotificationService = activity::requestNotificationService,
                checkNotificationServiceEnabled = activity::checkNotificationServiceEnabled,
                getDefaultSmsPackage = { Telephony.Sms.getDefaultSmsPackage(activity) }
            ) // Deine bisherige UI
            1 -> LogScreen()      // Die neue Log-Ansicht
        }
    }
}

@Composable
fun LogScreen() {
    val logs = LogManager.logs

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(logs) { entry ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "[${entry.timestamp}]",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entry.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Divider(color = Color.LightGray, thickness = 0.5.dp)
        }
    }
}

