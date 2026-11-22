package com.group2.cleancity.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface StatusChangeDao {
    @Query("SELECT * FROM status_changes")
    fun getAll(): List<StatusChangeEntity>

    @Query("SELECT * FROM status_changes WHERE reportId = :reportId")
    fun getByReportId(reportId: String): List<StatusChangeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(statusChange: StatusChangeEntity)

    @Update
    fun update(statusChange: StatusChangeEntity)

    @Delete
    fun delete(statusChange: StatusChangeEntity)
}
