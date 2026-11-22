package com.group2.cleancity.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports")
    fun getAll(): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE id = :id")
    fun getById(id: String): ReportEntity?

    @Query("SELECT * FROM reports WHERE reporterId = :reporterId")
    fun getByReporter(reporterId: Int): List<ReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(report: ReportEntity)

    @Update
    fun update(report: ReportEntity)

    @Delete
    fun delete(report: ReportEntity)
}
