package com.group2.cleancity.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAll(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id")
    fun getById(id: Int): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone")
    fun findByPhone(phone: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: UserEntity)

    @Update
    fun update(user: UserEntity)

    @Delete
    fun delete(user: UserEntity)
}
