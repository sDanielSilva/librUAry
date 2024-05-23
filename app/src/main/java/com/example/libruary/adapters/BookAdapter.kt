package com.example.libruary.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.libruary.models.Book
import com.example.libruary.R

class BookAdapter(private val onItemClick: (Book) -> Unit) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private val books: MutableList<Book> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.bind(book)
        holder.itemView.setOnClickListener { onItemClick(book) }
    }

    override fun getItemCount(): Int {
        return books.size
    }

    fun submitList(newList: List<Book>) {
        books.clear()
        books.addAll(newList)
        notifyDataSetChanged()
    }

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val authorTextView: TextView = itemView.findViewById(R.id.authorTextView)

        fun bind(book: Book) {
            titleTextView.text = book.title
            authorTextView.text = book.author
        }
    }
}
