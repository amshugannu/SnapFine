package com.example.snapfine

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var logoutButton: Button
    private lateinit var listView: ListView
    val homeFragment = HomeFragment()
    val historyFragment = HistoryFragment()
    val profileFragment = ProfileFragment()

    var activeFragment: Fragment = homeFragment

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        drawerLayout = findViewById(R.id.drawerlayout)
        val plus_img = findViewById<ImageView>(R.id.plus_img)
        listView = findViewById(R.id.drawer_menu_list)
        logoutButton = findViewById(R.id.logout_btn_drawer)

        plus_img.setOnClickListener(){
            startActivity(Intent(this,CameraActivity::class.java))
        }

        updateUserNameInDrawer()

        // Navigation drawer items and adapter
        val drawerItems = arrayOf(
            "My Cases",
            "Your Complaints",
            "Report Violation",
            "Pay Fines",
            "Contact",
            "FAQs",
            "Privacy Policy"
        )

        val adapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, drawerItems)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HistoryFragment())
                        .addToBackStack("HistoryFragment")
                        .commit()
                }
                1 -> startActivity(Intent(applicationContext, MyComplaintsActivity::class.java))
                2 -> startActivity(Intent(applicationContext, CameraActivity::class.java))
                3 -> Toast.makeText(applicationContext, "Pay Fines Clicked", Toast.LENGTH_SHORT).show()
                4 -> Toast.makeText(applicationContext, "Contact Clicked", Toast.LENGTH_SHORT).show()
                5 -> Toast.makeText(applicationContext, "FAQs Clicked", Toast.LENGTH_SHORT).show()
                6 -> Toast.makeText(applicationContext, "Privacy Policy Clicked", Toast.LENGTH_SHORT).show()
            }
            drawerLayout.closeDrawers()
        }

        // Logout button
        logoutButton.setOnClickListener {
            logout()
        }

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, historyFragment, "history").hide(historyFragment)
            .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
            .add(R.id.fragment_container, homeFragment, "home")
            .commit()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchFragment(homeFragment)
                R.id.nav_profile -> switchFragment(profileFragment)
                R.id.nav_history -> switchFragment(historyFragment)
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

    fun switchFragment(target: Fragment): Boolean {
        if (activeFragment == target) return false
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(target)
            .commit()
        activeFragment = target
        return true
    }
    override fun onBackPressed() {
        if (activeFragment != homeFragment) {
            // Switch to home fragment instead of exiting
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNavigationView.selectedItemId = R.id.nav_home
            switchFragment(homeFragment)
        } else {
            // If on home fragment, behave normally (exit app or navigate back)
            super.onBackPressed()
        }
    }

    fun viewProfile(view: View) {
        val fragment = ProfileFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Ensure you have this container in your activity_home.xml
            .addToBackStack(null)
            .commit()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, Register::class.java))
        finish()
    }
    override fun onResume() {
        super.onResume()
        updateUserNameInDrawer()
    }

    private fun updateUserNameInDrawer() {
        val userNameTextView = findViewById<TextView>(R.id.user_name_text)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("Name") ?: "User"
                    userNameTextView.text = name
                }
                .addOnFailureListener {
                    userNameTextView.text = "User"
                }
        } else {
            userNameTextView.text = "Guest"
        }
    }


}
