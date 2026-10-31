package com.group2.cleancity.data

import com.group2.cleancity.model.Role
import com.group2.cleancity.model.User

object SessionManager {

    // Readable by everyone, changeable only from inside this object
    var currentUser: User? = null
        private set

    val isLoggedIn: Boolean
        get() = currentUser != null

    val role: Role?
        get() = currentUser?.role

    fun login(user: User) {
        currentUser = user
    }

    // Used when the user edits their profile: keep the session in sync
    fun refresh(updated: User) {
        if (currentUser?.id == updated.id) currentUser = updated
    }

    fun logout() {
        currentUser = null
    }

    // True only when someone is signed in AND has the required role.
    // Officer and Admin screens will use this as their guard.
    fun hasRole(required: Role): Boolean = currentUser?.role == required
}