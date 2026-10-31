package com.group2.cleancity.data

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

    private val users = mutableListOf<User>()
    private var nextId = 1

    init {
        // Seeded staff accounts (Residents sign up themselves)
        addSeedUser("System Admin", "admin@cleancityreporter.com", "0700000001", "Admin1234", Role.ADMIN)
        addSeedUser("Achieng Odhiambo", "officer.achieng@cleancityreporter.com", "0700000002", "Officer1234", Role.OFFICER)
        addSeedUser("Brian Otieno", "officer.otieno@cleancityreporter.com", "0700000003", "Officer1234", Role.OFFICER)
        // A demo resident so you can log in before signing up
        addSeedUser("Demo Resident", "demo.resident@gmail.com", "0712345678", "Resident1234", Role.RESIDENT)
    }

    private fun addSeedUser(name: String, email: String, phone: String, password: String, role: Role) {
        users.add(
            User(
                id = nextId++,
                fullName = name,
                email = email.lowercase(),
                phone = Validators.normalizePhone(phone) ?: phone,
                passwordHash = PasswordHasher.hash(password),
                role = role
            )
        )
    }

    fun findByEmail(email: String): User? =
        users.find { it.email == email.trim().lowercase() }

    fun findByPhone(phone: String): User? {
        val normalized = Validators.normalizePhone(phone) ?: return null
        return users.find { it.phone == normalized }
    }

    fun findByIdentifier(input: String): User? =
        if (input.contains("@")) findByEmail(input) else findByPhone(input)

    // Returns null on success, or an error message to show
    fun registerResident(fullName: String, email: String, phone: String, password: String): String? {
        if (Validators.isStaffEmail(email)) return "This email domain is reserved for staff"
        if (findByEmail(email) != null) return "This email is already registered"
        if (findByPhone(phone) != null) return "This phone number is already registered"

        users.add(
            User(
                id = nextId++,
                fullName = fullName.trim(),
                email = email.trim().lowercase(),
                phone = Validators.normalizePhone(phone) ?: phone,
                passwordHash = PasswordHasher.hash(password),
                role = Role.RESIDENT
            )
        )
        return null
    }

    fun login(identifier: String, password: String): LoginResult {
        val user = findByIdentifier(identifier)
        // Same answer for "no such user" and "wrong password", on purpose
        if (user == null || !PasswordHasher.verify(password, user.passwordHash)) {
            return LoginResult.InvalidCredentials
        }
        if (!user.isActive) return LoginResult.Deactivated
        return LoginResult.Success(user)
    }
}