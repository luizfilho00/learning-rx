package com.example.learningrx01.feature.presentation.viewmodel

import com.example.learningrx01.feature.domain.model.Movie

sealed class MoviesStateMutation {
    data class Data(val movies: List<Movie>) : MoviesStateMutation()
    object Loading : MoviesStateMutation()
    object Error : MoviesStateMutation()
}