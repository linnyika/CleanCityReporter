package com.group2.cleancity.util

object NameFormat {
    fun displayName(fullName: String): String {
        return fullName.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString(" ")
    }
}
