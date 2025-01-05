package com.example.libruary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.example.libruary.api.ApiClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import kotlinx.coroutines.*

class AddBookActivity : AppCompatActivity() {

    private lateinit var isbnEditText: EditText
    private lateinit var submitButton: Button
    private var userId: Int = -1
    private val SCAN_BOOK_ACTIVITY_REQUEST_CODE = 0
    private lateinit var bottomNavigationView: BottomNavigationView
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        isbnEditText = findViewById(R.id.isbnEditText)
        submitButton = findViewById(R.id.submitButton)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val sharedPreferences = getSharedPreferences("LibruaryPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("userId", -1)
        val authToken = sharedPreferences.getString("authToken", "")

        bottomNavigationView = findViewById(R.id.navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_library -> {
                    finish()
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        submitButton.setOnClickListener {
            val isbn = isbnEditText.text.toString()
            if (isbn.isNotEmpty() && authToken != null) {
                addBook(isbn, userId, authToken)
            } else {
                Toast.makeText(this, "Please enter an ISBN", Toast.LENGTH_SHORT).show()
            }
        }

        val scanButton: MaterialButton = findViewById(R.id.scanButton)
        scanButton.setOnClickListener {
            val intent = Intent(this, ScanBookActivity::class.java)
            intent.putExtra("userId", userId)
            startActivityForResult(intent, SCAN_BOOK_ACTIVITY_REQUEST_CODE)
        }
    }

    private fun addBook(isbn: String, userId: Int, token: String) {
        val addBookUrl = ApiClient.getInstance(this).getFullUrl("add_book")
        val params = JSONObject()
        params.put("isbn", isbn)
        params.put("user_id", userId)

        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, addBookUrl, params,
            Response.Listener { response ->
                coroutineScope.launch {
                    try {
                        Toast.makeText(this@AddBookActivity, "Book added successfully!", Toast.LENGTH_SHORT).show()
                        val mainIntent = Intent(this@AddBookActivity, MainActivity::class.java)
                        startActivity(mainIntent)
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@AddBookActivity, "Error processing response: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                Toast.makeText(
                    this@AddBookActivity,
                    "Error adding book: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["x-access-token"] = token
                return headers
            }
            override fun getRetryPolicy(): RetryPolicy {
                return DefaultRetryPolicy(
                    5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            }
        }

        ApiClient.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SCAN_BOOK_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
