package com.example.learningrx01.feature.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import com.example.learningrx01.feature.domain.model.Movie
import com.example.learningrx01.feature.domain.usecase.GetMovies
import com.example.learningrx01.feature.domain.usecase.SearchMovie
import com.example.learningrx01.feature.domain.util.SchedulerProvider
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.subjects.PublishSubject

class MoviesViewModel(
    private val getMovies: GetMovies,
    private val searchMovie: SearchMovie,
    private val schedulerProvider: SchedulerProvider
) : ViewModel() {

    val state by lazy { stateFlowable.toLiveData() }

    private val stateFlowable by lazy { composeState().toFlowable(BackpressureStrategy.LATEST) }
    private val intentTransformer by lazy(::transformer)
    private val intentSubject = PublishSubject.create<MoviesViewIntent>()

    fun onIntent(intent: MoviesViewIntent) {
        intentSubject.onNext(intent)
    }

    private val intentFilter = ObservableTransformer<MoviesViewIntent, MoviesViewIntent> { upstream ->
        upstream.publish { shared ->
            Observable.merge(
                shared.ofType(MoviesViewIntent.LoadMovies::class.java).take(1),
                shared.filter { it !is MoviesViewIntent.LoadMovies }
            )
        }
    }

    private fun composeState(): Observable<MoviesViewState> {
        return intentSubject
            .compose(intentFilter)
            .compose(intentTransformer)
            .scan(MoviesViewState(), reducer)
            .replay(1)
            .autoConnect(0)
            .distinctUntilChanged()
    }

    private val reducer = BiFunction<MoviesViewState, MoviesStateAction, MoviesViewState> { state, mutation ->
        when (mutation) {
            is MoviesStateAction.Data -> state.copy(
                movies = mutation.movies,
                isLoading = false,
                hasError = false
            )
            is MoviesStateAction.Loading -> state.copy(
                isLoading = true,
                hasError = false
            )
            is MoviesStateAction.Error -> state.copy(
                isLoading = false,
                hasError = true
            )
        }
    }

    private fun transformer() =
        ObservableTransformer<MoviesViewIntent, MoviesStateAction> { upstream ->
            upstream.publish { shared ->
                Observable.merge(
                    shared.ofType(MoviesViewIntent.LoadMovies::class.java).compose(loadMoviesProcessor),
                    shared.ofType(MoviesViewIntent.SearchMovies::class.java).compose(searchMovieProcessor)
                )
            }
        }

    private val loadMoviesProcessor = ObservableTransformer<MoviesViewIntent.LoadMovies, MoviesStateAction> { upstream ->
        upstream.flatMap { getMovies().applyCommonOperators() }
    }

    private val searchMovieProcessor = ObservableTransformer<MoviesViewIntent.SearchMovies, MoviesStateAction> { upstream ->
        upstream.switchMap { searchMovie(it.text).applyCommonOperators() }
    }

    private fun Observable<List<Movie>>.applyCommonOperators(): Observable<MoviesStateAction> {
        return subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .map { MoviesStateAction.Data(it) }
            .cast(MoviesStateAction::class.java)
            .startWithItem(MoviesStateAction.Loading)
            .onErrorReturnItem(MoviesStateAction.Error)
    }
}