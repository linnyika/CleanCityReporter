package com.group2.cleancity.data

object SettingsRepository {

    private var overdueThresholdDays = 7

    fun getOverdueThreshold(): Int {
        return overdueThresholdDays
    }

    fun updateOverdueThreshold(days: Int): String? {
        if (days < 1) {
            return "The overdue threshold must be at least 1 day"
        }

        overdueThresholdDays = days
        return null
    }
}
