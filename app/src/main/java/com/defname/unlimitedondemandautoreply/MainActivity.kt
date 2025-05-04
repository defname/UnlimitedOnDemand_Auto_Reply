package com.defname.unlimitedondemandautoreply

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Telephony
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.widget.doOnTextChanged
import com.defname.unlimitedondemandautoreply.ui.theme.SmsTestAppTheme

private const val REQUEST_CODE_POST_NOTIFICATIONS = 1001

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)

        val requestSMSPermissionBtn = findViewById<Button>(R.id.button_request_sms_permission)
        requestSMSPermissionBtn.setOnClickListener {
            requestSMSPermissions()
        }

        val requestNotificationPermissionBtn = findViewById<Button>(R.id.button_request_notification_permission)
        requestNotificationPermissionBtn.setOnClickListener {
            requestNotificationPermission()
        }

        val enableListenerServiceBtn = findViewById<Button>(R.id.button_enable_notification_listener)
        enableListenerServiceBtn.setOnClickListener {
            requestNotificationService()
        }

        val useStandardSmsAppBtn = findViewById<Button>(R.id.button_use_standard_sms_app)
        useStandardSmsAppBtn.setOnClickListener {
            val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this)
            findViewById<EditText>(R.id.edit_app_package).setText(defaultSmsPackage)
        }

        val editSmsAppInput = findViewById<EditText>(R.id.edit_app_package)
        editSmsAppInput.doOnTextChanged { text, start, before, count ->
            saveSetting("sms_app", text.toString())
        }

        val editTitle = findViewById<EditText>(R.id.edit_title)
        editTitle.doOnTextChanged { text, start, before, count ->
            saveSetting("title_match", text.toString())
        }

        val editBody = findViewById<EditText>(R.id.edit_content)
        editBody.doOnTextChanged { text, start, before, count ->
            saveSetting("body_match", text.toString())
        }

        val editNumber = findViewById<EditText>(R.id.edit_number)
        editNumber.doOnTextChanged { text, start, before, count ->
            saveSetting("number", text.toString())
        }

        val editAnswer = findViewById<EditText>(R.id.edit_answer)
        editAnswer.doOnTextChanged { text, start, before, count ->
            saveSetting("answer", text.toString())
        }

        val editMinDelay = findViewById<EditText>(R.id.edit_min_delay)
        editMinDelay.doOnTextChanged { text, start, before, count ->
            saveSetting("min_delay", text.toString())
        }

        val editMaxDelay = findViewById<EditText>(R.id.edit_max_delay)
        editMaxDelay.doOnTextChanged { text, start, before, count ->
            saveSetting("max_delay", text.toString())
        }
    }

    override fun onResume() {
        super.onResume()

        updateSMSPermissionIndicator()
        updateNotificationInidicator()
        updateNotificationServiceIndicator()

        updateTextInputsFromPreferences()
    }

    private fun checkSMSPermissions(): Boolean {
        return checkSelfPermission(android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSMSPermissions() {
        Log.d("MainActivity", "request permissions")
        requestPermissions(arrayOf(android.Manifest.permission.SEND_SMS), PackageManager.PERMISSION_GRANTED)
    }

    private fun updateSMSPermissionIndicator() {
        val permissionStatusTextView = findViewById<TextView>(R.id.sms_permission_status)
        val requestPermissionBtn = findViewById<Button>(R.id.button_request_sms_permission)
        if (checkSMSPermissions()) {
            permissionStatusTextView.text = getString(R.string.sms_permission_granted)
            requestPermissionBtn.setEnabled(false)
        }
        else {
            permissionStatusTextView.text = getString(R.string.sms_permission_denied)
        }
    }

    private fun checkNotificationPermission(): Boolean {

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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
        }
    }

    private fun updateNotificationInidicator() {
        val notificationStatusTextView = findViewById<TextView>(R.id.notification_permission_status)
        val requestPermissionBtn = findViewById<Button>(R.id.button_request_notification_permission)
        if (checkNotificationPermission()) {
            notificationStatusTextView.text = getString(R.string.notification_permission_granted)
            requestPermissionBtn.setEnabled(false)
        }
        else {
            notificationStatusTextView.text = getString(R.string.notification_permission_denied)
        }
    }

    private fun checkNotificationServiceEnabled(): Boolean {
        val cn = ComponentName(this, MyNotificationListenerService::class.java)
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(cn.flattenToString()) == true
    }

    private fun requestNotificationService() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun updateNotificationServiceIndicator() {
        val notificationServiceIndicatorTextView = findViewById<TextView>(R.id.notification_listener_status)

        if (checkNotificationServiceEnabled()) {
            notificationServiceIndicatorTextView.text = getString(R.string.notification_listener_active)
        }
        else {
            notificationServiceIndicatorTextView.text = getString(R.string.notification_listener_inactive)
        }
    }

    private fun updateTextInputsFromPreferences() {
        findViewById<EditText>(R.id.edit_app_package).setText(getSetting("sms_app"))
        findViewById<EditText>(R.id.edit_title).setText(getSetting("title_match"))
        findViewById<EditText>(R.id.edit_content).setText(getSetting("body_match"))
        findViewById<EditText>(R.id.edit_number).setText(getSetting("number"))
        findViewById<EditText>(R.id.edit_answer).setText(getSetting("answer"))
        findViewById<EditText>(R.id.edit_min_delay).setText(getSetting("min_delay"))
        findViewById<EditText>(R.id.edit_max_delay).setText(getSetting("max_delay"))
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
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SmsTestAppTheme {
        Greeting("Android")
    }
}