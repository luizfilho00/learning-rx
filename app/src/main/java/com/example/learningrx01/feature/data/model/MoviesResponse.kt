package com.example.learningrx01.feature.data.model

import com.squareup.moshi.Json

data class MoviesResponse(
    val results: List<MovieResponse>
)

data class MovieResponse(
    val id: Int,
    val title: String,
    val overview: String,
    @Json(name = "poster_path") val posterPath: String,
)