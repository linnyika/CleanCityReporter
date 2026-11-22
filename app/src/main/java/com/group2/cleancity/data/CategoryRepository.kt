package com.group2.cleancity.data

import android.content.Context
import com.group2.cleancity.data.db.AppDatabase
import com.group2.cleancity.data.db.CategoryDao
import com.group2.cleancity.data.db.CategoryEntity
import com.group2.cleancity.model.Category
import com.group2.cleancity.model.Priority

/**
 * Singleton repository for managing sanitation issue categories.
 */
object CategoryRepository {

    private var categoryDao: CategoryDao? = null
    private val fallbackCategories = mutableListOf(
        Category(
            id = 1,
            name = "Uncollected Garbage",
            description = "Garbage that has not been collected.",
            defaultPriority = Priority.HIGH
        ),
        Category(
            id = 2,
            name = "Illegal Dumping",
            description = "Waste dumped in unauthorised areas.",
            defaultPriority = Priority.HIGH
        ),
        Category(
            id = 3,
            name = "Blocked Drainage",
            description = "Blocked drains and drainage systems.",
            defaultPriority = Priority.URGENT
        ),
        Category(
            id = 4,
            name = "Sewage Overflow",
            description = "Sewage leaks and overflowing wastewater.",
            defaultPriority = Priority.URGENT
        ),
        Category(
            id = 5,
            name = "Street Cleaning",
            description = "Dirty streets and public spaces.",
            defaultPriority = Priority.MEDIUM
        )
    )

    fun init(context: Context) {
        if (categoryDao == null) {
            val db = AppDatabase.getInstance(context)
            categoryDao = db.categoryDao()
            seedIfNeeded()
        }
    }

    private fun seedIfNeeded() {
        val dao = categoryDao ?: return
        if (dao.getAll().isEmpty()) {
            for (c in fallbackCategories) {
                dao.insert(CategoryEntity.toEntity(c))
            }
        }
    }

    fun getCategories(): List<Category> {
        val dao = categoryDao
        if (dao != null) {
            return dao.getAll().map { it.toModel() }
        }
        return fallbackCategories.toList()
    }

    fun getCategoryById(id: Int): Category? {
        val dao = categoryDao
        if (dao != null) {
            return dao.getById(id)?.toModel()
        }
        return fallbackCategories.find { it.id == id }
    }

    fun getCategoryById(id: String): Category? {
        return id.toIntOrNull()?.let { getCategoryById(it) }
    }

    fun addCategory(
        name: String,
        description: String,
        priority: Priority
    ): String? {
        if (name.isBlank()) return "Category name is required"

        val currentCategories = getCategories()
        if (currentCategories.any {
                it.name.equals(name.trim(), ignoreCase = true)
            }) {
            return "This category already exists"
        }

        val newId = (currentCategories.maxOfOrNull { it.id } ?: 0) + 1
        val newCategory = Category(
            id = newId,
            name = name.trim(),
            description = description.trim(),
            defaultPriority = priority
        )

        val dao = categoryDao
        if (dao != null) {
            dao.insert(CategoryEntity.toEntity(newCategory))
        } else {
            fallbackCategories.add(newCategory)
        }

        return null
    }

    fun updateDefaultPriority(
        categoryId: Int,
        newPriority: Priority
    ): String? {
        val category = getCategoryById(categoryId) ?: return "Category not found"
        val updatedCategory = category.copy(defaultPriority = newPriority)

        val dao = categoryDao
        if (dao != null) {
            dao.update(CategoryEntity.toEntity(updatedCategory))
        } else {
            val index = fallbackCategories.indexOfFirst { it.id == categoryId }
            if (index != -1) fallbackCategories[index] = updatedCategory
        }

        return null
    }

    fun deleteCategory(categoryId: Int): String? {
        val category = getCategoryById(categoryId)
            ?: return "Category not found"

        val dao = categoryDao
        if (dao != null) {
            dao.delete(CategoryEntity.toEntity(category))
        } else {
            fallbackCategories.remove(category)
        }
        return null
    }
}
