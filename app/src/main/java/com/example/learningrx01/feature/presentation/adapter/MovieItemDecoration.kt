package com.example.learningrx01.feature.presentation.adapter

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.learningrx01.R

class MovieItemDecoration(private val resources: Resources) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val totalCount = parent.adapter?.itemCount ?: 0
        val itemPosition = parent.getChildAdapterPosition(view)
        outRect.run {
            top = if (itemPosition == 0) 0
            else resources.getDimensionPixelOffset(R.dimen.inline_xs)
            if (itemPosition == (totalCount - 1))
                bottom = resources.getDimensionPixelOffset(R.dimen.inline_xs)
        }
    }
}