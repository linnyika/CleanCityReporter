package com.group2.cleancity.data

import com.group2.cleancity.model.Category
import com.group2.cleancity.model.Priority

object CategoryRepository{
    private val categories = mutableListOf(
        Category(
            id = "001",
            name = "Uncollected Garbage",
            description = "Garbage that has not been collected.",
            defaultPriority = Priority.HIGH
        ),
        Category(
           id = "002",
            name = "Illegal Dumping",
            description = "Waste dumped in unauthorised areas.",
            defaultPriority = Priority.HIGH
        ),
        Category(
            id = "003",
            name = "Blocked Drainage",
            description = "Blocked drains and drainage systems.",
            defaultPriority = Priority.CRITICAL
        ),
        Category(
            id = "004",
            name = "Sewage Overflow",
            description = "Sewage leaks and overflowing wastewater.",
            defaultPriority = Priority.CRITICAL
        ),
        Category(
            id = "005",
            name = "Street Cleaning",
            description = "Dirty streets and public spaces.",
            defaultPriority = Priority.MEDIUM
        )
    )
    fun getCategories(): List<Category>{
        return categories.toList()
    }
    fun getCategoryById(id: String): Category? {
        return categories.find{ it.id == id}
    }
}