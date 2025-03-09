package com.example.bookxpertassignment

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {



    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AccountUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
