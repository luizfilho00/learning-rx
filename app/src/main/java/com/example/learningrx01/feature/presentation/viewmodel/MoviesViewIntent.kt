package com.example.learningrx01.feature.presentation.viewmodel

sealed class MoviesViewIntent {
    object LoadMovies : MoviesViewIntent()
    data class SearchMovies(val text: String): MoviesViewIntent()
}