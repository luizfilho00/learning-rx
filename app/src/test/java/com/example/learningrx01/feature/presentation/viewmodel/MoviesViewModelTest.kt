package com.example.learningrx01.feature.presentation.viewmodel

import com.example.learningrx01.feature.domain.model.Movie
import com.example.learningrx01.feature.domain.usecase.GetMovies
import com.example.learningrx01.feature.domain.usecase.SearchMovie
import com.example.learningrx01.feature.domain.util.SchedulerProvider
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Before
import org.junit.Test

class MoviesViewModelTest {

    lateinit var vm: MoviesViewModel

    lateinit var schedulerProvider: SchedulerProvider

    @MockK
    lateinit var getMovies: GetMovies

    @MockK
    lateinit var searchMovie: SearchMovie

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        schedulerProvider = object : SchedulerProvider {
            override fun io(): Scheduler {
                return Schedulers.trampoline()
            }

            override fun ui(): Scheduler {
                return Schedulers.trampoline()
            }
        }
        vm = MoviesViewModel(getMovies, searchMovie, schedulerProvider)
    }

    @After
    fun after() {
        clearAllMocks()
    }

    @Test
    fun testStateOnLoadIntent() {
        val movies = listOf(Movie(1, "title", "overview", "poster"))
        every { getMovies() } returns Observable.just(movies)

        val state = vm.state.test()

        vm.onIntent(Observable.just(MoviesViewIntent.LoadMovies))

        state
            .assertValueAt(0) {
                it == MoviesViewState()
            }
            .assertValueAt(1) {
                it.isLoading
            }
            .assertValueAt(2) {
                !it.isLoading && !it.hasError && it.movies == movies
            }
    }

    @Test
    fun testLoadError() {
        every { getMovies() } returns Observable.error(Throwable())

        val state = vm.state.test()

        vm.onIntent(Observable.just(MoviesViewIntent.LoadMovies))

        state
            .assertValueAt(0) {
                it == MoviesViewState()
            }
            .assertValueAt(1) {
                it.isLoading
            }
            .assertValueAt(2) {
                it.hasError && !it.isLoading
            }

    }

    @Test
    fun testStateOnSearchIntent() {
        val movies = listOf(Movie(1, "title", "overview", "poster"))

        every {
            searchMovie(any())
        } returns Observable.just(movies)

        val state = vm.state.test()

        vm.onIntent(Observable.just(MoviesViewIntent.SearchMovies("")))

        state
            .assertValueAt(0) {
                it == MoviesViewState()
            }
            .assertValueAt(1) {
                it.isLoading
            }
            .assertValueAt(2) {
                !it.isLoading && !it.hasError && it.movies == movies
            }
    }

    @Test
    fun testEnsureRequestHasProceededOnlyOncePerSearch() {
        var callsToSearch = 0
        val testScheduler = TestScheduler()
        val movies = listOf(Movie(1, "title", "overview", "poster"))
        val queryObservable =
            Observable.fromIterable(listOf("b", "ba", "bat", "batm", "batma", "batman"))
                .map { MoviesViewIntent.SearchMovies(it) }
                .cast(MoviesViewIntent::class.java)

        every {
            searchMovie(any())
        } returns Observable
            .timer(10, TimeUnit.SECONDS, testScheduler)
            .map { movies }
            .doOnNext { callsToSearch++ }

        val state = vm.state.test()

        vm.onIntent(queryObservable)

        testScheduler.advanceTimeBy(1, TimeUnit.MINUTES)

        state
            .assertValueAt(0) {
                it == MoviesViewState()
            }
            .assertValueAt(1) {
                it.isLoading
            }
            .assertValueAt(2) {
                !it.isLoading && !it.hasError && it.movies == movies
            }

        assert(callsToSearch == 1)
    }
}