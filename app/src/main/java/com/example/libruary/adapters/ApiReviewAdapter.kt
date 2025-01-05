package com.example.libruary.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.libruary.R
import com.example.libruary.models.ApiReview

class ApiReviewAdapter : RecyclerView.Adapter<ApiReviewAdapter.ApiReviewViewHolder>() {
    private val reviews = mutableListOf<ApiReview>()

    fun clearReviews() {
        reviews.clear()
        notifyDataSetChanged()
    }


    fun addReviews(newReviews: List<ApiReview>) {
        val currentSize = reviews.size
        reviews.addAll(newReviews)
        notifyItemRangeInserted(currentSize, newReviews.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApiReviewViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false)
        return ApiReviewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ApiReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.bind(review)
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    class ApiReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reviewTextView: TextView = itemView.findViewById(R.id.reviewTextView)
        val reviewRatingBar: androidx.appcompat.widget.AppCompatRatingBar = itemView.findViewById(R.id.reviewRatingBar)
        val reviewAuthorTextView: TextView = itemView.findViewById(R.id.reviewAuthorTextView)

        fun bind(review: ApiReview) {
            reviewTextView.text = review.reviewText
            reviewRatingBar.rating = review.rating.toFloat()
            reviewAuthorTextView.text = "User: ${review.username}"
        }
    }
}
