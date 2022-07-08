package com.example.learningrx01.feature.domain.usecase

import com.example.learningrx01.feature.domain.boundaries.MoviesRepository
import com.example.learningrx01.feature.domain.model.Movie
import io.reactivex.rxjava3.core.Single

class GetMovies(
    private val repository: MoviesRepository
) {

    operator fun invoke(): Single<List<Movie>> =
        repository
            .getPopularMovies()
            .flatMap { popularMovies ->
                repository
                    .getTopRatedMovies()
                    .map { topRatedMovies ->
                        popularMovies + topRatedMovies
                    }
            }
}