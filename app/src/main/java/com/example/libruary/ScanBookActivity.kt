package com.example.libruary

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.libruary.api.ApiClient
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanBookActivity : AppCompatActivity() {

    private lateinit var cameraPreview: PreviewView
    private var userId: Int = -1
    private lateinit var cameraExecutor: ExecutorService
    private var currentIsbn: String? = null
    private val REQUEST_CAMERA_PERMISSION = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_book)

        userId = intent.getIntExtra("userId", -1)
        cameraPreview = findViewById(R.id.cameraPreview)
        cameraExecutor = Executors.newSingleThreadExecutor()

        val btnCancel: Button = findViewById(R.id.btnCancel)
        btnCancel.setOnClickListener {
            val intent = Intent(this, AddBookActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(cameraPreview.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor, { imageProxy ->
                    processImage(imageProxy)
                })
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                Log.e("ScanBookActivity", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()
            scanner.process(image).addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    when (barcode.valueType) {
                        Barcode.TYPE_ISBN -> {
                            val isbn = barcode.rawValue
                            if (isbn != null && isbn != currentIsbn) {
                                currentIsbn = isbn
                                runOnUiThread {
                                    val tvIsbn: TextView = findViewById(R.id.tvIsbn)
                                    tvIsbn.text = "ISBN found: $isbn"
                                }
                                addBook(isbn, userId)
                            }
                        }
                    }
                }
                imageProxy.close()
            }.addOnFailureListener {
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }

    private fun addBook(isbn: String, userId: Int) {
        val sharedPreferences = getSharedPreferences("LibruaryPrefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("authToken", "")

        if (authToken.isNullOrEmpty()) {
            Toast.makeText(this, "Authentication token not found. Please log in again.", Toast.LENGTH_LONG).show()
            return
        }

        val addBookUrl = ApiClient.getInstance(this).getFullUrl("add_book")
        val params = JSONObject().apply {
            put("isbn", isbn)
            put("user_id", userId)
        }

        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, addBookUrl, params,
            Response.Listener {
                Toast.makeText(this, "Book added successfully!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            },
            Response.ErrorListener { error ->
                val errorMsg = error.networkResponse?.let {
                    String(it.data)
                } ?: "Unknown error"
                Toast.makeText(this, "Failed to add book: $errorMsg", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["x-access-token"] = authToken
                return headers
            }
        }

        ApiClient.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = arrayOf(Manifest.permission.CAMERA).all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
