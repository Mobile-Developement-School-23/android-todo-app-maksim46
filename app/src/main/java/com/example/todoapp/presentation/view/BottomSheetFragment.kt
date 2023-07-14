package com.example.todoapp.presentation.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.todoapp.R
import com.example.todoapp.ToDoAppApp
import com.example.todoapp.databinding.BottomSheetBinding
import com.example.todoapp.domain.ChoosedTheme
import com.example.todoapp.domain.CurrentThemeStorage
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class BottomSheetFragment : BottomSheetDialogFragment(R.layout.bottom_sheet) {
    private val binding by viewBinding(BottomSheetBinding::bind)

    private val component by lazy {
        (requireActivity().application as ToDoAppApp).component.fragmentBottomSheetComponent().create()
    }

    @Inject
    lateinit var currentThemeStorage: CurrentThemeStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setCheckBox(currentThemeStorage.getTheme() ?: ChoosedTheme.SYSTEM.theme)


        binding.checkBox1.setOnClickListener {
            currentThemeStorage.setTheme(ChoosedTheme.SYSTEM)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            setCheckBox(ChoosedTheme.SYSTEM.theme)
        }
        binding.checkBox2.setOnClickListener {
            currentThemeStorage.setTheme(ChoosedTheme.DARK)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            setCheckBox(ChoosedTheme.DARK.theme)
        }
        binding.checkBox3.setOnClickListener {
            currentThemeStorage.setTheme(ChoosedTheme.LIGHT)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            setCheckBox(ChoosedTheme.LIGHT.theme)
        }


    }


    private fun setCheckBox(mode: String) {

        with(binding) {
            ivCheckBox1.setImageResource(R.drawable.ic_unchecked)
            ivCheckBox2.setImageResource(R.drawable.ic_unchecked)
            ivCheckBox3.setImageResource(R.drawable.ic_unchecked)

            when (mode) {
                ChoosedTheme.SYSTEM.theme -> ivCheckBox1.setImageResource(R.drawable.ic_check_done)
                ChoosedTheme.DARK.theme -> ivCheckBox2.setImageResource(R.drawable.ic_check_done)
                ChoosedTheme.LIGHT.theme -> ivCheckBox3.setImageResource(R.drawable.ic_check_done)
            }
        }
    }
}
