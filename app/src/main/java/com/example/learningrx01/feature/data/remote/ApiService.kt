package com.example.learningrx01.feature.data.remote

import com.example.learningrx01.feature.data.model.MoviesResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET

interface ApiService {

    @GET("movie/popular?page=1")
    fun getPopularMovies(): Single<MoviesResponse>

    @GET("movie/top_rated?page=1")
    fun getTopRatedMovies(): Single<MoviesResponse>
}