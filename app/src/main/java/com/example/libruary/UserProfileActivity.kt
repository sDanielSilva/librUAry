package com.example.libruary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.viewpager2.widget.ViewPager2
import android.content.Intent
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.libruary.adapters.ViewPagerAdapter
import com.example.libruary.api.ApiClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import org.json.JSONObject

class UserProfileActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.navigation_profile

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // ViewPager and TabLayout configuration
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "My Info" else "My Reviews"
        }.attach()


        // BottomNavigationView configuration
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_library -> {
                    // Navigate to the virtual library
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    true
                }
                else -> false
            }
        }


        // Button to add a new book
        findViewById<FloatingActionButton>(R.id.addBookButton).setOnClickListener {
            // Start the Activity to add a new book
            val intent = Intent(this, AddBookActivity::class.java)
            startActivity(intent)
        }
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val sharedPreferences = getSharedPreferences("LibruaryPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", -1)
        val authToken = sharedPreferences.getString("authToken", "")

        if (userId != -1 && authToken != null) {
            val userProfileUrl = ApiClient.getInstance(this).getFullUrl("profile/$userId")
            val jsonObjectRequest = object : JsonObjectRequest(
                Request.Method.GET, userProfileUrl, null,
                Response.Listener { response ->
                    updateUserInfo(response)
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, "Error fetching user profile: ${error.message}", Toast.LENGTH_SHORT).show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["x-access-token"] = authToken
                    return headers
                }
            }

            ApiClient.getInstance(this).addToRequestQueue(jsonObjectRequest)
        }
    }

    private fun updateUserInfo(response: JSONObject) {
        val username = response.getString("username")
        val reviewsArray = response.getJSONArray("reviews")

        userViewModel.updateUsername(username)
        userViewModel.updateReviews(reviewsArray)
    }

}
