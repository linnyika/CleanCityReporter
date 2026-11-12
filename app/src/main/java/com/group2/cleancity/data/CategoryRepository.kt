package com.group2.cleancity.data

import com.group2.cleancity.model.Category
import com.group2.cleancity.model.Priority

object CategoryRepository {
    private val categories = mutableListOf(
        Category(
            id = 1,
            name = "Uncollected Garbage",
            description = "Garbage that has not been collected.",
            defaultPriority = Priority.HIGH,
        ),
        Category(
            id = 2,
            name = "Illegal Dumping",
            description = "Waste dumped in unauthorised areas.",
            defaultPriority = Priority.HIGH,
        ),
        Category(
            id = 3,
            name = "Blocked Drainage",
            description = "Blocked drains and drainage systems.",
            defaultPriority = Priority.URGENT,
        ),
        Category(
            id = 4,
            name = "Sewage Overflow",
            description = "Sewage leaks and overflowing wastewater.",
            defaultPriority = Priority.URGENT,
        ),
        Category(
            id = 5,
            name = "Street Cleaning",
            description = "Dirty streets and public spaces.",
            defaultPriority = Priority.MEDIUM,
        ),
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
}
