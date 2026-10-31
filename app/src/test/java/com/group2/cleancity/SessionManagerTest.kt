package com.group2.cleancity

import com.group2.cleancity.data.LoginResult
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.data.UserRepository
import com.group2.cleancity.model.Role
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class SessionManagerTest {

    @After
    fun cleanUp() {
        SessionManager.logout()   // each test starts with nobody signed in
    }

    @Test
    fun starts_logged_out() {
        assertFalse(SessionManager.isLoggedIn)
        assertNull(SessionManager.currentUser)
        assertNull(SessionManager.role)
    }

    @Test
    fun login_and_logout_update_the_session() {
        val result = UserRepository.login("admin@cleancityreporter.com", "Admin1234")
        SessionManager.login((result as LoginResult.Success).user)

        assertTrue(SessionManager.isLoggedIn)
        assertEquals(Role.ADMIN, SessionManager.role)
        assertTrue(SessionManager.hasRole(Role.ADMIN))
        assertFalse(SessionManager.hasRole(Role.RESIDENT))

        SessionManager.logout()
        assertFalse(SessionManager.isLoggedIn)
        assertFalse(SessionManager.hasRole(Role.ADMIN))
    }

    @Test
    fun findById_returns_the_right_user() {
        val admin = UserRepository.findByEmail("admin@cleancityreporter.com")!!
        assertEquals(admin, UserRepository.findById(admin.id))
        assertNull(UserRepository.findById(9999))
    }
}