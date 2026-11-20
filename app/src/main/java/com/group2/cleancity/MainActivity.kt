package com.group2.cleancity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.model.Role

class MainActivity : AppCompatActivity() {

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

        if (savedInstanceState == null) openHomeIfLoggedIn()
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
                R.id.adminManageFragment,
                R.id.adminReportsFragment,
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
                        R.id.adminManageFragment,
                        R.id.adminReportsFragment,
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
    }
}