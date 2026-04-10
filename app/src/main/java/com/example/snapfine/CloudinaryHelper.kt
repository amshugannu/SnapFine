package com.example.snapfine

import android.content.Context
import android.net.Uri
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object CloudinaryHelper {

    private const val CLOUD_NAME = "drrfniibc"
    private const val UPLOAD_PRESET = "snapfine_upload"
    private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    private val client = OkHttpClient()

    /**
     * Uploads an image to Cloudinary using OkHttp multipart request.
     * 
     * @param imageUri The local URI of the image to upload.
     * @param context Recommended for ContentResolver access.
     * @param onResult Callback returning the secure_url on success, or null on failure.
     */
    fun uploadImageToCloudinary(imageUri: Uri, context: Context, onResult: (String?) -> Unit) {
        val contentResolver = context.contentResolver
        
        try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes == null) {
                onResult(null)
                return
            }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg", bytes.toRequestBody("image/jpeg".toMediaTypeOrNull()))
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .build()

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onResult(null)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null) {
                            val json = JSONObject(body)
                            val secureUrl = json.optString("secure_url", null)
                            onResult(secureUrl)
                        } else {
                            onResult(null)
                        }
                    } else {
                        onResult(null)
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(null)
        }
    }
}
