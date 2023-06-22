package com.example.todoapp.presentation

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentNoteBinding
import com.example.todoapp.domain.model.NoteData
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.ToDoEntity
import com.example.todoapp.presentation.utils.DatePickerFragment
import com.example.todoapp.presentation.utils.toEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class NoteDetailFragment : Fragment(R.layout.fragment_note) {
    private val binding by viewBinding(FragmentNoteBinding::bind)
    private val vm: MainFragmentViewModel by activityViewModels()
    lateinit var popupWindow: PopupWindow
    private var editingToDoNote: ToDoEntity =  NoteData.ToDoItem().toEntity()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val supportFragmentManager = requireActivity().supportFragmentManager
        setFragmentResultListener(supportFragmentManager)

        viewLifecycleOwner.lifecycleScope.launch {
            vm.toDoNoteByIdForEdit.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect { noteData ->
                 editingToDoNote = noteData
                with(binding) {
                    etNoteText.setText(noteData.text)

                    when (noteData.priority) {
                        Priority.Standart -> {
                            tvPriorityValue.text = getString(R.string.priority_standart)
                            tvPriorityValue.setTextAppearance(R.style.TextView_Body_Grey_Small)
                        }
                        Priority.Low -> {
                            tvPriorityValue.text = getString(R.string.priority_low)
                            tvPriorityValue.setTextAppearance(R.style.TextView_Body_Grey_Small)
                        }
                        Priority.Hight -> {
                            tvPriorityValue.text = getString(R.string.priority_hight)
                            tvPriorityValue.setTextAppearance(R.style.TextView_Body_Red)
                        }
                    }

                    if (noteData.deadline != 0L) {
                        binding.tvDeadlineValue.text =
                            SimpleDateFormat("dd MMMM yyyy", Locale("ru")).format(noteData.deadline)
                    } else {
                        deadlineSwitch.toggle()
                        tvDeadlineValue.visibility = View.GONE
                    }

                    if (noteData.text.isEmpty()) {
                        deleteGroup.isEnabled = false

                    } else {
                        deleteGroup.isEnabled = true
                        delete.setTextAppearance(R.style.TextView_Body_Red)
                        ivTrash.setColorFilter(ContextCompat.getColor(requireContext(), R.color.L_color_red))

                    }
                }
            }
        }

        with(binding) {

            tvDeadlineValue.setOnClickListener {
                DatePickerFragment().show(supportFragmentManager, "DatePickerFragment")
            }

            deadlineSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    DatePickerFragment().show(supportFragmentManager, "DatePickerFragment")
                } else {
                    editingToDoNote = editingToDoNote.copy(deadline = 0L)
                    tvDeadlineValue.visibility = View.GONE
                }
            }

/*            tvPriorityValue.setOnClickListener {
                choosePriorityPopup(layoutInflater.inflate(R.layout.popup_priority, null), tvPriorityValue)
            }*/
            registerForContextMenu(tvPriorityValue)
            tvPriorityValue.setOnClickListener {
                Log.d("aaa", "aaa")
                it.showContextMenu(0F,0F)
            }

            deleteGroup.setOnClickListener {
                vm.delete(editingToDoNote.id)
                findNavController().popBackStack()
            }

            ibClose.setOnClickListener {
                findNavController().popBackStack()
            }

            tvSave.setOnClickListener {
                editingToDoNote = editingToDoNote.copy(text = etNoteText.text.toString())
                editingToDoNote =
                    if (editingToDoNote.createDate == Date(0)) {
                        editingToDoNote.copy(createDate = Date(), updateDate = Date())
                    } else {
                        editingToDoNote.copy(updateDate = Date())
                    }
                if (checkBeforeSave(editingToDoNote)) {
                    vm.updateToDoNote(editingToDoNote)
                    findNavController().popBackStack()
                }
            }
        }
    }


    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {

        super.onCreateContextMenu(menu, v, menuInfo)
      //  MenuInflater(requireContext()).inflate(R.menu.priority_menu, menu)
        Log.d("aaa", "assaa")
        val inflater = MenuInflater(requireContext())
        inflater.inflate(R.menu.priority_menu, menu)
    }
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option_low -> {
                binding.tvPriorityValue.apply {
                    text = getString(R.string.priority_standart)
                    setTextAppearance(R.style.TextView_Body_Grey_Small)
                    editingToDoNote = editingToDoNote.copy(priority = Priority.Standart)
                }
                true
            }
            R.id.option_medium -> {
                binding.tvPriorityValue.apply {
                    text = getString(R.string.priority_low)
                    setTextAppearance(R.style.TextView_Body_Grey_Small)
                    editingToDoNote = editingToDoNote.copy(priority = Priority.Low)}
                    true

            }

            R.id.option_high -> {
                binding.tvPriorityValue.apply {
                    text = getString(R.string.priority_hight)
                    setTextAppearance(R.style.TextView_Body_Red)
                    editingToDoNote = editingToDoNote.copy(priority = Priority.Hight)
                }
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    private fun checkBeforeSave(editingToDoNote: ToDoEntity): Boolean {
        return if (editingToDoNote.text.isEmpty()) {
            Toast.makeText(requireContext(), R.string.emptyText_Toast, Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }

    }

    private fun choosePriorityPopup(view: View, tvPriorityValue: View) {
        popupWindow = PopupWindow(context)
        popupWindow.animationStyle = R.style.PopupAnimationPriority
        popupWindow.contentView = view
        popupWindow.height =700
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(null)
        popupWindow.showAsDropDown(tvPriorityValue)

        view.findViewById<TextView>(R.id.menu_priority_standart1).setOnClickListener {
            (tvPriorityValue as TextView).apply {
                text = getString(R.string.priority_standart)
                setTextAppearance(R.style.TextView_Body_Grey_Small)
                editingToDoNote = editingToDoNote.copy(priority = Priority.Standart)
            }
            popupWindow.dismiss()
        }
        view.findViewById<TextView>(R.id.menu_priority_low1).setOnClickListener {
            (tvPriorityValue as TextView).apply {
                text = getString(R.string.priority_low)
                setTextAppearance(R.style.TextView_Body_Grey_Small)
                editingToDoNote = editingToDoNote.copy(priority = Priority.Low)
            }
            popupWindow.dismiss()
        }
        view.findViewById<TextView>(R.id.menu_priority_hight1).setOnClickListener {
            (tvPriorityValue as TextView).apply {
                text = getString(R.string.priority_hight)
                setTextAppearance(R.style.TextView_Body_Red)
                editingToDoNote = editingToDoNote.copy(priority = Priority.Hight)
            }
            popupWindow.dismiss()
        }
    }

    private fun setFragmentResultListener(supportFragmentManager: FragmentManager) {
        supportFragmentManager.setFragmentResultListener("FRAGMENT_RESULT_KEY", viewLifecycleOwner) { resultKey, bundle ->
            if (bundle.containsKey("SELECTED_DATE")) {
                val choosedDate = bundle.getLong("SELECTED_DATE")
                editingToDoNote = editingToDoNote.copy(deadline = choosedDate)
                val selectedDate =
                    SimpleDateFormat("dd MMMM yyyy", Locale("ru")).format(choosedDate)
                binding.tvDeadlineValue.text = selectedDate
                binding.tvDeadlineValue.visibility = View.VISIBLE
            } else if (binding.tvDeadlineValue.isVisible) {

            } else {
                binding.deadlineSwitch.isChecked = false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::popupWindow.isInitialized) {
            popupWindow.dismiss()
        }
    }
}

