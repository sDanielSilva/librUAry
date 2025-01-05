// AddReviewActivity.kt
package com.example.libruary

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.libruary.api.ApiClient
import org.json.JSONObject

class AddReviewActivity : AppCompatActivity() {

    private lateinit var reviewText: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var submitReviewButton: Button
    private var bookId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)

        reviewText = findViewById(R.id.reviewText)
        ratingBar = findViewById(R.id.ratingBar)
        submitReviewButton = findViewById(R.id.submitReviewButton)

        bookId = intent.getIntExtra("BOOK_ID", 0)

        submitReviewButton.setOnClickListener {
            val review = reviewText.text.toString().trim()
            val rating = ratingBar.rating.toInt()
            val sharedPreferences = getSharedPreferences("LibruaryPrefs", Context.MODE_PRIVATE)
            val authToken = sharedPreferences.getString("authToken", null)
            val userId = sharedPreferences.getInt("userId", -1)

            if (review.isNotEmpty() && rating > 0 && authToken != null) {
                addReview(bookId, userId, review, rating, authToken)
            } else {
                Toast.makeText(this, "Please fill in all fields and provide a rating", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addReview(bookId: Int, userId: Int, reviewText: String, rating: Int, token: String?) {
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Authentication token is missing. Please log in again.", Toast.LENGTH_LONG).show()
            return
        }

        val reviewUrl = ApiClient.getInstance(this).getFullUrl("review")
        val params = JSONObject()
        params.put("book_id", bookId)
        params.put("user_id", userId)
        params.put("review_text", reviewText)
        params.put("rating", rating)

        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, reviewUrl, params,
            Response.Listener {
                Toast.makeText(this, "Review added successfully!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            },
            Response.ErrorListener { error ->
                val errorMsg = error.networkResponse?.let {
                    String(it.data)
                } ?: "Unknown error"
                Toast.makeText(this, "Failed to add review: $errorMsg", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["x-access-token"] = token
                return headers
            }
        }

        ApiClient.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

}
