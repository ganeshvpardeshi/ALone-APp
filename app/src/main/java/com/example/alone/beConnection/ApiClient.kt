package com.example.alone.beConnection

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://13.202.100.25:8000/"
//    private const val BASE_URL_1 = "http://10.0.2.2:8000/"

    //    private const val SQL_URL = "postgres://postgres:Bl00dyde?1211@127.0.0.1:5432/alone_app_db";
    val moderationApi: ModerationApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ModerationApi::class.java)
    }

    val userApi: UserApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApi::class.java)
    }
}
