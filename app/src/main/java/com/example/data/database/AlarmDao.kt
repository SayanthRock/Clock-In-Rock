package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    // Alarms Queries
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<Alarm>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long

    @Update
    suspend fun updateAlarm(alarm: Alarm)

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): Alarm?

    // World Clocks Queries
    @Query("SELECT * FROM world_clocks ORDER BY cityName ASC")
    fun getAllWorldClocks(): Flow<List<WorldClock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorldClock(worldClock: WorldClock)

    @Query("DELETE FROM world_clocks WHERE timezoneId = :timezoneId")
    suspend fun deleteWorldClockById(timezoneId: String)

    // Alarm Presets Queries
    @Query("SELECT * FROM alarm_presets ORDER BY id DESC")
    fun getAllAlarmPresets(): Flow<List<AlarmPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarmPreset(preset: AlarmPreset): Long

    @Update
    suspend fun updateAlarmPreset(preset: AlarmPreset)

    @Delete
    suspend fun deleteAlarmPreset(preset: AlarmPreset)

    @Query("UPDATE alarm_presets SET isActive = 0")
    suspend fun deactivateAllAlarmPresets()

    @Query("UPDATE alarm_presets SET isActive = 1 WHERE id = :id")
    suspend fun activateAlarmPreset(id: Int)

    // Helper queries to replace all active alarms when restoring a preset
    @Query("DELETE FROM alarms")
    suspend fun deleteAllAlarms()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAlarms(alarms: List<Alarm>)
}
