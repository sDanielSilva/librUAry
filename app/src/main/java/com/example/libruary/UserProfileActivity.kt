package com.example.libruary

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libruary.adapters.ReviewAdapter
import com.example.libruary.api.ApiClient
import com.example.libruary.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UserProfileActivity : AppCompatActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var userReviewsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        usernameTextView = findViewById(R.id.username)
        userReviewsRecyclerView = findViewById(R.id.userReviewsRecyclerView)

        userReviewsRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter()
        userReviewsRecyclerView.adapter = reviewAdapter

        fetchUserProfile()
        fetchUserReviews()
    }

    private fun fetchUserProfile() {
        ApiClient.apiService.getUserProfile().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        usernameTextView.text = user.username
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                // Handle error
            }
        })
    }

    private fun fetchUserReviews() {
        ApiClient.apiService.getUserReviews().enqueue(object : Callback<ReviewResponse> {
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
