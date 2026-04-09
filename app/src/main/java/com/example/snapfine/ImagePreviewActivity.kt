package com.example.snapfine

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var btnRetake: MaterialButton
    private lateinit var btnUsePhoto: MaterialButton
    private lateinit var btnBack: ImageButton
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)

        ivPreview = findViewById(R.id.ivPreview)
        btnRetake = findViewById(R.id.btnRetake)
        btnUsePhoto = findViewById(R.id.btnUsePhoto)
        btnBack = findViewById(R.id.btnBack)

        val uriString = intent.getStringExtra("IMAGE_URI")
        if (uriString != null) {
            imageUri = Uri.parse(uriString)
            Glide.with(this)
                .load(imageUri)
                .into(ivPreview)
        }

        btnRetake.setOnClickListener {
            // Simply finish and return to camera
            finish()
        }

        btnUsePhoto.setOnClickListener {
            // Navigate to ReportViolationActivity with URI
            val intent = Intent(this, ReportViolationActivity::class.java).apply {
                putExtra("IMAGE_URI", imageUri?.toString())
            }
            startActivity(intent)
            finish() // Optional: Finish preview so they can't come back to it
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
