package com.defname.unlimitedondemandautoreply

import androidx.compose.runtime.mutableStateListOf

object LogManager {
    // Eine Liste, die automatisch die UI aktualisiert, wenn neue Einträge dazukommen
    val logs = mutableStateListOf<LogEntry>()

    data class LogEntry(
        val timestamp: String,
        val message: String
    )

    fun addLog(message: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        // Wir fügen neue Logs oben an (index 0)
        logs.add(0, LogEntry(time, message))

        // Optional: Begrenze die Liste auf z.B. 100 Einträge, um Speicher zu sparen
        if (logs.size > 100) logs.removeAt(logs.lastIndex)
    }
}