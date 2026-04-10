package com.example.snapfine

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var fAuth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        
        val mEmail = findViewById<EditText>(R.id.email)
        val mPassword = findViewById<EditText>(R.id.password)
        val mRadioGroup = findViewById<RadioGroup>(R.id.rgRole)
        progressBar = findViewById(R.id.progressBar2)
        fAuth = FirebaseAuth.getInstance()
        val mLoginBtn = findViewById<Button>(R.id.loginbtn)
        val mCreateBtn = findViewById<TextView>(R.id.textlogin)
        val mForgotBtn = findViewById<TextView>(R.id.tvForgotPassword)

        mForgotBtn.setOnClickListener {
            val resetMail = EditText(it.context)
            val passwordResetDialog = androidx.appcompat.app.AlertDialog.Builder(it.context)
            passwordResetDialog.setTitle("Reset Password?")
            passwordResetDialog.setMessage("Enter Your Email To Received Reset Link.")
            passwordResetDialog.setView(resetMail)

            passwordResetDialog.setPositiveButton("Yes") { _, _ ->
                val mail = resetMail.text.toString().trim()
                if (mail.isNotEmpty()) {
                    fAuth.sendPasswordResetEmail(mail).addOnSuccessListener {
                        Toast.makeText(this, "Reset Link Sent To Your Email.", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Error! Reset Link is Not Sent ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show()
                }
            }

            passwordResetDialog.setNegativeButton("No") { _, _ -> }
            passwordResetDialog.create().show()
        }

        mLoginBtn.setOnClickListener {
            val email = mEmail.text.toString().trim()
            val password = mPassword.text.toString().trim()

            if (email.isEmpty()) {
                mEmail.error = "Email is Required."
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                mPassword.error = "Password is Required."
                return@setOnClickListener
            }

            if (password.length < 6) {
                mPassword.error = "Password must be >= 6 characters"
                return@setOnClickListener
            }

            val selectedId = mRadioGroup.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an account type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRole = when (selectedId) {
                R.id.rbAdmin -> "admin"
                R.id.rbStaff -> "staff"
                else -> "citizen"
            }

            progressBar.visibility = View.VISIBLE

            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = fAuth.currentUser?.uid
                    if (userId != null) {
                        routeUserBasedOnRole(this, userId, selectedRole)
                    } else {
                        val intent = Intent(applicationContext, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Error! ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }
        }

        mCreateBtn.setOnClickListener {
            startActivity(Intent(applicationContext, LoginActivity::class.java))
        }
    }
}
