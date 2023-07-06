package com.example.todoapp.di

import androidx.fragment.app.Fragment
import com.example.todoapp.presentation.view.MainFragmentViewModel
import com.example.todoapp.presentation.utils.DUtils
import com.example.todoapp.presentation.view.RVListAdapter

class NoteFragmentComponent (
    val applicationComponent: ApplicationComponent,
    val fragment: Fragment,
    val viewModel: MainFragmentViewModel,
   // val binding: FragmentMainBinding?
) {
   //val a = fragment.getString()
    val adapter = RVListAdapter(viewModel, DUtils())
}