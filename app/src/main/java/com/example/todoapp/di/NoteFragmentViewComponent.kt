package com.example.todoapp.di

import android.view.View
import android.widget.PopupWindow
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.todoapp.di.NoteFragmentComponent
import com.example.todoapp.presentation.view.MainViewController
import com.example.todoapp.presentation.view.NoteDetailViewController

class NoteFragmentViewComponent (

    popUpWindows: PopupWindow,
    itemTouchHelper: ItemTouchHelper,
    fragmentComponent: NoteFragmentComponent,
    root: View,
    lifecycleOwner: LifecycleOwner,
   // binding:    View
    ){

    val mainFragmentViewController = MainViewController(

        popUpWindows,
        itemTouchHelper,
        fragmentComponent,
        fragmentComponent.fragment.requireActivity(),
        root,
        fragmentComponent.adapter,
        lifecycleOwner,
        fragmentComponent.viewModel,
      // binding
    )
  //  )
  val noteDetailFragmentViewController = NoteDetailViewController(
      fragmentComponent.fragment.requireActivity(),
      root,
      fragmentComponent.adapter,
      lifecycleOwner,
      fragmentComponent.viewModel,
      // binding
  )
}


