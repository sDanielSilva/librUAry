package com.example.libruary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONArray

class UserViewModel : ViewModel() {
    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    private val _reviews = MutableLiveData<JSONArray>()
    val reviews: LiveData<JSONArray> = _reviews

    init {
        reviews.observeForever { reviews ->
        }
    }

    fun updateUsername(newUsername: String) {
        _username.value = newUsername
    }

    fun updateReviews(newReviews: JSONArray) {
        _reviews.value = newReviews
    }

}

