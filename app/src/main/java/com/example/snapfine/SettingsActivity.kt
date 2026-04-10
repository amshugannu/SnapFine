package com.example.snapfine

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var rowLogout: View
    private lateinit var rowAbout: View
    private lateinit var rowVersion: View
    private lateinit var rowDeleteAccount: View
    private lateinit var tvVersion: TextView

    private val fAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initializeViews()
        setupClickListeners()
        displayAppVersion()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        rowLogout = findViewById(R.id.rowLogout)
        rowAbout = findViewById(R.id.rowAbout)
        rowVersion = findViewById(R.id.rowVersion)
        rowDeleteAccount = findViewById(R.id.rowDeleteAccount)
        tvVersion = findViewById(R.id.tvVersion)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            onBackPressed()
        }

        rowLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        rowAbout.setOnClickListener {
            showAboutDialog()
        }

        rowVersion.setOnClickListener {
            // Re-show version toast or dialog
            Toast.makeText(this, "SnapFine Version ${tvVersion.text}", Toast.LENGTH_SHORT).show()
        }

        rowDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun displayAppVersion() {
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            tvVersion.text = version
        } catch (e: Exception) {
            tvVersion.text = "1.0"
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.item_logout)
            .setMessage(R.string.msg_logout_confirm)
            .setPositiveButton("Logout") { _, _ ->
                fAuth.signOut()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.item_about)
            .setMessage("SnapFine is a modern traffic violation reporting application. Report incidents, track your history, and help keep our roads safe.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.item_delete_account)
            .setMessage(R.string.msg_delete_confirm)
            .setPositiveButton("Delete") { _, _ ->
                // Placeholder for actual deletion logic
                Toast.makeText(this, "Account deletion request sent.", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
