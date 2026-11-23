package com.group2.cleancity.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getById(id: Int): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(category: CategoryEntity)

    @Update
    fun update(category: CategoryEntity)

    @Delete
    fun delete(category: CategoryEntity)
}
