package com.group2.cleancity

import android.app.Application
import com.group2.cleancity.data.AuditRepository
import com.group2.cleancity.data.CategoryRepository
import com.group2.cleancity.data.NotificationRepository
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SettingsRepository
import com.group2.cleancity.data.UserRepository
import com.group2.cleancity.data.db.AppDatabase

class CleanCityApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize AppDatabase singleton
        AppDatabase.getInstance(this)

        // Initialize repositories
        UserRepository.init(this)
        ReportRepository.init(this)
        CategoryRepository.init(this)
        AuditRepository.init(this)
        NotificationRepository.init(this)
        SettingsRepository.init(this)
    }
}
