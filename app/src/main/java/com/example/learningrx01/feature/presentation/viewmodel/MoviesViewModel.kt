package com.example.learningrx01.feature.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.learningrx01.feature.domain.model.Movie
import com.example.learningrx01.feature.domain.usecase.GetMovies
import com.example.learningrx01.feature.domain.usecase.SearchMovie
import com.example.learningrx01.feature.domain.util.SchedulerProvider
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.subjects.PublishSubject

class MoviesViewModel(
    private val getMovies: GetMovies,
    private val searchMovie: SearchMovie,
    private val schedulerProvider: SchedulerProvider
) : ViewModel() {
    val state by lazy { composeState() }

    private val intentTransformer by lazy(::transformer)
    private val intentSubject = PublishSubject.create<MoviesViewIntent>()

    fun onIntent(intentObservable: Observable<MoviesViewIntent>) {
        intentObservable.subscribe(intentSubject)
    }

    private fun composeState(): Observable<MoviesViewState> {
        return intentSubject
            .compose(intentTransformer)
            .scan(MoviesViewState()) { state, mutation ->
                when (mutation) {
                    is MoviesStateMutation.Data -> state.copy(
                        movies = mutation.movies,
                        isLoading = false,
                        hasError = false
                    )
                    is MoviesStateMutation.Loading -> state.copy(
                        isLoading = true,
                        hasError = false
                    )
                    is MoviesStateMutation.Error -> state.copy(
                        isLoading = false,
                        hasError = true
                    )
                }
            }
            .distinctUntilChanged()
            .replay(1)
            .autoConnect()
    }

    private fun transformer() =
        ObservableTransformer<MoviesViewIntent, MoviesStateMutation> { upstream ->
            upstream.switchMap { intent ->
                when (intent) {
                    is MoviesViewIntent.LoadMovies -> {
                        getMovies().applyCommonOperators()
                    }
                    is MoviesViewIntent.SearchMovies -> {
                        searchMovie(intent.text).applyCommonOperators()
                    }
                }
            }
        }

    private fun Observable<List<Movie>>.applyCommonOperators(): Observable<MoviesStateMutation> {
        return subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .map { MoviesStateMutation.Data(it) }
            .cast(MoviesStateMutation::class.java)
            .startWithItem(MoviesStateMutation.Loading)
            .onErrorReturnItem(MoviesStateMutation.Error)
    }
}