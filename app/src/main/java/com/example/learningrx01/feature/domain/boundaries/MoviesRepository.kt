package com.example.learningrx01.feature.domain.boundaries

import com.example.learningrx01.feature.domain.model.Movie
import io.reactivex.rxjava3.core.Observable

interface MoviesRepository {
    fun getPopularMovies(): Observable<List<Movie>>
    fun getTopRatedMovies(): Observable<List<Movie>>
}