package com.example.libruary.models

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val message: String
)

data class RegisterRequest(
    val username: String,
    val password: String
)

data class RegisterResponse(
    val message: String
)

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val published_date: String,
    val publisher: String,
    val pages: Int,
    val image: String
)

data class BookResponse(
    val books: List<Book>
)

data class Review(
    val review: String,
    val rating: Int
)

data class ReviewResponse(
    val reviews: List<Review>
)

data class User(
    val id: Int,
    val username: String
)