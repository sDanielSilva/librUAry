package com.example.libruary.api

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class ApiClient(context: Context) {
    companion object {
        private const val BASE_URL = "https://api-libruary.vercel.app/"
        private var instance: ApiClient? = null
        private var requestQueue: RequestQueue? = null

        @Synchronized
        fun getInstance(context: Context): ApiClient {
            if (instance == null) {
                instance = ApiClient(context)
            }
            return instance!!
        }

        @Synchronized
        fun getRequestQueue(context: Context): RequestQueue {
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(context.applicationContext)
            }
            return requestQueue!!
        }
    }

    init {
        requestQueue = getRequestQueue(context)
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue?.add(req)
    }

    fun getFullUrl(path: String): String {
        return BASE_URL + path
    }
}
