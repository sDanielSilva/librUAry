package com.example.libruary

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libruary.adapters.UserReviewAdapter
import com.example.libruary.models.UserReview
import org.json.JSONArray

class UserReviewsFragment : Fragment() {
    private lateinit var reviewsRecyclerView: RecyclerView
    private val reviewAdapter by lazy { UserReviewAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_reviews, container, false)
        reviewsRecyclerView = view.findViewById<RecyclerView>(R.id.userReviewsRecyclerView)
        reviewsRecyclerView.layoutManager = LinearLayoutManager(context)
        reviewsRecyclerView.adapter = reviewAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as UserProfileActivity).userViewModel.reviews.observe(viewLifecycleOwner) { reviewsJsonArray ->
            updateUserReviews(reviewsJsonArray)
        }
    }

    fun updateUserReviews(reviewsJsonArray: JSONArray) {
        val reviewsList = ArrayList<UserReview>()
        for (i in 0 until reviewsJsonArray.length()) {
            val reviewObj = reviewsJsonArray.getJSONObject(i)
            val review = UserReview(
                book_id = reviewObj.getInt("book_id"),
                book_title = reviewObj.getString("book_title"),
                review_text = reviewObj.getString("review"),
                rating = reviewObj.getInt("rating")
            )
            Log.d("UserReviewsFragment", "Review: $review")
            reviewsList.add(review)
        }
        reviewAdapter.updateUserReviews(reviewsList)
    }
}