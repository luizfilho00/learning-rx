package com.example.learningrx01.feature.data.repositories

import com.example.learningrx01.feature.data.model.MovieResponse
import com.example.learningrx01.feature.data.remote.ApiService
import com.example.learningrx01.feature.domain.boundaries.MoviesRepository
import com.example.learningrx01.feature.domain.model.Movie
import io.reactivex.rxjava3.core.Observable

class MoviesRepositoryImpl(
    private val apiService: ApiService,
    private val imageBaseUrl: String
) : MoviesRepository {

    private val popularMovies = apiService.getPopularMovies()
        .toObservable()
        .replay(1)
        .autoConnect()

    private val topRatedMovies = apiService.getTopRatedMovies()
        .toObservable()
        .replay(1)
        .autoConnect()

    override fun getPopularMovies(): Observable<List<Movie>> {
        return popularMovies.map { response ->
            response.results.toDomain()
        }
    }

    override fun getTopRatedMovies(): Observable<List<Movie>> {
        return topRatedMovies.map { response ->
            response.results.toDomain()
        }
    }

    private fun List<MovieResponse>.toDomain() = map {
        Movie(
            id = it.id,
            title = it.title,
            overview = it.overview,
            poster = "$imageBaseUrl${it.posterPath}"
        )
    }
}