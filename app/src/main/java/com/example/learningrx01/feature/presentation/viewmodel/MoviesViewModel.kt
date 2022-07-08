package com.example.learningrx01.feature.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learningrx01.feature.domain.usecase.GetMovies
import com.example.learningrx01.feature.domain.usecase.SearchMovie
import com.example.learningrx01.feature.domain.util.SchedulerProvider
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject

class MoviesViewModel(
    private val getMovies: GetMovies,
    private val searchMovie: SearchMovie,
    private val schedulerProvider: SchedulerProvider
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private var viewState = MoviesViewState()

    val state by lazy { MutableLiveData<MoviesViewState>() }

    private val querySubject = PublishSubject.create<String>()

    init {
        loadMovies()
        observeQueryChanges()
    }

    fun onSearchChanged(text: String) {
        querySubject.onNext(text)
    }

    private fun loadMovies() {
        getMovies()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                viewState.copy(isLoading = true).emit()
            }
            .subscribe(
                {
                    viewState.copy(movies = it, isLoading = false).emit()
                },
                {
                    Log.d("GetGitHubRepo", "$it")
                    viewState.copy(hasError = true, isLoading = false).emit()
                }
            )
            .let(disposables::add)
    }

    private fun observeQueryChanges() {
        querySubject
            .switchMap(searchMovie::invoke)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
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