package com.group2.cleancity.util

object Validators {

    const val STAFF_DOMAIN = "@cleancityreporter.com"

    private val EMAIL_REGEX =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Accepts 0712345678, 0112345678, 712345678, 254712345678, +254712345678
    private val PHONE_REGEX = Regex("^(?:\\+254|254|0)?([71]\\d{8})$")

    fun validateRequired(value: String, fieldName: String): String? =
        if (value.isBlank()) "$fieldName is required" else null

    fun validateFullName(name: String): String? {
        if (name.isBlank()) return "Full name is required"
        if (name.trim().length < 3) return "Enter your full name"
        return null
    }

    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Email is required"
        if (!EMAIL_REGEX.matches(email.trim())) return "Enter a valid email address"
        return null
    }

    // Sign Up only: residents can't use the staff domain
    fun validateResidentEmail(email: String): String? {
        validateEmail(email)?.let { return it }
        if (isStaffEmail(email)) return "This email domain is reserved for staff"
        return null
    }

    fun isStaffEmail(email: String): Boolean =
        email.trim().lowercase().endsWith(STAFF_DOMAIN)

    // Returns the phone as +2547XXXXXXXX, or null if it isn't a valid Kenyan number
    fun normalizePhone(input: String): String? {
        val cleaned = input.replace(" ", "").replace("-", "")
        val match = PHONE_REGEX.matchEntire(cleaned) ?: return null
        return "+254" + match.groupValues[1]
    }

    fun validatePhone(phone: String): String? {
        if (phone.isBlank()) return "Phone number is required"
        if (normalizePhone(phone) == null) return "Enter a valid Kenyan phone number"
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.isEmpty()) return "Password is required"
        if (password.length < 8) return "Use at least 8 characters"
        if (!password.any { it.isDigit() }) return "Include at least one number"
        return null
    }

    fun validateConfirmPassword(password: String, confirm: String): String? {
        if (confirm.isEmpty()) return "Confirm your password"
        if (password != confirm) return "Passwords do not match"
        return null
    }

    // Login and Forgot Password accept an email OR a phone number
    fun validateIdentifier(input: String): String? {
        if (input.isBlank()) return "Email or phone number is required"
        val looksLikeEmail = input.contains("@")
        if (looksLikeEmail && validateEmail(input) != null) return "Enter a valid email address"
        if (!looksLikeEmail && normalizePhone(input) == null) return "Enter a valid email or phone number"
        return null
    }
}