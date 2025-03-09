package com.example.bookxpertassignment

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("Fillaccounts/nadc/2024-2025")
    fun getAccounts(): Call<String>
}
