package com.example.libruary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.example.libruary.api.ApiClient
import com.example.libruary.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.libruary.adapters.BookAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var bookAdapter: BookAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        bookAdapter = BookAdapter { book -> showBookDetails(book) }
        recyclerView.adapter = bookAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { fetchBooks(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        fetchBooks("")
    }

    private fun fetchBooks(query: String) {
        ApiClient.apiService.getBooks(query).enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                if (response.isSuccessful) {
                    val books = response.body()?.books
                    books?.let {
                        bookAdapter.submitList(it)
                    }
                }
            }
            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                // Handle error
            }
        })
    }


    private fun showBookDetails(book: Book) {
        val intent = Intent(this, BookDetailActivity::class.java).apply {
            putExtra("BOOK_ID", book.id)
        }
        startActivity(intent)
    }
}
