package com.example.data.database

import kotlinx.coroutines.flow.Flow

class AlarmRepository(private val alarmDao: AlarmDao) {
    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()
    val allWorldClocks: Flow<List<WorldClock>> = alarmDao.getAllWorldClocks()

    suspend fun insertAlarm(alarm: Alarm): Long = alarmDao.insertAlarm(alarm)
    suspend fun updateAlarm(alarm: Alarm) = alarmDao.updateAlarm(alarm)
    suspend fun deleteAlarm(alarm: Alarm) = alarmDao.deleteAlarm(alarm)
    suspend fun getAlarmById(id: Int): Alarm? = alarmDao.getAlarmById(id)

    suspend fun insertWorldClock(worldClock: WorldClock) = alarmDao.insertWorldClock(worldClock)
    suspend fun deleteWorldClockById(timezoneId: String) = alarmDao.deleteWorldClockById(timezoneId)

    // Alarm Presets
    val allAlarmPresets: Flow<List<AlarmPreset>> = alarmDao.getAllAlarmPresets()

    suspend fun insertAlarmPreset(preset: AlarmPreset): Long = alarmDao.insertAlarmPreset(preset)
    suspend fun updateAlarmPreset(preset: AlarmPreset) = alarmDao.updateAlarmPreset(preset)
    suspend fun deleteAlarmPreset(preset: AlarmPreset) = alarmDao.deleteAlarmPreset(preset)
    
    suspend fun togglePresetActive(id: Int) {
        alarmDao.deactivateAllAlarmPresets()
        alarmDao.activateAlarmPreset(id)
    }

    suspend fun deactivateAllPresets() = alarmDao.deactivateAllAlarmPresets()

    suspend fun replaceAlarms(alarms: List<Alarm>) {
        alarmDao.deleteAllAlarms()
        alarmDao.insertAllAlarms(alarms)
    }
}
