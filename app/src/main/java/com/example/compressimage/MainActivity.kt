package com.example.compressimage

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var displayImage: ImageView? = null
    private var selectImage: Button? = null
    private var uploadWithoutCompress: Button? = null
    private var uploadWithCompress: Button? = null

    private val REQUEST_CODE = 101
    private var imageUri: Uri? = null

    private var mLoadingBar: ProgressDialog? = null

    private var bmp: Bitmap? = null
    private var baos: ByteArrayOutputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        displayImage = findViewById(R.id.imageView)
        selectImage = findViewById(R.id.selectImage)
        uploadWithoutCompress = findViewById(R.id.upload)
        uploadWithCompress = findViewById(R.id.uploadWithCompress)

        val storageRef = Firebase.storage.reference.child("uploade")

        selectImage?.setOnClickListener {
            // For getting images
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE)
        }

        uploadWithCompress?.setOnClickListener {
            if (imageUri != null) {
                uploadImageWithCompress(storageRef)
            }


        }
        uploadWithoutCompress?.setOnClickListener {
            if (imageUri != null) {
                uploadImageWithoutCompress(storageRef)
            }
        }
    }
    private fun uploadImageWithCompress(storageRef: StorageReference) {
        mLoadingBar = ProgressDialog(this)
        mLoadingBar?.setTitle("Uploading Image with Compress")
        mLoadingBar?.setCanceledOnTouchOutside(false)
        mLoadingBar?.show()
        val timestamp = System.currentTimeMillis().toString()
        bmp = try {
            MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        bmp?.let {
            baos = ByteArrayOutputStream()
            // Here we can choose the quality factor (e.g., 25)
            it.compress(Bitmap.CompressFormat.JPEG, 25, baos)

            val fileInBytes = baos?.toByteArray()
              storageRef.child(timestamp).putBytes(fileInBytes!!)
                .addOnSuccessListener {
                    mLoadingBar?.dismiss()
                    Toast.makeText(this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    mLoadingBar?.dismiss()
                    Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun uploadImageWithoutCompress(storageRef: StorageReference) {
        mLoadingBar = ProgressDialog(this)
        mLoadingBar?.setTitle("Uploading Image without Compress")
        mLoadingBar?.setCanceledOnTouchOutside(false)
        mLoadingBar?.show()

        val timestamp = System.currentTimeMillis().toString()

        imageUri?.let {
            storageRef.child(timestamp).putFile(it)
                .addOnSuccessListener {
                    mLoadingBar?.dismiss()
                    Toast.makeText(this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    mLoadingBar?.dismiss()
                    Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            displayImage?.setImageURI(imageUri)
        }
    }
}
