package com.group2.cleancity.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AppNotificationDao {
    @Query("SELECT * FROM notifications")
    fun getAll(): List<AppNotificationEntity>

    @Query("SELECT * FROM notifications WHERE id = :id")
    fun getById(id: Int): AppNotificationEntity?

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getForUser(userId: Int): List<AppNotificationEntity>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND isPosted = 0 ORDER BY timestamp ASC")
    fun getUnposted(userId: Int): List<AppNotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun unreadCount(userId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(notification: AppNotificationEntity)

    @Update
    fun update(notification: AppNotificationEntity)

    @Delete
    fun delete(notification: AppNotificationEntity)
}
