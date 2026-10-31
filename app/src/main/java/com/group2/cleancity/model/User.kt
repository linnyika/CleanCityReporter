package com.group2.cleancity.model

data class User(
    val id: Int,
    val fullName: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val role: Role,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)