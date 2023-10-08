package com.soumyajit.cataractdetector

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class Classify : AppCompatActivity() {

    // Presets for RGB conversion
    private val RESULTS_TO_SHOW = 1
    private val IMAGE_MEAN = 64
    private val IMAGE_STD = 64.0f

    // Options for the model interpreter
    private val tfliteOptions = Interpreter.Options()
    // TensorFlow Lite graph
    private lateinit var tflite: Interpreter
    // Holds all the possible labels for the model
    private lateinit var labelList: List<String>
    // Holds the selected image data as bytes
    private var imgData: ByteBuffer? = null
    // Holds the probabilities of each label for non-quantized graphs
    private lateinit var labelProbArray: Array<FloatArray>
    // Array that holds the labels with the highest probabilities
    private var topLabels: String? = null
    // Array that holds the highest probabilities
    private var topConfidence: String? = null

    // Selected classifier information received from extras
    private var chosen: String? = null
    // Input image dimensions for the Inception Model
    private val DIM_IMG_SIZE_X = 64
    private val DIM_IMG_SIZE_Y = 64
    private val DIM_PIXEL_SIZE = 3
    // Int array to hold image data
    private lateinit var intValues: IntArray

    // Activity elements
    private lateinit var selectedImage: ImageView
    private lateinit var classifyButton: Button
    private lateinit var backButton: Button
    private lateinit var label1: TextView

    // Priority queue that will hold the top results from the CNN
    private val sortedLabels = PriorityQueue<Map.Entry<String, Float>>(
        RESULTS_TO_SHOW,
        Comparator { o1, o2 -> o1.value.compareTo(o2.value) }
    )

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Get all selected classifier data from extras
        chosen = intent.getStringExtra("chosen")
        // Initialize array that holds image data
        intValues = IntArray(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classify);
        // Initialize graph and labels
        labelList = loadLabelList()
        tflite = Interpreter(loadModelFile(), tfliteOptions)
        // Initialize byte array. The size depends on whether the input data needs to be quantized or not
        imgData = ByteBuffer.allocateDirect(4 * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE)
        imgData?.order(ByteOrder.nativeOrder())
        // Initialize probabilities array. The data types that the array holds depend on whether the input data needs to be quantized or not
        labelProbArray = Array(1) { FloatArray(labelList.size) }

        // Labels that hold top three results of CNN
        label1 = findViewById(R.id.label1)
        // Initialize ImageView that displays the selected image to the user
        selectedImage = findViewById(R.id.selected_image)

        // Allows the user to go back to the previous activity to select a different image
        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            val i = Intent(this@Classify, DetectionActivity::class.java)
            startActivity(i)
        }

        // Classify the currently displayed image
        classifyButton = findViewById(R.id.classify_image)
        classifyButton.setOnClickListener {
            // Get the current bitmap from the ImageView
            val bitmapOrig = (selectedImage.drawable as BitmapDrawable).bitmap
            // Resize the bitmap to the required input size for the CNN
            val bitmap = getResizedBitmap(bitmapOrig, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y)
            // Convert the bitmap to a byte array
            convertBitmapToByteBuffer(bitmap)
            // Pass the byte data to the graph
            tflite.run(imgData, labelProbArray)
            // Display the results
            printTopKLabels()
        }

        // Get the image from the previous activity to show in the ImageView
        val uri = intent.getParcelableExtra<Uri>("resID_uri")
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            selectedImage.setImageBitmap(bitmap)
            // Not sure why this happens, but without this, the image appears on its side
            selectedImage.rotation = selectedImage.rotation + 0
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Loads the tflite graph from a file
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("akha.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Converts bitmap to byte array which is passed to the tflite graph
    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        imgData?.rewind()
        bitmap.getPixels(
            intValues,
            0,
            bitmap.width,
            0,
            0,
            bitmap.width,
            bitmap.height
        )
        var pixel = 0
        for (i in 0 until DIM_IMG_SIZE_X) {
            for (j in 0 until DIM_IMG_SIZE_Y) {
                val value = intValues[pixel++]
                imgData?.putFloat((((value shr 16) and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData?.putFloat((((value shr 8) and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData?.putFloat(((value and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
    }

    // Loads the labels from the label txt file in assets into a string array
    private fun loadLabelList(): List<String> {
        val labelList = mutableListOf<String>()
        val reader = BufferedReader(InputStreamReader(assets.open("label.txt")))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            labelList.add(line!!)
        }
        reader.close()
        return labelList
    }

    // Print the top labels and respective confidences
    private fun printTopKLabels() {
        var result: Float
        // Add all results to priority queue
        sortedLabels.add(AbstractMap.SimpleEntry(labelList[0], labelProbArray[0][0]))
        // Get top results from the priority queue
        val size = sortedLabels.size
        val label = sortedLabels.poll()
        topLabels = label.key
        result = label.value
        if (result < 0.6 && result >= 0.2) {
            topLabels = "Early stage"
            result = 1 - result
        }
        if (result < 0.2) {
            topLabels = "Cataract"
            result = 1 - result
        }
        topConfidence = String.format("%.00f%%", label.value * 100)
        label1.text = "1. $topLabels"
    }

    // Resizes bitmap to given dimensions
    private fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
    }
}
