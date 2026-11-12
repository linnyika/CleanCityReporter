package com.group2.cleancity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.model.Role
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SessionManager.init(this)
        SessionManager.restore()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        setupBottomNavigation()

        if (savedInstanceState == null) openHomeIfLoggedIn()
    }

    private fun setupBottomNavigation() {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // The four screens that show the bar
        val tabIds = setOf(
            R.id.residentHomeFragment,
            R.id.reportsFragment,
            R.id.alertsFragment,
            R.id.profileFragment
        )

        // 1. When a tab is tapped, go to it
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId != navController.currentDestination?.id) {
                val options = NavOptions.Builder()
                    .setLaunchSingleTop(true)                       // no duplicate copies
                    .setPopUpTo(R.id.residentHomeFragment, false)   // Home stays at the bottom
                    .build()
                navController.navigate(item.itemId, null, options)
            }
            true
        }

        // 2. Whenever the screen changes: show/hide the bar and highlight the right tab
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.visibility =
                if (destination.id in tabIds) View.VISIBLE else View.GONE
            bottomNav.menu.findItem(destination.id)?.isChecked = true
        }
    } // <-- this closing brace was missing before openHomeIfLoggedIn()

    private fun openHomeIfLoggedIn() {
        // Pick the landing screen from the stored account's role
        val homeId = when (SessionManager.role) {
            Role.RESIDENT -> R.id.residentHomeFragment
            Role.OFFICER -> R.id.officerHomeFragment
            Role.ADMIN -> R.id.adminHomeFragment
            null -> return          // nobody signed in: stay on Welcome
        }

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        // Clear the whole back stack so Back doesn't return to Welcome
        val options = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build()
        navHost.navController.navigate(homeId, null, options)
    }
}