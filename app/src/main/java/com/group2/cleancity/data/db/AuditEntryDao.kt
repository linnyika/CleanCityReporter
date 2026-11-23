package com.group2.cleancity.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AuditEntryDao {
    @Query("SELECT * FROM audit_entries ORDER BY timestamp DESC")
    fun getAll(): List<AuditEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entry: AuditEntryEntity)

    @Delete
    fun delete(entry: AuditEntryEntity)
}
