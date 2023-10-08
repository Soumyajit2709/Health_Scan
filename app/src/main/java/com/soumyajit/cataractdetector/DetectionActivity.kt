package com.soumyajit.cataractdetector

import android.content.ContentValues
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.soumyajit.cataractdetector.Classify
import com.soumyajit.cataractdetector.DiseaseDescription
import com.soumyajit.cataractdetector.R
import com.soundcloud.android.crop.Crop
import java.io.File

class DetectionActivity : AppCompatActivity() {

    private lateinit var takePictureButton: Button
    private lateinit var imageView: ImageView
    private lateinit var eyeMessage: ImageView
    private var chosen: String? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detection)


        takePictureButton = findViewById(R.id.button)
        eyeMessage = findViewById(R.id.img_eye)
        val dis = DiseaseDescription()
        //eyeMessage.text = dis.eye_info

        // Check and request permissions
        checkAndRequestPermissions()

        takePictureButton.setOnClickListener {
            // Filename in assets
            chosen = "assets/akha.tflite"
            openCameraIntent()
        }
    }

    private fun checkAndRequestPermissions() {
        val cameraPermission = android.Manifest.permission.CAMERA
        val writeStoragePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        val readStoragePermission = android.Manifest.permission.READ_EXTERNAL_STORAGE

        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, cameraPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(cameraPermission)
        }

        if (ContextCompat.checkSelfPermission(this, writeStoragePermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(writeStoragePermission)
        }

        if (ContextCompat.checkSelfPermission(this, readStoragePermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(readStoragePermission)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), REQUEST_PERMISSION)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted
            } else {
                // Permission denied, handle it as needed
                Toast.makeText(
                    applicationContext,
                    "This application needs camera and storage permissions to run. Application now closing.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }

    private fun openCameraIntent() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Photo")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        startActivityForResult(intent, REQUEST_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            try {
                val sourceUri = imageUri
                val destUri = Uri.fromFile(File(cacheDir, "cropped"))
                Crop.of(sourceUri, destUri).asSquare().start(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            imageUri = Crop.getOutput(data)
            val i = Intent(this, Classify::class.java)
            i.putExtra("resID_uri", imageUri)
            i.putExtra("chosen", chosen)
            startActivity(i)
        }
    }

    companion object {
        private const val REQUEST_IMAGE = 100
        private const val REQUEST_PERMISSION = 300
    }
}
