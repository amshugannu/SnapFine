package com.example.snapfine

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class splashscreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splashscreen)
        android.os.Handler().postDelayed({
            var i = Intent(this, LoginActivity::class.java)
            startActivity(i)
            finish()
        },2000)
    }
}
