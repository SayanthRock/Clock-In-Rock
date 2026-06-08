package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "Alarm",
    val isEnabled: Boolean = true,
    val daysOfWeek: String = "0000000", // 1 or 0 for each day: Mon, Tue, Wed, Thu, Fri, Sat, Sun
    // Sound customizer synth variables
    val waveType: String = "SINE", // SINE, SQUARE, TRIANGLE, SAWTOOTH
    val frequency: Float = 600f,
    val pulseSpeedMs: Long = 400L,
    val vibratoDepth: Float = 0.15f,
    val vibratoSpeed: Float = 6.0f,
    val isVibrationEnabled: Boolean = true
) {
    fun isRepeating(): Boolean = daysOfWeek.contains("1")
    
    fun getFormattedTime(is24Hour: Boolean): String {
        return if (is24Hour) {
            String.format("%02d:%02d", hour, minute)
        } else {
            val amPm = if (hour >= 12) "PM" else "AM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            String.format("%d:%02d %s", displayHour, minute, amPm)
        }
    }
}

@Entity(tableName = "world_clocks")
data class WorldClock(
    @PrimaryKey val timezoneId: String, // e.g. "UTC", "Asia/Kolkata", "America/New_York"
    val cityName: String,
    val countryName: String
)

@Entity(tableName = "alarm_presets")
data class AlarmPreset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val alarmsJson: String, // Serialized list of Alarms
    val isActive: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

