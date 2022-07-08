package com.example.learningrx01.feature.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learningrx01.feature.domain.model.Movie
import com.example.learningrx01.feature.domain.usecase.GetMovies
import com.example.learningrx01.feature.domain.usecase.SearchMovie
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MoviesViewModel(
    getMovies: GetMovies,
    private val searchMovie: SearchMovie
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private var viewState = MoviesViewState()

    val state by lazy { MutableLiveData<MoviesViewState>() }

    private lateinit var cachedMovies: List<Movie>

    init {
        getMovies()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                viewState.copy(isLoading = true).emit()
            }
            .subscribe(
                {
                    cachedMovies = it
                    viewState.copy(movies = it, isLoading = false).emit()
                },
                {
                    Log.d("GetGitHubRepo", "$it")
                    viewState.copy(hasError = true, isLoading = false).emit()
                }
            )
            .let(disposables::add)
    }

    fun onSearchChanged(text: String) {
        searchMovie(cachedMovies, text)
            .subscribe(
                { viewState.copy(movies = it).emit() },
                { Log.d("MoviesViewModel", "onSearchChanged: $it") }
            )
            .let(disposables::add)
    }

    private fun MoviesViewState.emit() {
        viewState = this
        state.value = viewState
    }
}