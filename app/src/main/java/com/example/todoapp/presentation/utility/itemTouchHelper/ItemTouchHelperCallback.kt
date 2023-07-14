package com.example.todoapp.presentation.utility.itemTouchHelper


import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import javax.inject.Inject

/**
 * Callback for notes swipes implementation on [MainFragment] recycler
 */


class ItemTouchHelperCallback @Inject constructor(
    private val swipeBackgroundHelper: SwipeBackgroundHelper,
    private val onItemSwiped: (position: Int, direction: Int) -> Unit
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        if (viewHolder.itemViewType == R.layout.footer_item) return 0
        else {
            val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            return makeMovementFlags(0, swipeFlags)
        }
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onItemSwiped(viewHolder.bindingAdapterPosition, direction)
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val viewItem = viewHolder.itemView
            swipeBackgroundHelper.startPaintDraw(canvas, viewItem, dX)
        }
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }


}


