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
    fun findById(id: Int): User? = users.find { it.id == id }

    fun getOfficers(): List<User> = users.filter { it.role == Role.OFFICER && it.isActive }
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

    //cera-had to add this to your slice1 bit
    // Returns null on success, or an error message to show
    fun updateProfile(userId: Int, fullName: String, phone: String): String? {
        val index = users.indexOfFirst { it.id == userId }
        if (index == -1) return "User not found"

        val normalized = Validators.normalizePhone(phone)
            ?: return "Enter a valid Kenyan phone number"

        // The phone may already be mine, but it must not belong to someone else
        val owner = findByPhone(normalized)
        if (owner != null && owner.id != userId) return "This phone number is already registered"

        // copy() makes a new User with only these two fields changed
        users[index] = users[index].copy(fullName = fullName.trim(), phone = normalized)
        return null
    }

    // Returns null on success, or an error message to show
    fun changePassword(userId: Int, currentPassword: String, newPassword: String): String? {
        val index = users.indexOfFirst { it.id == userId }
        if (index == -1) return "User not found"

        // Compare the typed password with the stored hash (never with plain text)
        if (!PasswordHasher.verify(currentPassword, users[index].passwordHash)) {
            return "Current password is incorrect"
        }
        if (currentPassword == newPassword) {
            return "New password must be different from the current one"
        }

        // Store only the new hash
        users[index] = users[index].copy(passwordHash = PasswordHasher.hash(newPassword))
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