package com.example.snapfine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class StaffHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_home)

        val btnReviewUsers = findViewById<android.widget.Button>(R.id.btnReviewUsers)
        btnReviewUsers.setOnClickListener {
            val intent = android.content.Intent(this, UserVerificationActivity::class.java)
            startActivity(intent)
        }
    }
}
