package com.example.learningrx01.feature.domain.boundaries

import com.example.learningrx01.feature.domain.model.Movie
import io.reactivex.rxjava3.core.Single

interface MoviesRepository {
    fun getPopularMovies(): Single<List<Movie>>
    fun getTopRatedMovies(): Single<List<Movie>>
}