package com.group2.cleancity

import com.group2.cleancity.data.LoginResult
import com.group2.cleancity.data.UserRepository
import com.group2.cleancity.model.Role
import org.junit.Assert.*
import org.junit.Test

class UserRepositoryTest {

    @Test
    fun admin_can_log_in() {
        val result = UserRepository.login("admin@cleancityreporter.com", "Admin1234")
        assertTrue(result is LoginResult.Success)
        assertEquals(Role.ADMIN, (result as LoginResult.Success).user.role)
    }

    @Test
    fun wrong_password_is_rejected() {
        val result = UserRepository.login("admin@cleancityreporter.com", "Wrong1234")
        assertTrue(result is LoginResult.InvalidCredentials)
    }

    @Test
    fun resident_signup_then_login_by_phone() {
        assertNull(UserRepository.registerResident("Tracy Karuga", "tracy@gmail.com", "0722111222", "Tracy1234"))
        assertNotNull(UserRepository.registerResident("Other Person", "tracy@gmail.com", "0733000111", "Other1234")) // duplicate email
        val result = UserRepository.login("0722111222", "Tracy1234")
        assertTrue(result is LoginResult.Success)
    }

    @Test
    fun staff_domain_cannot_sign_up() {
        assertNotNull(UserRepository.registerResident("Fake", "fake@cleancityreporter.com", "0744000111", "Fake1234"))
    }
}