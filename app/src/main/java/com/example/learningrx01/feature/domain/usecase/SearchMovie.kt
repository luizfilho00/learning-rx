package com.example.learningrx01.feature.domain.usecase

import com.example.learningrx01.feature.domain.boundaries.MoviesRepository
import com.example.learningrx01.feature.domain.model.Movie
import com.example.learningrx01.feature.domain.util.SchedulerProvider
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

interface SearchMovie {
    operator fun invoke(text: String): Observable<List<Movie>>
}

class SearchMovieImpl(
    private val repository: MoviesRepository,
    private val schedulerProvider: SchedulerProvider
) : SearchMovie {

    override operator fun invoke(text: String): Observable<List<Movie>> =
        Observable
            .merge(
                repository.getPopularMovies().subscribeOn(schedulerProvider.io()),
                repository.getTopRatedMovies().subscribeOn(schedulerProvider.io())
            )
            .delay(300, TimeUnit.MILLISECONDS)
            .reduce { popularMovies, topRatedMovies ->
                popularMovies + topRatedMovies
            }
            .map { list ->
                list.filter {
                    it.title.contains(text, true) ||
                        it.overview.contains(text, true)
                }
            }
            .doOnSuccess { println("Search: $text") }
            .toObservable()
}