package com.example.todoapp.presentation.utils.itemTouchHelper

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import com.example.todoapp.R
import javax.inject.Inject

class SwipeBackgroundHelper @Inject constructor(){

        fun startPaintDraw(canvas: Canvas, viewItem: View, dX: Float) {
            val infoForDraw = getInfoForDraw(viewItem, dX)
            if (infoForDraw != null) {
                drawBackground(canvas, viewItem, dX, infoForDraw.backgroundColor)
                drawIcon(canvas, viewItem, dX, infoForDraw.icon)
            }
        }

        private fun getInfoForDraw(viewItem: View, dX: Float): InfoForDraw? {
            val context = viewItem.context

            val icon = when( dX<0){
                true -> ContextCompat.getDrawable(context, R.drawable.ic_trash)
                false ->  ContextCompat.getDrawable(context, R.drawable.ic_check)
            }

            icon?.let {icon.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN) }

            val backgroundColor = when (dX <0) {
                true -> ContextCompat.getColor(viewItem.context, R.color.L_color_red)
                false -> ContextCompat.getColor(viewItem.context, R.color.L_color_green)
            }
            return icon?.let { InfoForDraw(it, backgroundColor) }
        }


        private fun drawBackground(canvas: Canvas, viewItem: View, dX: Float, color: Int) {
            val backgroundPaint = Paint()
            backgroundPaint.color = color
            val backgroundRectangle = when (dX <0) {
                true -> RectF(viewItem.right.toFloat() + dX, viewItem.top.toFloat(), viewItem.right.toFloat(), viewItem.bottom.toFloat())
                false -> RectF(viewItem.left.toFloat(), viewItem.top.toFloat(), viewItem.left.toFloat()+dX, viewItem.bottom.toFloat())
            }
            canvas.drawRect(backgroundRectangle, backgroundPaint)
        }

        private fun drawIcon(canvas: Canvas, viewItem: View, dx: Float, icon: Drawable) {
            val iconMargin = (viewItem.height - icon.intrinsicHeight) / 2
            val topBound = viewItem.top + iconMargin
            val bottomBound = viewItem.bottom - iconMargin

            val leftBoundForDelete = viewItem.right + dx.toInt() +iconMargin
            val rightBoundForDelete = viewItem.right + dx.toInt() + icon.intrinsicWidth+iconMargin

            val leftBoundForDone =   viewItem.left + dx.toInt() - icon.intrinsicWidth-iconMargin
            val rightBoundForDone =  viewItem.left + dx.toInt()-iconMargin

            icon.bounds =   when (dx <0) {
                true ->  Rect(leftBoundForDelete, topBound, rightBoundForDelete, bottomBound)
                false ->  Rect(leftBoundForDone, topBound, rightBoundForDone, bottomBound)
            }
            icon.draw(canvas)
 }

    private class InfoForDraw  (val icon: Drawable, val backgroundColor: Int)

}
