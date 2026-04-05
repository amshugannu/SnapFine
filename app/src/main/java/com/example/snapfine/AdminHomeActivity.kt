package com.example.snapfine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AdminHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        val btnCreateStaff = findViewById<android.widget.Button>(R.id.btnCreateStaff)
        btnCreateStaff.setOnClickListener {
            val intent = android.content.Intent(this, CreateStaffAccountActivity::class.java)
            startActivity(intent)
        }
    }
}
