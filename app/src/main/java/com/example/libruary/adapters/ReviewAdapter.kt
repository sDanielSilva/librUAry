package com.example.libruary.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.libruary.R
import com.example.libruary.models.Review

class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private var reviewList: List<Review>? = null

    fun submitList(reviews: List<Review>?) {
        reviewList = reviews
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        reviewList?.get(position)?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return reviewList?.size ?: 0
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reviewTextView: TextView = itemView.findViewById(R.id.reviewText)
        private val ratingTextView: TextView = itemView.findViewById(R.id.ratingText)

        fun bind(review: Review) {
            reviewTextView.text = review.review
            ratingTextView.text = "Rating: ${review.rating}"
        }
    }
}
