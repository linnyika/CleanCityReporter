
package com.group2.cleancity.data

import com.group2.cleancity.model.Category
import com.group2.cleancity.model.Priority

object CategoryRepository {

    private val categories = mutableListOf(
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

    fun getCategories(): List<Category> {
        return categories.toList()
    }

    fun getCategoryById(id: Int): Category? {
        return categories.find { it.id == id }
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

        if (categories.any {
                it.name.equals(name.trim(), ignoreCase = true)
            }) {
            return "This category already exists"
        }

        val newId = (categories.maxOfOrNull { it.id } ?: 0) + 1

        categories.add(
            Category(
                id = newId,
                name = name.trim(),
                description = description.trim(),
                defaultPriority = priority
            )
        )

        return null
    }

    fun updateDefaultPriority(
        categoryId: Int,
        newPriority: Priority
    ): String? {
        val index = categories.indexOfFirst { it.id == categoryId }

        if (index == -1) return "Category not found"

        categories[index] = categories[index].copy(
            defaultPriority = newPriority
        )

        return null
    }

    fun deleteCategory(categoryId: Int): String? {
        val category = categories.find { it.id == categoryId }
            ?: return "Category not found"

        categories.remove(category)
        return null
    }
}
