package com.example.todoapp.presentation

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get
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
import java.util.Date

class NoteDetailFragment : Fragment(R.layout.fragment_note) {
    private val binding by viewBinding(FragmentNoteBinding::bind)
    private val vm: MainFragmentViewModel by activityViewModels()
    var isUserTriggeredDeadLineSwitch = false
    var isUpdated = true
    private var editingToDoNote: ToDoEntity = NoteData.ToDoItem().toEntity()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val supportFragmentManager = requireActivity().supportFragmentManager
        setDatePickerFragmentResultListener(supportFragmentManager)

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

                        Priority.High -> {
                            tvPriorityValue.text = getString(R.string.priority_hight)
                            tvPriorityValue.setTextAppearance(R.style.TextView_Body_Red)
                        }
                    }
                    /*     val currentLocale = resources.configuration.locales.get(0)
                         val a = Locale("ru")*/
                    if (noteData.deadline != 0L) {
                        binding.tvDeadlineValue.text =
                            SimpleDateFormat("dd MMMM yyyy", resources.configuration.locales.get(0)).format(noteData.deadline)
                        deadlineSwitch.isChecked = true
                    } else {
                        deadlineSwitch.isChecked = false
                        tvDeadlineValue.visibility = View.GONE
                    }
                    isUserTriggeredDeadLineSwitch = true

                    if (noteData.text.isEmpty()) {
                        isUpdated=false
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
                if (isUserTriggeredDeadLineSwitch) {
                    if (isChecked) {
                        DatePickerFragment().show(supportFragmentManager, "DatePickerFragment")
                    } else {
                        editingToDoNote = editingToDoNote.copy(deadline = 0L)
                        tvDeadlineValue.visibility = View.GONE
                    }
                }
            }

            registerForContextMenu(tvPriorityValue)
            tvPriorityValue.setOnClickListener {
                it.showContextMenu(0F, 0F)
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
                    if (isUpdated){
                        vm.updateToDoNote(editingToDoNote)
                    }
                   else{
                       vm.addNewNote(editingToDoNote)
                    }
                    findNavController().popBackStack()
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
                    editingToDoNote = editingToDoNote.copy(priority = Priority.Low)
                }
                true
            }

            R.id.option_medium -> {
                binding.tvPriorityValue.apply {
                    text = getString(R.string.priority_standart)
                    setTextAppearance(R.style.TextView_Body_Grey_Small)
                    editingToDoNote = editingToDoNote.copy(priority = Priority.Standart)
                }
                true
            }

            R.id.option_high -> {
                binding.tvPriorityValue.apply {
                    text = getString(R.string.priority_hight)
                    setTextAppearance(R.style.TextView_Body_Red)
                    editingToDoNote = editingToDoNote.copy(priority = Priority.High)
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

    private fun setDatePickerFragmentResultListener(supportFragmentManager: FragmentManager) {
        supportFragmentManager.setFragmentResultListener("FRAGMENT_RESULT_KEY", viewLifecycleOwner) { resultKey, bundle ->
            if (bundle.containsKey("SELECTED_DATE")) {
                val choosedDate = bundle.getLong("SELECTED_DATE")
                editingToDoNote = editingToDoNote.copy(deadline = choosedDate)
                val selectedDate =
                    SimpleDateFormat("dd MMMM yyyy", resources.configuration.locales.get(0)).format(choosedDate)
                binding.tvDeadlineValue.text = selectedDate
                binding.tvDeadlineValue.visibility = View.VISIBLE
                isUserTriggeredDeadLineSwitch = false
                binding.deadlineSwitch.isChecked = true
                isUserTriggeredDeadLineSwitch = true
            }
        }
    }
}

