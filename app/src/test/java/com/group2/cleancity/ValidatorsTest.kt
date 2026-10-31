package com.group2.cleancity

import com.group2.cleancity.util.PasswordHasher
import com.group2.cleancity.util.Validators
import org.junit.Assert.*
import org.junit.Test

class ValidatorsTest {

    @Test
    fun password_needs_8_chars_and_a_number() {
        assertNotNull(Validators.validatePassword("abc123"))      // too short
        assertNotNull(Validators.validatePassword("abcdefgh"))    // no number
        assertNull(Validators.validatePassword("abcdefg1"))       // valid
    }

    @Test
    fun phone_formats_are_normalized() {
        assertEquals("+254712345678", Validators.normalizePhone("0712 345 678"))
        assertEquals("+254712345678", Validators.normalizePhone("+254712345678"))
        assertNull(Validators.normalizePhone("12345"))
    }

    @Test
    fun residents_cannot_use_staff_domain() {
        assertNotNull(Validators.validateResidentEmail("hacker@cleancityreporter.com"))
        assertNull(Validators.validateResidentEmail("linda@gmail.com"))
    }

    @Test
    fun hashing_works_and_uses_salt() {
        val h1 = PasswordHasher.hash("secret123")
        val h2 = PasswordHasher.hash("secret123")
        assertNotEquals(h1, h2)                                   // different salts
        assertTrue(PasswordHasher.verify("secret123", h1))
        assertFalse(PasswordHasher.verify("wrong123", h1))
    }
}