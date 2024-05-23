package com.example.libruary.api

import com.example.libruary.models.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {

    @POST("register")
    fun registerUser(@Body registerRequest: RegisterRequest): Call<RegisterResponse>

    @POST("login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("books")
    fun getBooks(@Query("query") query: String): Call<BookResponse>

    @GET("books/{id}")
    fun getBookDetails(@Path("id") id: Int): Call<Book>

    @POST("books/{id}/reviews")
    fun addReview(@Path("id") id: Int, @Body review: Review): Call<Void>

    @GET("books/{id}/reviews")
    fun getBookReviews(@Path("id") id: Int): Call<ReviewResponse>

    @GET("user")
    fun getUserProfile(): Call<User>

    @GET("user/reviews")
    fun getUserReviews(): Call<ReviewResponse>
}

object ApiClient {
    private const val BASE_URL = "https://api-libruary.vercel.app/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
