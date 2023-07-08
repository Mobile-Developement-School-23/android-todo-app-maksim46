package com.example.todoapp.di

import android.content.Context
import android.widget.PopupWindow
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.todoapp.presentation.utility.itemTouchHelper.ItemTouchHelperCallback
import com.example.todoapp.presentation.utility.itemTouchHelper.SwipeBackgroundHelper
import com.example.todoapp.presentation.view.MainFragmentViewModel
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthSdk
import dagger.Module
import dagger.Provides

/**
 * DI module related with utility needs
 */
@Module
class UtilsModule {

    @Provides
    fun provideItemTouchHelper(itemTouchHelperCallback: ItemTouchHelperCallback): ItemTouchHelper {
        return ItemTouchHelper(itemTouchHelperCallback)
    }

    @Provides
    fun provideItemTouchHelperCallback(
        sbh: SwipeBackgroundHelper, mainFragmentViewModel: MainFragmentViewModel): ItemTouchHelperCallback {
        return ItemTouchHelperCallback(sbh, mainFragmentViewModel.onItemSwiped)
    }

    @Provides
    fun providePopupWindow(context: Context): PopupWindow {
        return PopupWindow(context)
    }

    @Provides
    fun provideYandexAuthOptions(context: Context): YandexAuthOptions {
        return YandexAuthOptions(context, true)
    }

    @Provides
    fun provideYandexAuthSdk(context: Context, authOptions: YandexAuthOptions): YandexAuthSdk {
        return YandexAuthSdk(context, authOptions)
    }

}
