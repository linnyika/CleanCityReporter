package com.group2.cleancity.model

/**
 * Data class representing a sanitation issue category.
 */
data class Category(
    val id: Int, // Unique category ID
    val name: String, // Display name (e.g., "Illegal Dumping", "Blocked Drainage")
    val defaultPriority: Priority = Priority.MEDIUM, // Default priority for reports in this category
    val isActive: Boolean = true, // Whether this category is active in the system
    val description: String = "" // Optional short description shown on the report form
)