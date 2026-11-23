package com.group2.cleancity.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.group2.cleancity.model.Role
import com.group2.cleancity.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val fullName: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: Long
) {
    fun toModel(): User = User(
        id = id,
        fullName = fullName,
        email = email,
        phone = phone,
        passwordHash = passwordHash,
        role = Role.valueOf(role),
        isActive = isActive,
        createdAt = createdAt
    )

    companion object {
        fun toEntity(user: User): UserEntity = UserEntity(
            id = user.id,
            fullName = user.fullName,
            email = user.email,
            phone = user.phone,
            passwordHash = user.passwordHash,
            role = user.role.name,
            isActive = user.isActive,
            createdAt = user.createdAt
        )
    }
}
