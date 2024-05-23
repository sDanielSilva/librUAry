package com.example.libruary

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.libruary.api.ApiClient
import com.example.libruary.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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

            if (review.isNotEmpty() && rating > 0) {
                addReview(bookId, review, rating)
            } else {
                Toast.makeText(this, "Please fill all fields and provide a rating", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addReview(bookId: Int, review: String, rating: Int) {
        ApiClient.apiService.addReview(bookId, Review(review, rating)).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddReviewActivity, "Review added successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddReviewActivity, "Failed to add review: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@AddReviewActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
