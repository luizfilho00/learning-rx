package com.example.learningrx01.feature.presentation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.example.learningrx01.R
import com.example.learningrx01.databinding.ActivityMoviesBinding
import com.example.learningrx01.feature.presentation.adapter.GitHubReposAdapter
import com.example.learningrx01.feature.presentation.adapter.MovieItemDecoration
import com.example.learningrx01.feature.presentation.viewmodel.MoviesViewIntent
import com.example.learningrx01.feature.presentation.viewmodel.MoviesViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.koin.androidx.scope.ScopeActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class MoviesActivity : ScopeActivity() {
    companion object {
        const val DATA = 0
        const val LOADING = 1
    }

    private lateinit var binding: ActivityMoviesBinding
    private val viewModel by viewModel<MoviesViewModel>()
    private val adapter by lazy { GitHubReposAdapter() }
    private val decoration by lazy { MovieItemDecoration(resources) }

    private val querySubject = PublishSubject.create<String>()
    private val loadIntent = Observable.just(MoviesViewIntent.LoadMovies)

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoviesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        observeUI()
        viewModel.onIntent(intentObservable())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.movies_toolbar_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(text: String?): Boolean {
                querySubject.onNext(text.orEmpty())
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        binding.recyclerView.adapter = null
        querySubject.onComplete()
        disposables.clear()
        super.onDestroy()
    }

    private fun setupUI() {
        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(decoration)
    }

    private fun observeUI() {
        viewModel.state.subscribe { viewState ->
            binding.viewFlipper.displayedChild =
                if (viewState.isLoading) LOADING
                else DATA
            adapter.submitList(viewState.movies)
        }.let(disposables::add)
    }

    private fun intentObservable(): Observable<MoviesViewIntent> {
        return Observable.merge(
            loadIntent,
            querySubject.skip(1).map { MoviesViewIntent.SearchMovies(it) }
        )
    }
}