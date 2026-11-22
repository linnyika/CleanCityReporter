package com.group2.cleancity.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.group2.cleancity.model.Category
import com.group2.cleancity.model.Priority

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val defaultPriority: String,
    val isActive: Boolean,
    val description: String
) {
    fun toModel(): Category = Category(
        id = id,
        name = name,
        defaultPriority = Priority.valueOf(defaultPriority),
        isActive = isActive,
        description = description
    )

    companion object {
        fun toEntity(category: Category): CategoryEntity = CategoryEntity(
            id = category.id,
            name = category.name,
            defaultPriority = category.defaultPriority.name,
            isActive = category.isActive,
            description = category.description
        )
    }
}
