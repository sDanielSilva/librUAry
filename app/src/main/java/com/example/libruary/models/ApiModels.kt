package com.example.libruary.models

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String
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
    val publishedDate: String?,
    val publisher: String,
    val pages: Int,
    val image: String?,
    var rating: Int
) {
    override fun toString(): String {
        return "Book(id=$id, title='$title', author='$author', publishedDate=$publishedDate, publisher='$publisher', pages=$pages, image=$image, rating=$rating)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Book

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}


data class BookRequest(
    val isbn: String,
    val user_id: Int
)

data class UserBookItem(
    val book_id: Int
)

data class UserBookResponse(
    val user_books: List<UserBookItem>
)

data class Review(
    val id: Int? = null, // Tornando o 'id' opcional com um valor padrão de 'null'
    val book_id: Int,
    val user_id: Int,
    val review_text: String,
    val rating: Int
)

data class ReviewResponse(
    val reviews: List<Review>
)

data class User(
    val id: Int,
    val username: String
)

data class UserReview(
    val book_id: Int,
    val book_title: String,
    val review_text: String,
    val rating: Int
)

data class ApiReview(
    val id: Int,
    val username: String,
    val reviewText: String,
    val rating: Float,
    val userId: Int
)

