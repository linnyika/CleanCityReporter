package com.group2.cleancity.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.model.Report
import com.group2.cleancity.model.ReportStatus

class ResidentHomeViewModel : ViewModel() {

    private val _totalCount = MutableLiveData("0")
    val totalCount: LiveData<String> = _totalCount

    private val _inProgressCount = MutableLiveData("0")
    val inProgressCount: LiveData<String> = _inProgressCount

    private val _resolvedCount = MutableLiveData("0")
    val resolvedCount: LiveData<String> = _resolvedCount

    private val _recentReports = MutableLiveData<List<Report>>(emptyList())
    val recentReports: LiveData<List<Report>> = _recentReports

    private val _greeting = MutableLiveData("")
    val greeting: LiveData<String> = _greeting

    fun loadResidentData() {
        val user = SessionManager.currentUser ?: return
        val fullName = user.fullName
        val firstName = fullName.trim().substringBefore(" ")
        _greeting.value = "Hello, $firstName"

        val myReports = ReportRepository.getReportsByReporter(user.id)
        _totalCount.value = myReports.size.toString()
        _inProgressCount.value =
            myReports.count { it.status == ReportStatus.IN_PROGRESS }.toString()
        _resolvedCount.value =
            myReports.count { it.status == ReportStatus.RESOLVED }.toString()

        val recent = myReports.sortedByDescending { it.createdAt }.take(3)
        _recentReports.value = recent
    }
}
