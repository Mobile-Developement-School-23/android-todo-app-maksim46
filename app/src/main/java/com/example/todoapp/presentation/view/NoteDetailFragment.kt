package com.example.todoapp.presentation.view

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.todoapp.R
import com.example.todoapp.ToDoAppApp
import com.example.todoapp.databinding.FragmentNoteBinding
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.presentation.utility.DatePickerFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Second fragment of the application.
 * Contains logic of ContexMenu and DataPickerFragment management.
 */
class NoteDetailFragment : Fragment(R.layout.fragment_note) {
    private val binding by viewBinding(FragmentNoteBinding::bind)
    private val vm: MainFragmentViewModel by activityViewModels()

    @Inject
    lateinit var noteDetailViewController: NoteDetailViewController

    private val component by lazy {
        (requireActivity().application as ToDoAppApp).component.fragmentNoteDetailComponent().create(this, this.requireActivity())
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        component.inject(this)
        val view = inflater.inflate(R.layout.fragment_note, container, false)
        noteDetailViewController.setUpViews(view, viewLifecycleOwner)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val supportFragmentManager = requireActivity().supportFragmentManager
        setDatePickerFragmentResultListener(supportFragmentManager)

        binding.tvDeadlineValue.setOnClickListener {
            DatePickerFragment().show(supportFragmentManager, "DatePickerFragment")
        }

        registerForContextMenu(binding.tvPriorityValue)
        binding.tvPriorityValue.setOnClickListener {
            it.showContextMenu(0F, 0F)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.isShowDataPicker.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { isShow ->
                if (isShow) {
                    DatePickerFragment().show(supportFragmentManager, "DatePickerFragment")
                }
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        MenuInflater(requireContext()).inflate(R.menu.priority_menu, menu)
        val spannable = SpannableString(menu.get(2).title.toString())
        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.L_color_red)), 0, spannable.length, 0)
        menu.get(2).title = spannable
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option_low -> {
                binding.tvPriorityValue.apply {
                    text = getString(R.string.priority_low)
                    setTextAppearance(R.style.TextView_Body_Grey_Small)
                    vm.editNote(vm.getEditNote().copy(priority = Priority.Low))
                }
                true
            }

            R.id.option_medium -> {
                binding.tvPriorityValue.apply {
                    text = getString(R.string.priority_standart)
                    setTextAppearance(R.style.TextView_Body_Grey_Small)
                    vm.editNote(vm.getEditNote().copy(priority = Priority.Standart))
                }
                true
            }

            R.id.option_high -> {
                binding.tvPriorityValue.apply {
                    text = getString(R.string.priority_hight)
                    setTextAppearance(R.style.TextView_Body_Red)
                    vm.editNote(vm.getEditNote().copy(priority = Priority.High))
                }
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    private fun setDatePickerFragmentResultListener(supportFragmentManager: FragmentManager) {
        supportFragmentManager.setFragmentResultListener("FRAGMENT_RESULT_KEY", viewLifecycleOwner) { _, bundle ->
            vm.setDatePickerResult(bundle)
        }
    }
}




