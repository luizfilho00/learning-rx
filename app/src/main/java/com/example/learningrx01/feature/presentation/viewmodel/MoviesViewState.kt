package com.example.learningrx01.feature.presentation.viewmodel

import com.example.learningrx01.feature.domain.model.Movie

data class MoviesViewState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false
)