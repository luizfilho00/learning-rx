package com.example.learningrx01.feature.presentation.adapter

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.learningrx01.R
import com.example.learningrx01.databinding.ItemMovieBinding
import com.example.learningrx01.feature.domain.model.Movie

class GitHubReposAdapter : ListAdapter<Movie, GitHubRepoViewHolder>(Diff) {
    companion object Diff : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GitHubRepoViewHolder {
        return GitHubRepoViewHolder(
            ItemMovieBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: GitHubRepoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class GitHubRepoViewHolder(
    private val view: ItemMovieBinding
) : RecyclerView.ViewHolder(view.root) {

    fun bind(item: Movie) {
        view.textViewTitle.text = item.title
        view.textViewOverview.text = item.overview
        Glide
            .with(view.imageView)
            .load(item.poster)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(R.drawable.poster_placeholder)
            .into(view.imageView)
    }
}