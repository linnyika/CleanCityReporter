package com.group2.cleancity.data

import com.group2.cleancity.model.Role
import com.group2.cleancity.model.User
import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS_NAME = "session_prefs"
    private const val KEY_USER_ID = "user_id"
    private var prefs: SharedPreferences? = null

    // Called once from MainActivity so this object can reach the saved file
    fun init(context: Context) {
        prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Called on app start: if an ID was saved and that account is still valid, sign in again
    fun restore() {
        val savedId = prefs?.getInt(KEY_USER_ID, -1) ?: -1
        if (savedId == -1) return

        val user = UserRepository.findById(savedId)
        if (user != null && user.isActive) {
            currentUser = user
        } else {
            prefs?.edit()?.remove(KEY_USER_ID)?.apply()   // stale session: forget it
        }
    }

    // Readable by everyone, changeable only from inside this object
    var currentUser: User? = null
        private set

    val isLoggedIn: Boolean
        get() = currentUser != null

    val role: Role?
        get() = currentUser?.role

    fun login(user: User) {
        currentUser = user
        prefs?.edit()?.putInt(KEY_USER_ID, user.id)?.apply()
    }

    // Used when the user edits their profile: keep the session in sync
    fun refresh(updated: User) {
        if (currentUser?.id == updated.id) currentUser = updated
    }

    fun logout() {
        currentUser = null
        prefs?.edit()?.remove(KEY_USER_ID)?.apply()
    }

    // True only when someone is signed in AND has the required role.
    // Officer and Admin screens will use this as their guard.
    fun hasRole(required: Role): Boolean = currentUser?.role == required
}