package com.example.learningrx01.feature.presentation.di

import com.example.learningrx01.app.SchedulerProvider
import com.example.learningrx01.app.SchedulerProviderImpl
import com.example.learningrx01.feature.data.remote.ApiService
import com.example.learningrx01.feature.data.repositories.MoviesRepositoryImpl
import com.example.learningrx01.feature.domain.boundaries.MoviesRepository
import com.example.learningrx01.feature.domain.usecase.GetMovies
import com.example.learningrx01.feature.domain.usecase.SearchMovie
import com.example.learningrx01.feature.presentation.MoviesActivity
import com.example.learningrx01.feature.presentation.viewmodel.MoviesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

val featureModule = module {
    single<SchedulerProvider> { SchedulerProviderImpl() }

    single(named("ImageBaseUrl")) {
        "https://image.tmdb.org/t/p/w500"
    }

    single {
        val retrofit = get<Retrofit>()
        retrofit.create(ApiService::class.java)
    }

    scope<MoviesActivity> {
        viewModel { MoviesViewModel(get(), get()) }
        scoped<MoviesRepository> { MoviesRepositoryImpl(get(), get(named("ImageBaseUrl"))) }
        scoped { GetMovies(get()) }
        scoped { SearchMovie() }
    }
}