package com.soumyajit.cataractdetector

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager

class MainActivity: AppCompatActivity() {

    private lateinit var list: ListView

    private val maintitle = arrayOf(
        "Cataract Detection", "Skin Cancer Detection",
        "Alzheimer Detection", "Covid-19 Detection",
        "Brain Cancer Detection", "Skin Disease Detection"
    )

    private val subtitle = arrayOf(
        "Helps to detect Cataract from normal eye image and AI-based predictions.",
        "Helps to detect Skin Cancer from normal skin image and AI-based predictions.",
        "Helps to detect Alzheimer from Brain MRI and AI-based predictions.",
        "Helps to detect Covid-19 from Chest X-ray and AI-based predictions.",
        "Helps to detect Brain Cancer from MRI from Chest X-ray and AI-based predictions.",
        "Helps to detect Skin disease from skin images and AI-based predictions."
    )

    private val imgid = arrayOf(
        R.drawable.cataract,
        R.drawable.skin_c,
        R.drawable.brain_a,
        R.drawable.covid,
        R.drawable.brain_c,
        R.drawable.skin_d
    )


    companion object {
        const val number_item = 14
        val Image_name = arrayOf(
            "d2", "d1", "d3", "d4", "d5", "d6", "d7", "d8", "d9", "d77", "d10", "d11", "d12", "d13"
        )
    }
    private lateinit var imageFragmentPagerAdapter: ImageFragmentPagerAdapter
    private lateinit var viewPager: ViewPager

    // Button for each available classifier
    private lateinit var akhaJach: Button

    // For permission requests
    private val REQUEST_PERMISSION = 300

    // Request code for permission requests to the OS for an image
    private val REQUEST_IMAGE = 100

    // Will hold the URI of the image obtained from the camera
    private var imageUri: Uri? = null

    // String to send to the next activity that describes the chosen classifier
    private var chosen: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = MyListAdapter(this, maintitle, subtitle, imgid)
        list = findViewById(R.id.list)
        list.adapter = adapter
        list.setOnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> {
                    // Code specific to the first list item
                    // Before using intent in any activity, add the class in the manifest file.
                    val i = Intent(applicationContext, DetectionActivity::class.java)
                    startActivity(i)
                }
                1, 2, 3, 4, 5 -> {
                    // Code specific to the 2nd, 3rd, 4th, and 5th list items
                    Toast.makeText(applicationContext, "Sorry, this module is under development.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Slide view setup
        imageFragmentPagerAdapter = ImageFragmentPagerAdapter(supportFragmentManager)
        viewPager = findViewById(R.id.pager)
        viewPager.adapter = imageFragmentPagerAdapter
    }
}

