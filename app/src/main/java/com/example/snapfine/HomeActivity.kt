package com.example.snapfine

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private val homeFragment = HomeFragment()
    private val historyFragment = HistoryFragment()
    private val profileFragment = ProfileFragment()

    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, historyFragment, "history").hide(historyFragment)
            .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
            .add(R.id.fragment_container, homeFragment, "home")
            .commit()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_home
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchFragment(homeFragment)
                R.id.nav_report -> {
                    startActivity(Intent(this, CameraActivity::class.java))
                    false
                }
                R.id.nav_complaints -> {
                    startActivity(Intent(this, MyComplaintsActivity::class.java))
                    false
                }
                R.id.nav_cases -> switchFragment(historyFragment)
                R.id.nav_profile -> switchFragment(profileFragment)
                else -> false
            }
        }
    }

    fun switchToHomeFragment() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_home
        if (activeFragment != homeFragment) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(homeFragment)
                .commit()
            activeFragment = homeFragment
        }
    }

    fun isHomeFragmentActive(): Boolean {
        return activeFragment == homeFragment
    }

    private fun switchFragment(target: Fragment): Boolean {
        if (activeFragment == target) return true
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(target)
            .commit()
        activeFragment = target
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (activeFragment != homeFragment) {
            switchToHomeFragment()
        } else {
            super.onBackPressed()
        }
    }
}
