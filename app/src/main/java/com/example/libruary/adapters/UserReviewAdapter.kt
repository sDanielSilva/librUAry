package com.example.libruary.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.libruary.R
import com.example.libruary.models.UserReview

class UserReviewAdapter : RecyclerView.Adapter<UserReviewAdapter.UserReviewViewHolder>() {
    private var userReviews: List<UserReview> = listOf()

    fun updateUserReviews(newReviews: List<UserReview>) {
        userReviews = newReviews
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReviewViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_user_review, parent, false)
        return UserReviewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserReviewViewHolder, position: Int) {
        val userReview = userReviews[position]
        holder.bookTitle.text = "Book title: ${userReview.book_title}"
        holder.reviewTextView.text = "Review: ${userReview.review_text}"
        holder.ratingTextView.text = "Rating: ${userReview.rating}"

    }


    override fun getItemCount(): Int {
        return userReviews.size
    }

    class UserReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reviewTextView: TextView = itemView.findViewById(R.id.reviewText)
        val ratingTextView: TextView = itemView.findViewById(R.id.ratingText)
        val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
    }
}
