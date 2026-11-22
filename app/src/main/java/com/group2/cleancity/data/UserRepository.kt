package com.group2.cleancity.data

import android.content.Context
import com.group2.cleancity.data.db.AppDatabase
import com.group2.cleancity.data.db.UserDao
import com.group2.cleancity.data.db.UserEntity
import com.group2.cleancity.model.Role
import com.group2.cleancity.model.User
import com.group2.cleancity.util.PasswordHasher
import com.group2.cleancity.util.Validators

// The possible outcomes of a login attempt
sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    object InvalidCredentials : LoginResult()
    object Deactivated : LoginResult()
}

object UserRepository {

    private var userDao: UserDao? = null
    private val fallbackUsers = mutableListOf<User>()
    private var fallbackNextId = 1

    init {
        if (fallbackUsers.isEmpty()) {
            addFallbackSeed("System Admin", "admin@cleancityreporter.com", "0700000001", "Admin1234", Role.ADMIN)
            addFallbackSeed("Achieng Odhiambo", "officer.achieng@cleancityreporter.com", "0700000002", "Officer1234", Role.OFFICER)
            addFallbackSeed("Brian Otieno", "officer.otieno@cleancityreporter.com", "0700000003", "Officer1234", Role.OFFICER)
            addFallbackSeed("Demo Resident", "demo.resident@gmail.com", "0712345678", "Resident1234", Role.RESIDENT)
        }
    }

    private fun addFallbackSeed(name: String, email: String, phone: String, password: String, role: Role) {
        fallbackUsers.add(
            User(
                id = fallbackNextId++,
                fullName = name,
                email = email.lowercase(),
                phone = Validators.normalizePhone(phone) ?: phone,
                passwordHash = PasswordHasher.hash(password),
                role = role
            )
        )
    }

    fun init(context: Context) {
        if (userDao == null) {
            val db = AppDatabase.getInstance(context)
            userDao = db.userDao()
            seedIfNeeded()
        }
    }

    private fun seedIfNeeded() {
        val dao = userDao ?: return
        if (dao.getAll().isEmpty()) {
            val now = System.currentTimeMillis()
            val seedUsers = listOf(
                UserEntity(1, "System Admin", "admin@cleancityreporter.com", "0700000001", PasswordHasher.hash("Admin1234"), Role.ADMIN.name, true, now),
                UserEntity(2, "Achieng Odhiambo", "officer.achieng@cleancityreporter.com", "0700000002", PasswordHasher.hash("Officer1234"), Role.OFFICER.name, true, now),
                UserEntity(3, "Brian Otieno", "officer.otieno@cleancityreporter.com", "0700000003", PasswordHasher.hash("Officer1234"), Role.OFFICER.name, true, now),
                UserEntity(4, "Demo Resident", "demo.resident@gmail.com", "0712345678", PasswordHasher.hash("Resident1234"), Role.RESIDENT.name, true, now)
            )
            for (u in seedUsers) {
                dao.insert(u)
            }
        }
    }

    private fun getNextId(): Int {
        val dao = userDao
        if (dao != null) {
            val all = dao.getAll()
            return (all.maxOfOrNull { it.id } ?: 0) + 1
        } else {
            return (fallbackUsers.maxOfOrNull { it.id } ?: 0) + 1
        }
    }

    fun findByEmail(email: String): User? {
        val target = email.trim().lowercase()
        val dao = userDao
        if (dao != null) {
            return dao.findByEmail(target)?.toModel()
        }
        return fallbackUsers.find { it.email == target }
    }

    fun findByPhone(phone: String): User? {
        val normalized = Validators.normalizePhone(phone) ?: return null
        val dao = userDao
        if (dao != null) {
            return dao.findByPhone(normalized)?.toModel()
        }
        return fallbackUsers.find { it.phone == normalized }
    }

    fun findByIdentifier(input: String): User? =
        if (input.contains("@")) findByEmail(input) else findByPhone(input)

    fun findById(id: Int): User? {
        val dao = userDao
        if (dao != null) {
            return dao.getById(id)?.toModel()
        }
        return fallbackUsers.find { it.id == id }
    }

    fun getAllUsers(): List<User> {
        val dao = userDao
        if (dao != null) {
            return dao.getAll().map { it.toModel() }
        }
        return fallbackUsers.toList()
    }

    fun getOfficers(): List<User> = getAllUsers().filter { it.role == Role.OFFICER && it.isActive }

    fun registerResident(fullName: String, email: String, phone: String, password: String): String? {
        if (Validators.isStaffEmail(email)) return "This email domain is reserved for staff"
        if (findByEmail(email) != null) return "This email is already registered"
        if (findByPhone(phone) != null) return "This phone number is already registered"

        val normalizedPhone = Validators.normalizePhone(phone) ?: phone
        val user = User(
            id = getNextId(),
            fullName = fullName.trim(),
            email = email.trim().lowercase(),
            phone = normalizedPhone,
            passwordHash = PasswordHasher.hash(password),
            role = Role.RESIDENT
        )

        val dao = userDao
        if (dao != null) {
            dao.insert(UserEntity.toEntity(user))
        } else {
            fallbackUsers.add(user)
        }
        return null
    }

    fun createOfficer(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): String? {
        if (fullName.isBlank()) return "Enter the officer's full name"
        if (!Validators.isStaffEmail(email)) {
            return "Officer email must use @cleancityreporter.com"
        }
        if (findByEmail(email) != null) {
            return "This email is already registered"
        }
        val normalizedPhone = Validators.normalizePhone(phone)
            ?: return "Enter a valid Kenyan phone number"
        if (findByPhone(normalizedPhone) != null) {
            return "This phone number is already registered"
        }
        if (password.isBlank()) {
            return "Enter a password"
        }

        val user = User(
            id = getNextId(),
            fullName = fullName.trim(),
            email = email.trim().lowercase(),
            phone = normalizedPhone,
            passwordHash = PasswordHasher.hash(password),
            role = Role.OFFICER
        )

        val dao = userDao
        if (dao != null) {
            dao.insert(UserEntity.toEntity(user))
        } else {
            fallbackUsers.add(user)
        }
        return null
    }

    fun updateUserStatus(
        userId: Int,
        isActive: Boolean,
        actingAdminId: Int
    ): String? {
        val user = findById(userId) ?: return "User not found"
        val allUsers = getAllUsers()

        if (!isActive && userId == actingAdminId) {
            return "You cannot deactivate your own account"
        }

        if (!isActive && user.role == Role.ADMIN) {
            val activeAdmins = allUsers.count {
                it.role == Role.ADMIN && it.isActive
            }
            if (activeAdmins <= 1) {
                return "You cannot deactivate the last active administrator"
            }
        }

        val updatedUser = user.copy(isActive = isActive)
        val dao = userDao
        if (dao != null) {
            dao.update(UserEntity.toEntity(updatedUser))
        } else {
            val index = fallbackUsers.indexOfFirst { it.id == userId }
            if (index != -1) fallbackUsers[index] = updatedUser
        }
        SessionManager.refresh(updatedUser)
        return null
    }

    fun updateUserRole(
        userId: Int,
        newRole: Role,
        actingAdminId: Int
    ): String? {
        val user = findById(userId) ?: return "User not found"
        val allUsers = getAllUsers()

        if (userId == actingAdminId) {
            return "You cannot change your own role"
        }

        if (user.role == Role.ADMIN && newRole != Role.ADMIN && user.isActive) {
            val activeAdmins = allUsers.count {
                it.role == Role.ADMIN && it.isActive
            }
            if (activeAdmins <= 1) {
                return "You cannot remove the role of the last active administrator"
            }
        }

        val updatedUser = user.copy(role = newRole)
        val dao = userDao
        if (dao != null) {
            dao.update(UserEntity.toEntity(updatedUser))
        } else {
            val index = fallbackUsers.indexOfFirst { it.id == userId }
            if (index != -1) fallbackUsers[index] = updatedUser
        }
        SessionManager.refresh(updatedUser)
        return null
    }

    fun updateProfile(userId: Int, fullName: String, phone: String): String? {
        val user = findById(userId) ?: return "User not found"
        val normalized = Validators.normalizePhone(phone)
            ?: return "Enter a valid Kenyan phone number"

        val owner = findByPhone(normalized)
        if (owner != null && owner.id != userId) return "This phone number is already registered"

        val updatedUser = user.copy(fullName = fullName.trim(), phone = normalized)
        val dao = userDao
        if (dao != null) {
            dao.update(UserEntity.toEntity(updatedUser))
        } else {
            val index = fallbackUsers.indexOfFirst { it.id == userId }
            if (index != -1) fallbackUsers[index] = updatedUser
        }
        return null
    }

    fun changePassword(userId: Int, currentPassword: String, newPassword: String): String? {
        val user = findById(userId) ?: return "User not found"
        if (!PasswordHasher.verify(currentPassword, user.passwordHash)) {
            return "Current password is incorrect"
        }
        if (currentPassword == newPassword) {
            return "New password must be different from the current one"
        }

        val updatedUser = user.copy(passwordHash = PasswordHasher.hash(newPassword))
        val dao = userDao
        if (dao != null) {
            dao.update(UserEntity.toEntity(updatedUser))
        } else {
            val index = fallbackUsers.indexOfFirst { it.id == userId }
            if (index != -1) fallbackUsers[index] = updatedUser
        }
        return null
    }

    fun login(identifier: String, password: String): LoginResult {
        val user = findByIdentifier(identifier)
        if (user == null || !PasswordHasher.verify(password, user.passwordHash)) {
            return LoginResult.InvalidCredentials
        }
        if (!user.isActive) return LoginResult.Deactivated
        return LoginResult.Success(user)
    }
}
