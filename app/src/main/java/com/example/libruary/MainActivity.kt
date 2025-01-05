// MainActivity.kt
package com.example.libruary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.libruary.adapters.BookAdapter
import com.example.libruary.api.ApiClient
import com.example.libruary.models.Book
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONException
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var booksToReadRecyclerView: RecyclerView
    private lateinit var booksReadRecyclerView: RecyclerView
    private lateinit var booksToReadAdapter: BookAdapter
    private lateinit var booksReadAdapter: BookAdapter
    private val booksToRead = ArrayList<Book>()
    private val booksRead = ArrayList<Book>()
    private var booksToFetch = 0
    private var booksFetched = 0
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var addBookButton: FloatingActionButton
    private lateinit var booksToReadLabel: TextView
    private lateinit var booksReadLabel: TextView
    private var userId: Int = -1
    private var authToken: String? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addBookButton = findViewById(R.id.addBookButton)
        bottomNavigationView = findViewById(R.id.navigation)
        booksToReadLabel = findViewById(R.id.booksToReadLabel)
        booksReadLabel = findViewById(R.id.booksReadLabel)

        booksToReadRecyclerView = findViewById(R.id.booksToReadRecyclerView)
        booksReadRecyclerView = findViewById(R.id.readBooksRecyclerView)

        booksToReadAdapter = BookAdapter { book -> showBookDetails(book) }
        booksReadAdapter = BookAdapter { book -> showBookDetails(book) }

        booksToReadRecyclerView.layoutManager = GridLayoutManager(this, 3)
        booksReadRecyclerView.layoutManager = GridLayoutManager(this, 3)

        booksToReadRecyclerView.adapter = booksToReadAdapter
        booksReadRecyclerView.adapter = booksReadAdapter

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_library -> {
                    if (this !is MainActivity) {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    true
                }

                R.id.navigation_profile -> {
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    true
                }

                else -> false
            }
        }

        val sharedPreferences = getSharedPreferences("LibruaryPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("userId", -1)
        authToken = sharedPreferences.getString("authToken", "")

        if (userId != -1 && authToken != null) {
            fetchUserBooks(userId, authToken!!)
        } else {
            Toast.makeText(
                this,
                "Usuário não identificado. Por favor, faça login novamente.",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        addBookButton.setOnClickListener {
            val intent = Intent(this, AddBookActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        bottomNavigationView.selectedItemId = R.id.navigation_library
        checkLoginAndFetchBooks()
    }

    private fun checkLoginAndFetchBooks() {
        val sharedPreferences = getSharedPreferences("LibruaryPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("userId", -1)
        authToken = sharedPreferences.getString("authToken", "")

        if (userId != -1 && authToken != null && authToken!!.isNotEmpty()) {
            booksToRead.clear()
            booksRead.clear()
            fetchUserBooks(userId, authToken!!)
        } else {
            Toast.makeText(this, "User not identified. Please log in again.", Toast.LENGTH_LONG)
                .show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun fetchUserBooks(userId: Int, token: String) {
        booksToRead.clear()
        booksRead.clear()
        val userBooksUrl = ApiClient.getInstance(this).getFullUrl("user_books/$userId")
        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET, userBooksUrl, null,
            Response.Listener { response ->
                coroutineScope.launch {
                    try {
                        val booksToReadArray = response.getJSONArray("books_to_read")
                        val booksReadArray = response.getJSONArray("books_read")

                        for (i in 0 until booksToReadArray.length()) {
                            val bookObject = booksToReadArray.getJSONObject(i)
                            val bookId = bookObject.getInt("book_id")
                            val ratingValue = bookObject.optString("rating", "0")
                            val rating =
                                if (ratingValue == "Not Rated") -1 else ratingValue.toIntOrNull() ?: 0
                            fetchBookDetails(bookId, token, rating, booksToRead)
                        }

                        for (i in 0 until booksReadArray.length()) {
                            val bookObject = booksReadArray.getJSONObject(i)
                            val bookId = bookObject.getInt("book_id")
                            val ratingValue = bookObject.optString("rating", "0")
                            val rating =
                                if (ratingValue == "Not Rated") -1 else ratingValue.toIntOrNull() ?: 0
                            fetchBookDetails(bookId, token, rating, booksRead)
                        }

                        runOnUiThread {
                            booksToReadLabel.text = "Books to read (${booksToReadArray.length()}):"
                            booksReadLabel.text = "Books read (${booksReadArray.length()}):"
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@MainActivity,
                            "Error processing response: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                Toast.makeText(
                    this@MainActivity,
                    "Error fetching user's books: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["x-access-token"] = token
                return headers
            }
            override fun getRetryPolicy(): RetryPolicy {
                return DefaultRetryPolicy(
                    5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            }
        }

        ApiClient.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private fun updateRecyclerViews() {
        booksToReadAdapter = BookAdapter { book -> showBookDetails(book) }
        booksReadAdapter = BookAdapter { book -> showBookDetails(book) }

        booksToReadRecyclerView.adapter = booksToReadAdapter
        booksReadRecyclerView.adapter = booksReadAdapter

        booksToReadAdapter.submitList(booksToRead)
        booksReadAdapter.submitList(booksRead)
    }

    private fun fetchBookDetails(
        bookId: Int,
        token: String,
        userRating: Int,
        bookList: ArrayList<Book>
    ) {
        val bookDetailsUrl = ApiClient.getInstance(this).getFullUrl("book/$bookId")
        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET, bookDetailsUrl, null,
            Response.Listener { response ->
                try {
                    val bookJson = response.getJSONObject("book")

                    val id = bookJson.optInt("id", -1)
                    val title = bookJson.optString("title", "Unknown Title")
                    val author = bookJson.optString("author", "Unknown Author")
                    val publishedDate =
                        bookJson.optString("published_date", "Unknown Publication Date")
                    val publisher = bookJson.optString("publisher", "Unknown Publisher")
                    val pages =
                        if (bookJson.has("pages") && !bookJson.isNull("pages")) bookJson.getInt("pages") else 0
                    val image = bookJson.optString("image", "")


                    val book =
                        Book(id, title, author, publishedDate, publisher, pages, image, userRating)
                    if (!bookList.contains(book)) {
                        bookList.add(book)
                    }

                    fetchUserRating(bookId, token, userId)

                    if (bookList == booksToRead) {
                        runOnUiThread {
                            booksToReadAdapter.submitList(booksToRead.toList())
                        }
                    } else if (bookList == booksRead) {
                        runOnUiThread {
                            booksReadAdapter.submitList(booksRead.toList())
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this,
                        "Error processing book details: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                Toast.makeText(
                    this,
                    "Error fetching book details: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["x-access-token"] = token
                return headers
            }
        }

        ApiClient.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private fun fetchUserRating(bookId: Int, token: String, userId: Int) {
        val userRatingUrl = ApiClient.getInstance(this).getFullUrl("user_rating/$userId/$bookId")
        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET, userRatingUrl, null,
            Response.Listener { response ->
                try {
                    val userRating = response.getInt("rating")
                    runOnUiThread {
                        booksToReadAdapter.updateRating(bookId, userRating)
                        booksReadAdapter.updateRating(bookId, userRating)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this,
                        "Error processing user rating: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                Toast.makeText(
                    this,
                    "Error fetching user rating: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["x-access-token"] = token
                return headers
            }
        }

        ApiClient.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    companion object {
        private const val REQUEST_CODE = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            fetchUserBooks(userId, authToken!!)
            updateRecyclerViews()
        }
    }

    private fun showBookDetails(book: Book) {
        val intent = Intent(this, BookDetailActivity::class.java)
        intent.putExtra("BOOK_ID", book.id)
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}