// LoginActivity.kt
package com.example.libruary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.example.libruary.api.ApiClient
import org.json.JSONObject
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameField = findViewById<EditText>(R.id.username)
        val passwordField = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerLink = findViewById<Button>(R.id.registerLink)

        val sharedPreferences = getSharedPreferences("LibruaryPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", -1)
        val authToken = sharedPreferences.getString("authToken", "")

        if (userId != -1 && !authToken.isNullOrEmpty()) {
            checkTokenAndRedirect(authToken)
        }

        loginButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            } else {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            }
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }

    private fun checkTokenAndRedirect(authToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val isValid = isTokenValid(authToken)
            withContext(Dispatchers.Main) {
                if (!isValid) {
                    // Invalid or expired token, redirect to LoginActivity
                    Toast.makeText(this@LoginActivity, "Invalid or expired token. Please log in again.", Toast.LENGTH_LONG).show()
                } else {
                    // Valid token, redirect to MainActivity
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun loginUser(username: String, password: String) {
        val loginUrl = ApiClient.getInstance(this).getFullUrl("login")
        val params = JSONObject()
        params.put("username", username)
        params.put("password", password)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, loginUrl, params,
            { response ->
                try {
                    val token = response.getString("token")
                    val userId = response.getString("user_id").toInt()
                    val sharedPreferences = getSharedPreferences("LibruaryPrefs", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putString("authToken", token)
                        putInt("userId", userId)
                        apply()
                    }
                    Toast.makeText(this, "Successfully logged in!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error processing response: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Login failed: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        ApiClient.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private suspend fun isTokenValid(token: String): Boolean {
        val validateTokenUrl = ApiClient.getInstance(this).getFullUrl("validateToken")
        val params = JSONObject()
        params.put("token", token)

        val completion = CompletableDeferred<Boolean>()

        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, validateTokenUrl, params,
            { response ->
                try {
                    val isValid = response.getBoolean("is_valid")
                    completion.complete(isValid)
                } catch (e: Exception) {
                    e.printStackTrace()
                    completion.completeExceptionally(e)
                }
            },
            { error ->
                error.printStackTrace()
                if (error is AuthFailureError) {
                    // Treats error 401 as invalid token
                    completion.complete(false)
                } else {
                    completion.completeExceptionally(error)
                }
            }) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
                if (response.statusCode == 401) {
                    // Treats error 401 as invalid token
                    val responseData = JSONObject()
                    responseData.put("is_valid", false)
                    return Response.success(responseData, HttpHeaderParser.parseCacheHeaders(response))
                }
                return super.parseNetworkResponse(response)
            }
        }

        ApiClient.getInstance(this).addToRequestQueue(jsonObjectRequest)

        return completion.await()
    }

}
