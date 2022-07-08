package com.example.learningrx01.feature.domain.usecase

import com.example.learningrx01.feature.domain.boundaries.MoviesRepository
import com.example.learningrx01.feature.domain.model.Movie
import com.example.learningrx01.feature.domain.util.SchedulerProvider
import io.reactivex.rxjava3.core.Observable

interface GetMovies {
    operator fun invoke(): Observable<List<Movie>>
}

class GetMoviesImpl(
    private val repository: MoviesRepository,
    private val schedulerProvider: SchedulerProvider
) : GetMovies {

    override operator fun invoke(): Observable<List<Movie>> =
        Observable
            .merge(
                repository.getPopularMovies().subscribeOn(schedulerProvider.io()),
                repository.getTopRatedMovies().subscribeOn(schedulerProvider.io())
            )
            .reduce { popularMovies, topRatedMovies ->
                popularMovies + topRatedMovies
            }
            .toObservable()
}