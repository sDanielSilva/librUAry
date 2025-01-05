// BookAdapter.kt
package com.example.libruary.adapters

import android.content.Intent
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.libruary.AddReviewActivity
import com.example.libruary.models.Book
import com.example.libruary.R
import android.content.Context

class BookAdapter(private val onItemClick: (Book) -> Unit) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private val books: MutableList<Book> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])

    }

    override fun getItemCount(): Int = books.size

    fun submitList(newList: List<Book>) {
        books.clear()
        books.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateRating(bookId: Int, newRating: Int) {
        var book = books.find { it.id == bookId }
        book?.rating = newRating
        notifyDataSetChanged()
    }


    class BookViewHolder(itemView: View, private val onItemClick: (Book) -> Unit) : RecyclerView.ViewHolder(itemView) {
        val context = itemView.context
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val authorTextView: TextView = itemView.findViewById(R.id.authorTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)


        fun bind(book: Book) {
            titleTextView.text = book.title
            authorTextView.text = book.author
            ratingBar.rating = book.rating.toFloat()
            Glide.with(itemView.context)
                .load(book.image)
                .placeholder(R.drawable.placeholder_image)
                .into(imageView)

            itemView.setOnClickListener {
                it.animate().setDuration(200).scaleX(0.95f).scaleY(0.95f).withEndAction {
                    it.animate().setDuration(200).scaleX(1f).scaleY(1f).withEndAction {
                        onItemClick(book)
                    }
                }
            }
        }
    }
}


