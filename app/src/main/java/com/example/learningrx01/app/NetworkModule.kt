package com.example.learningrx01.app

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

val networkModule = module {

    single {
        val bearer = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI4OWQwMmI2YmJiM2NmNTI5YTQ2YWRiZWU0NTIyZTVkMiIsInN1YiI6IjVjZGQ2OGMzOTI1MTQxNmUzMmQwOTlkOSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.dm7o25af_fztwSm8hvLLH6wjrNNqY8C8CAIlHAmirDc"
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            })
            .addInterceptor(Interceptor { chain ->
                val request = chain.request()
                val newBuilder = request.newBuilder()
                newBuilder.header(
                    "Authorization",
                    "Bearer $bearer"
                )
                newBuilder.header(
                    "Content-Type",
                    "application/json;charset=utf-8"
                )
                chain.proceed(newBuilder.build())
            })
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
        Retrofit
            .Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("https://api.themoviedb.org/3/")
            .build()
    }
}