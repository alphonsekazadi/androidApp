package com.hedvig.app.feature.embark.passages

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.BASE_MARGIN
import com.hedvig.app.BASE_MARGIN_DOUBLE

class SelectActionDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val spanIndex = (view.layoutParams as? GridLayoutManager.LayoutParams)?.spanIndex ?: return
        when (spanIndex) {
            SPAN_LEFT -> {
                outRect.left = BASE_MARGIN_DOUBLE
                outRect.right = BASE_MARGIN
            }
            SPAN_RIGHT -> {
                outRect.left = BASE_MARGIN
                outRect.right = BASE_MARGIN_DOUBLE
            }
        }
        return
    }

    companion object {
        private const val SPAN_LEFT = 0
        private const val SPAN_RIGHT = 1
    }
}
