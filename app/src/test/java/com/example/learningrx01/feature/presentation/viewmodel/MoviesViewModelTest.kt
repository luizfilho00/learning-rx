package com.example.learningrx01.feature.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.learningrx01.feature.domain.model.Movie
import com.example.learningrx01.feature.domain.usecase.GetMovies
import com.example.learningrx01.feature.domain.usecase.SearchMovie
import com.example.learningrx01.feature.domain.util.SchedulerProvider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class MoviesViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

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

        val states = vm.state.captureValues()
        vm.onIntent(MoviesViewIntent.LoadMovies)

        states
            .assertValueAt(0) { it == MoviesViewState() }
            .assertValueAt(1) { it.isLoading }
            .assertValueAt(2) { !it.isLoading && !it.hasError && it.movies == movies }
    }

    @Test
    fun testLoadError() {
        every { getMovies() } returns Observable.error(Throwable())

        val states = vm.state.captureValues()
        vm.onIntent(MoviesViewIntent.LoadMovies)

        states
            .assertValueAt(0) { !it.isLoading }
            .assertValueAt(1) { it.isLoading }
            .assertValueAt(2) { !it.isLoading && it.hasError && it.movies.isEmpty() }
    }

    @Test
    fun testStateOnSearchIntent() {
        val movies = listOf(Movie(1, "title", "overview", "poster"))

        every {
            searchMovie(any())
        } returns Observable.just(movies)

        val states = vm.state.captureValues()
        vm.onIntent(MoviesViewIntent.SearchMovies(""))

        states
            .assertValueAt(0) { !it.isLoading }
            .assertValueAt(1) { it.isLoading }
            .assertValueAt(2) { !it.isLoading && !it.hasError && it.movies == movies }
            .assertCount(3)
    }

    @Test
    fun testEnsureRequestHasProceededOnlyOncePerSearch() {
        var callsToSearch = 0
        val testScheduler = TestScheduler()
        val movies = listOf(Movie(1, "title", "overview", "poster"))
        val search = mutableListOf("b", "ba", "bat", "batm", "batma", "batman")

        every {
            searchMovie(any())
        } returns Observable
            .timer(1, TimeUnit.SECONDS, testScheduler)
            .map { movies }
            .doOnNext { callsToSearch++ }

        val states = vm.state.captureValues()
        Observable.timer(100, TimeUnit.MILLISECONDS, testScheduler)
            .map { search.removeFirst() }
            .map { MoviesViewIntent.SearchMovies(it) }
            .doOnNext { vm.onIntent(it) }
            .test()
        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)

        states
            .assertValueAt(0) {
                it == MoviesViewState()
            }
            .assertValueAt(1) {
                it.isLoading
            }
            .assertValueAt(2) {
                !it.isLoading && !it.hasError && it.movies == movies
            }
            .assert { callsToSearch == 1 }
    }

    private fun <T> List<T>.assertValueAt(index: Int, predicate: (T) -> Boolean): List<T> {
        assert(predicate(get(index)))
        return this
    }

    private fun <T> List<T>.assertCount(size: Int): List<T> {
        assert(this.size == size)
        return this
    }

    private fun <T> List<T>.assert(predicate: () -> Boolean): List<T> {
        assert(predicate())
        return this
    }

    private inline fun <reified T : Any> LiveData<T>.captureValues(): List<T> {
        val mockObserver = mockk<Observer<T>>()
        val list = mutableListOf<T>()
        every { mockObserver.onChanged(capture(list)) } just runs
        this.observeForever(mockObserver)
        return list
    }
}