package com.group2.cleancity.data

import android.content.Context
import com.group2.cleancity.data.db.AppDatabase
import com.group2.cleancity.data.db.AppSettingsEntity
import com.group2.cleancity.data.db.SettingsDao

object SettingsRepository {

    private var settingsDao: SettingsDao? = null
    private var fallbackThreshold = 7

    fun init(context: Context) {
        if (settingsDao == null) {
            val db = AppDatabase.getInstance(context)
            settingsDao = db.settingsDao()
            seedIfNeeded()
        }
    }

    private fun seedIfNeeded() {
        val dao = settingsDao ?: return
        if (dao.getOverdueThreshold() == null) {
            dao.insert(AppSettingsEntity(value = 7))
        }
    }

    fun getOverdueThreshold(): Int {
        val dao = settingsDao
        if (dao != null) {
            return dao.getOverdueThreshold() ?: 7
        }
        return fallbackThreshold
    }

    fun updateOverdueThreshold(days: Int): String? {
        if (days < 1) {
            return "The overdue threshold must be at least 1 day"
        }

        val dao = settingsDao
        if (dao != null) {
            dao.insert(AppSettingsEntity(value = days))
        } else {
            fallbackThreshold = days
        }
        return null
    }
}
