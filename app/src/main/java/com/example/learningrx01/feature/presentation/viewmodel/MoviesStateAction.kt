package com.example.learningrx01.feature.presentation.viewmodel

import com.example.learningrx01.feature.domain.model.Movie

sealed class MoviesStateAction {
    data class Data(val movies: List<Movie>) : MoviesStateAction()
    object Loading : MoviesStateAction()
    object Error : MoviesStateAction()
}