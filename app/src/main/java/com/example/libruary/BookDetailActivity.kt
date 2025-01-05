package com.example.libruary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.example.libruary.adapters.ApiReviewAdapter
import com.example.libruary.adapters.UserReviewAdapter
import com.example.libruary.api.ApiClient
import com.example.libruary.models.ApiReview
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject

class BookDetailActivity : AppCompatActivity() {


    private var bookId: Int = 0
    private var userId: Int = -1
    private var authToken: String? = null
    private lateinit var bookImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var authorTextView: TextView
    private lateinit var publishedDateTextView: TextView
    private lateinit var publisherTextView: TextView
    private lateinit var pagesTextView: TextView
    private lateinit var synopsisTextView: TextView
    private lateinit var isbnTextView: TextView
    private lateinit var languageTextView: TextView
    private lateinit var addReviewButton: Button
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var apiReviewAdapter: ApiReviewAdapter
    private lateinit var pageSpinner: Spinner
    private var totalPages: Int = 0
    private var selectedPage = 1
    private var isLoading = false
    private val pageSize = 5
    private lateinit var removeBookButton: Button
    private lateinit var markAsReadButton: Button
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    val REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        apiReviewAdapter = ApiReviewAdapter()
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView)
        reviewsRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewsRecyclerView.adapter = apiReviewAdapter
        pageSpinner = findViewById(R.id.pageSpinner)
        bookImageView = findViewById(R.id.bookImage)
        titleTextView = findViewById(R.id.title)
        authorTextView = findViewById(R.id.author)
        languageTextView = findViewById(R.id.language)
        isbnTextView = findViewById(R.id.isbn)
        synopsisTextView = findViewById(R.id.synopsis)
        publishedDateTextView = findViewById(R.id.publishedDate)
        publisherTextView = findViewById(R.id.publisher)
        pagesTextView = findViewById(R.id.pages)
        addReviewButton = findViewById(R.id.addReviewButton)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        bookId = intent.getIntExtra("BOOK_ID", 0)
        val sharedPreferences = getSharedPreferences("LibruaryPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("userId", -1)
        authToken = sharedPreferences.getString("authToken", "")

        if (authToken != null) {
            fetchBookDetails(bookId, authToken!!)
            fetchBookReviews(bookId, authToken!!, 1)
            fetchUserRead(bookId, authToken!!, userId)
            setupRemoveBookButton()
        } else {
            promptForLogin()
        }


        addReviewButton.setOnClickListener {
            val intent = Intent(this, AddReviewActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            startActivityForResult(intent, REQUEST_CODE)
        }



        pageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedPage = parent.getItemAtPosition(position).toString().toInt()
                fetchBookReviews(bookId, authToken!!, selectedPage)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        markAsReadButton = findViewById(R.id.markAsReadButton)
        markAsReadButton.setOnClickListener {
            markBookAsRead(bookId, authToken!!)
        }

    }
    private fun setupRemoveBookButton() {
        removeBookButton = findViewById(R.id.removeBookButton)
        removeBookButton.setOnClickListener {

            removeBookFromLibrary(bookId, authToken!!)
        }
    }

    private fun markBookAsRead(bookId: Int, token: String) {
        val markAsReadUrl = ApiClient.getInstance(this).getFullUrl("mark_book_as_read")
        val jsonBody = JSONObject()
        jsonBody.put("book_id", bookId)

        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, markAsReadUrl, jsonBody,
            Response.Listener { response ->
                Toast.makeText(this, "Book read status toggled successfully!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK) },
            Response.ErrorListener { error ->
                val errorMsg = error.networkResponse?.let {
                    String(it.data)
                } ?: "Unknown error"
                Toast.makeText(this, "Error marking book as read: $errorMsg", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["x-access-token"] = token
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        ApiClient.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private fun removeBookFromLibrary(bookId: Int, token: String) {
        val removeBookUrl = ApiClient.getInstance(this).getFullUrl("remove_book")
        val jsonBody = JSONObject()
        jsonBody.put("book_id", bookId)
        jsonBody.put("action", "delete")

        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, removeBookUrl, jsonBody,
            Response.Listener { response ->
                Toast.makeText(this, "Book removed successfully.", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()

            },
            Response.ErrorListener { error ->
                val errorMsg = error.networkResponse?.let {
                    String(it.data)
                } ?: "Unknown error"
                Toast.makeText(this, "Error removing book: $errorMsg", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["x-access-token"] = token
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        ApiClient.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private fun promptForLogin() {
        Toast.makeText(this, "Authentication token not found. Please log in again.", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchBookDetails(bookId: Int, token: String) {
        val bookDetailsUrl = ApiClient.getInstance(this).getFullUrl("book/$bookId")
        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET, bookDetailsUrl, null,
            Response.Listener { response ->
                try {
                    val bookJson = response.getJSONObject("book")
                    titleTextView.text = "Title: ${bookJson.optString("title").takeUnless{ it == "null" || it.isEmpty() } ?: "Unknown"}"
                    authorTextView.text = Html.fromHtml("<b>Author(s):</b> ${bookJson.optString("author").takeUnless { it == "null" || it.isEmpty() } ?: "Unknown"}", Html.FROM_HTML_MODE_COMPACT)
                    publishedDateTextView.text = Html.fromHtml("<b>Published Date:</b> ${bookJson.optString("published_date").takeIf { it.isNotEmpty() && it != "0" } ?: "Unknown"}", Html.FROM_HTML_MODE_COMPACT)
                    publisherTextView.text = Html.fromHtml("<b>Publisher:</b> ${bookJson.optString("publisher").takeUnless { it == "null" || it.isEmpty() } ?: "Unknown"}", Html.FROM_HTML_MODE_COMPACT)
                    pagesTextView.text = Html.fromHtml("<b>Pages:</b> ${bookJson.optString("pages").takeIf { it != "0" } ?: "Unknown"}", Html.FROM_HTML_MODE_COMPACT)
                    isbnTextView.text = Html.fromHtml("<b>ISBN:</b> ${bookJson.optString("isbn").takeUnless { it == "null" || it.isEmpty() } ?: "Unknown"}", Html.FROM_HTML_MODE_COMPACT)
                    languageTextView.text = Html.fromHtml("<b>Language:</b> ${bookJson.optString("language").takeUnless { it == "null" || it.isEmpty() } ?: "Unknown"}", Html.FROM_HTML_MODE_COMPACT)
                    synopsisTextView.text = Html.fromHtml("<b>Synopsis:</b> ${bookJson.optString("synopsis").takeUnless { it == "null" || it.isEmpty() } ?: "Unknown"}", Html.FROM_HTML_MODE_COMPACT)


                    val imageUrl = bookJson.optString("image")
                    Glide.with(this@BookDetailActivity)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(bookImageView)

                    fetchUserRating(bookId, token, userId)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@BookDetailActivity,
                        "Error processing response: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                Toast.makeText(
                    this@BookDetailActivity,
                    "Error fetching book details: ${error.message}",
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

    private fun fetchBookReviews(bookId: Int, token: String, page: Int) {
        if (isLoading) return
        isLoading = true
        val bookReviewsUrl = ApiClient.getInstance(this).getFullUrl("book_reviews/$bookId?page=$page&per_page=$pageSize")
        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET, bookReviewsUrl, null,
            Response.Listener { response ->
                try {
                    if (!response.has("reviews") || response.isNull("reviews")) {
                        Toast.makeText(this, "Reviews not available.", Toast.LENGTH_SHORT).show()
                        return@Listener
                    }
                    val reviewsArray = response.getJSONArray("reviews")
                    val newReviews = ArrayList<ApiReview>()
                    for (i in 0 until reviewsArray.length()) {
                        val reviewObj = reviewsArray.getJSONObject(i)
                        val apiReview = ApiReview(
                            id = reviewObj.getInt("id"),
                            username = reviewObj.getString("username"),
                            reviewText = reviewObj.getString("review"),
                            rating = reviewObj.getDouble("rating").toFloat(),
                            userId = reviewObj.getInt("user_id")
                        )
                        newReviews.add(apiReview)
                    }

                    apiReviewAdapter.clearReviews()
                    apiReviewAdapter.addReviews(newReviews)
                    isLoading = false

                    totalPages = response.getInt("pages")
                    val pageNumbers = (1..totalPages).map { it.toString() }
                    val spinnerAdapter = ArrayAdapter(this@BookDetailActivity, android.R.layout.simple_spinner_item, pageNumbers)
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    pageSpinner.adapter = spinnerAdapter
                    spinnerAdapter.notifyDataSetChanged()
                    pageSpinner.setSelection(selectedPage - 1, false)

                } catch (e: JSONException) {
                    Toast.makeText(this@BookDetailActivity, "Error processing reviews.", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                error.networkResponse?.let {
                    val errorData = String(it.data)
                    Toast.makeText(this@BookDetailActivity, "Error fetching reviews: $errorData", Toast.LENGTH_LONG).show()
                } ?: run {
                    Toast.makeText(this@BookDetailActivity, "Network error fetching reviews.", Toast.LENGTH_SHORT).show()
                }
            }
            ) {
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
                    val rating = response.getInt("rating")
                    runOnUiThread {
                        val buttonText = if (rating == 0) "Add Review" else "Edit Review"
                        addReviewButton.text = buttonText
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

    private fun fetchUserRead(bookId: Int, token: String, userId: Int) {
        val userRatingUrl = ApiClient.getInstance(this).getFullUrl("user_read/$userId/$bookId")
        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET, userRatingUrl, null,
            Response.Listener { response ->
                try {
                    val hasRead = response.getBoolean("has_read")
                    runOnUiThread {
                        val buttonText = if (hasRead) "Unmark as read" else "Mark as read"
                        markAsReadButton.text = buttonText
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            fetchBookReviews(bookId, authToken!!, 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
