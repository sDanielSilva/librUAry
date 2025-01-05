package com.example.libruary

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.libruary.api.ApiClient
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val usernameField = findViewById<EditText>(R.id.username)
        val passwordField = findViewById<EditText>(R.id.password)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginLink = findViewById<Button>(R.id.loginLink)

        registerButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                registerUser(username, password)
            } else {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            }
        }

        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(username: String, password: String) {
        val registerUrl = ApiClient.getInstance(this).getFullUrl("register")
        val params = JSONObject()
        params.put("username", username)
        params.put("password", password)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, registerUrl, params,
            Response.Listener { response ->
                Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.registration_successful),
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            },
            Response.ErrorListener { error ->
                Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.registration_failed, error.message),
                    Toast.LENGTH_SHORT
                ).show()
            })

        ApiClient.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }
}
