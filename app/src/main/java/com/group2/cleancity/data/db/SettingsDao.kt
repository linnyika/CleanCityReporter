package com.group2.cleancity.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingsDao {
    @Query("SELECT value FROM settings WHERE setting_key = :settingKey")
    fun getOverdueThreshold(settingKey: String = "overdue_threshold"): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(setting: AppSettingsEntity)
}
