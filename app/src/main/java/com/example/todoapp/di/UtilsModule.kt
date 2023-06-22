package com.example.todoapp.di

import android.content.Context
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.todoapp.presentation.utils.itemTouchHelper.ItemTouchHelperCallback
import dagger.Module
import dagger.Provides


@Module
class UtilsModule {


    @Provides
    fun provideItemTouchHelper(itemTouchHelperCallback: ItemTouchHelperCallback): ItemTouchHelper {
        return ItemTouchHelper(itemTouchHelperCallback)
    }
    @Provides
    fun providePopupWindow(context: Context): PopupWindow {
        return PopupWindow(context)
    }

}
