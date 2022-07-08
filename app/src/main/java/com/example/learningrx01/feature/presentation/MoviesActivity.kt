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
import com.example.learningrx01.feature.presentation.viewmodel.MoviesViewModel
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoviesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        observeUI()
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
                viewModel.onSearchChanged(text.orEmpty())
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.recyclerView.adapter = null
    }

    private fun setupUI() {
        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(decoration)
    }

    private fun observeUI() {
        viewModel.state.observe(this) { viewState ->
            binding.viewFlipper.displayedChild =
                if (viewState.isLoading) LOADING
                else DATA
            adapter.submitList(viewState.movies)
            Handler(Looper.getMainLooper())
                .postDelayed(
                    { binding.recyclerView.scrollToPosition(0) },
                    100
                )
        }
    }
}