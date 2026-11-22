package com.group2.cleancity.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class AppSettingsEntity(
    @PrimaryKey @ColumnInfo(name = "setting_key") val key: String = "overdue_threshold",
    @ColumnInfo(name = "value") val value: Int
)
