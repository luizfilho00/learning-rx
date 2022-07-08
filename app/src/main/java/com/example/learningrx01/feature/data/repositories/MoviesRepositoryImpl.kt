package com.example.learningrx01.feature.data.repositories

import com.example.learningrx01.feature.data.model.MovieResponse
import com.example.learningrx01.feature.data.remote.ApiService
import com.example.learningrx01.feature.domain.boundaries.MoviesRepository
import com.example.learningrx01.feature.domain.model.Movie
import io.reactivex.rxjava3.core.Single

class MoviesRepositoryImpl(
    private val apiService: ApiService,
    private val imageBaseUrl: String
) : MoviesRepository {

    override fun getPopularMovies(): Single<List<Movie>> {
        return apiService
            .getPopularMovies()
            .flatMap { Single.just(it.results) }
            .concatMap { list ->
                Single.fromCallable {
                    list.toDomain()
                }
            }
    }

    override fun getTopRatedMovies(): Single<List<Movie>> {
        return apiService
            .getTopRatedMovies()
            .flatMap { Single.just(it.results) }
            .flatMap { list ->
                Single.just(list.toDomain())
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