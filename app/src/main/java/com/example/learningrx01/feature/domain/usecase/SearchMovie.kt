package com.example.learningrx01.feature.domain.usecase

import com.example.learningrx01.feature.domain.model.Movie
import io.reactivex.rxjava3.core.Single

class SearchMovie {

    operator fun invoke(movies: List<Movie>, text: String): Single<List<Movie>> {
        return if (text.isEmpty()) {
            Single.just(movies)
        } else {
            Single.just(
                movies.filter {
                    it.title.contains(text, true) ||
                        it.overview.contains(text, true)
                }
            )
        }
    }
}