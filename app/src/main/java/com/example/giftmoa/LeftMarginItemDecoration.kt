package com.example.giftmoa

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LeftMarginItemDecoration(private val margin: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)

        if (position == 0) {
            outRect.left = margin
        }
    }
}