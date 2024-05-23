package com.example.libruary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.libruary.api.ApiClient
import com.example.libruary.models.*
import com.example.libruary.adapters.ReviewAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookDetailActivity : AppCompatActivity() {

    private lateinit var bookImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var authorTextView: TextView
    private lateinit var publishedDateTextView: TextView
    private lateinit var publisherTextView: TextView
    private lateinit var pagesTextView: TextView
    private lateinit var addReviewButton: Button
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private var bookId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        bookImageView = findViewById(R.id.bookImage)
        titleTextView = findViewById(R.id.title)
        authorTextView = findViewById(R.id.author)
        publishedDateTextView = findViewById(R.id.publishedDate)
        publisherTextView = findViewById(R.id.publisher)
        pagesTextView = findViewById(R.id.pages)
        addReviewButton = findViewById(R.id.addReviewButton)
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView)

        bookId = intent.getIntExtra("BOOK_ID", 0)

        reviewsRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter()
        reviewsRecyclerView.adapter = reviewAdapter

        fetchBookDetails(bookId)
        fetchBookReviews(bookId)

        addReviewButton.setOnClickListener {
            val intent = Intent(this, AddReviewActivity::class.java).apply {
                putExtra("BOOK_ID", bookId)
            }
            startActivity(intent)
        }
    }

    private fun fetchBookDetails(bookId: Int) {
        ApiClient.apiService.getBookDetails(bookId).enqueue(object : Callback<Book> {
            override fun onResponse(call: Call<Book>, response: Response<Book>) {
                if (response.isSuccessful) {
                    response.body()?.let { book ->
                        titleTextView.text = book.title
                        authorTextView.text = book.author
                        publishedDateTextView.text = book.published_date.toString()
                        publisherTextView.text = book.publisher
                        pagesTextView.text = book.pages.toString()
                        Glide.with(this@BookDetailActivity).load(book.image).into(bookImageView)
                    }
                }
            }

            override fun onFailure(call: Call<Book>, t: Throwable) {
                // Handle error
            }
        })
    }

    private fun fetchBookReviews(bookId: Int) {
        ApiClient.apiService.getBookReviews(bookId).enqueue(object : Callback<ReviewResponse> {
            override fun onResponse(call: Call<ReviewResponse>, response: Response<ReviewResponse>) {
                if (response.isSuccessful) {
                    reviewAdapter.submitList(response.body()?.reviews)
                }
            }

            override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                // Handle error
            }
        })
    }
}
