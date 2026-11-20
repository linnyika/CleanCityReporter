package com.group2.cleancity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.group2.cleancity.data.NotificationRepository
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.model.Role
import com.group2.cleancity.util.NotificationHelper

class MainActivity : AppCompatActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private fun requestNotificationPermissionOnceForResident() {
        val resident = SessionManager.currentUser
        if (resident == null || resident.role != Role.RESIDENT) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val prefs = getSharedPreferences("notification_permission_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("resident_permission_requested", false)) return

        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!granted) {
            prefs.edit().putBoolean("resident_permission_requested", true).apply()
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SessionManager.init(this)
        SessionManager.restore()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        setupNavigation()

        NotificationHelper.createChannel(this)
        requestNotificationPermissionOnceForResident()

        if (savedInstanceState == null) openHomeIfLoggedIn()
        handleOpenReportIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOpenReportIntent(intent)
    }

    private fun handleOpenReportIntent(intent: Intent) {
        val reportId = intent.getStringExtra("open_report_id") ?: return
        val resident = SessionManager.currentUser
        if (resident == null || resident.role != Role.RESIDENT) return

        val report = ReportRepository.getReportById(reportId)
        if (report == null || report.reporterId != resident.id) return

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val arguments = Bundle().apply { putString("reportId", reportId) }
        navHost.navController.navigate(R.id.residentReportDetailsFragment, arguments)
        intent.removeExtra("open_report_id")
    }

    private fun setupNavigation() {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        // Only the role's four bottom-navigation destinations are top-level tabs.
        // Every other destination is a child and NavigationUI shows the up arrow.
        val topLevelDestinations = when (SessionManager.role) {
            Role.RESIDENT -> setOf(
                R.id.residentHomeFragment,
                R.id.reportsFragment,
                R.id.alertsFragment,
                R.id.profileFragment
            )

            Role.OFFICER -> setOf(
                R.id.officerHomeFragment,
                R.id.officerReportQueueFragment,
                R.id.officerAssignedFragment,
                R.id.profileFragment
            )

            Role.ADMIN -> setOf(
                R.id.adminHomeFragment,
                R.id.adminManageHubFragment,
                R.id.adminReportsHubFragment,
                R.id.profileFragment
            )

            null -> setOf(R.id.welcomeFragment)
        }

        val appBarConfiguration = AppBarConfiguration(topLevelDestinations)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        var currentMenuResId: Int? = null

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId != navController.currentDestination?.id) {
                val firstTabId = when (SessionManager.role) {
                    Role.RESIDENT -> R.id.residentHomeFragment
                    Role.OFFICER -> R.id.officerHomeFragment
                    Role.ADMIN -> R.id.adminHomeFragment
                    null -> R.id.welcomeFragment
                }

                val options = NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(firstTabId, false)
                    .build()

                navController.navigate(item.itemId, null, options)
            }
            true
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val (menuResId, tabDestinations) = when (SessionManager.role) {
                Role.RESIDENT -> Pair(
                    R.menu.menu_resident,
                    setOf(
                        R.id.residentHomeFragment,
                        R.id.reportsFragment,
                        R.id.alertsFragment,
                        R.id.profileFragment
                    )
                )

                Role.OFFICER -> Pair(
                    R.menu.menu_officer,
                    setOf(
                        R.id.officerHomeFragment,
                        R.id.officerReportQueueFragment,
                        R.id.officerAssignedFragment,
                        R.id.profileFragment
                    )
                )

                Role.ADMIN -> Pair(
                    R.menu.menu_admin,
                    setOf(
                        R.id.adminHomeFragment,
                        R.id.adminManageHubFragment,
                        R.id.adminReportsHubFragment,
                        R.id.profileFragment
                    )
                )

                null -> Pair(null, emptySet<Int>())
            }

            if (menuResId != null && destination.id in tabDestinations) {
                if (currentMenuResId != menuResId) {
                    bottomNav.menu.clear()
                    bottomNav.inflateMenu(menuResId)
                    currentMenuResId = menuResId
                }

                bottomNav.visibility = View.VISIBLE
                bottomNav.menu.findItem(destination.id)?.isChecked = true
            } else {
                bottomNav.visibility = View.GONE
            }

            updateResidentAlertsBadge(bottomNav)
        }

        toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }
    }

    private fun openHomeIfLoggedIn() {
        val homeId = when (SessionManager.role) {
            Role.RESIDENT -> R.id.residentHomeFragment
            Role.OFFICER -> R.id.officerHomeFragment
            Role.ADMIN -> R.id.adminHomeFragment
            null -> return
        }

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val options = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build()

        navHost.navController.navigate(homeId, null, options)
        postResidentNotifications()
    }

    override fun onResume() {
        super.onResume()
        requestNotificationPermissionOnceForResident()
        updateResidentAlertsBadge(findViewById(R.id.bottomNav))
        postResidentNotifications()
    }

    private fun postResidentNotifications() {
        val resident = SessionManager.currentUser ?: return
        if (SessionManager.role != Role.RESIDENT) return
        if (!NotificationHelper.areNotificationsAllowed(this)) return

        val unposted = NotificationRepository.getUnposted(resident.id)
        if (unposted.isEmpty()) return

        if (unposted.size > 3) {
            NotificationHelper.show(
                this,
                "summary_${resident.id}_${System.currentTimeMillis()}",
                "You have ${unposted.size} new updates"
            )
            unposted.forEach { NotificationRepository.markPosted(it.id) }
        } else {
            unposted.forEach { notification ->
                NotificationHelper.show(
                    this,
                    notification.reportId,
                    notification.message
                )
                NotificationRepository.markPosted(notification.id)
            }
        }
    }

    private fun updateResidentAlertsBadge(bottomNav: BottomNavigationView) {
        val resident = SessionManager.currentUser
        if (SessionManager.role != Role.RESIDENT || resident == null) {
            bottomNav.removeBadge(R.id.alertsFragment)
            return
        }

        val count = NotificationRepository.unreadCount(resident.id)
        if (count == 0) {
            bottomNav.removeBadge(R.id.alertsFragment)
        } else {
            val badge = bottomNav.getOrCreateBadge(R.id.alertsFragment)
            badge.number = count
            badge.isVisible = true
        }
    }
}