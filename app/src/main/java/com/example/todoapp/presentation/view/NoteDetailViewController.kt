package com.example.todoapp.presentation.view

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import com.example.todoapp.R
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.ToDoEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

/**
 * Contains logic for second screen views configuration.
 */

class NoteDetailViewController @Inject constructor(
    private val activity: Activity,
    private val vm: MainFragmentViewModel,
) {
    private lateinit var rootView: View
    private lateinit var viewLifecycleOwner: LifecycleOwner
    var isUserTriggeredDeadLineSwitch = false
    var isUpdated = true

    fun setUpViews(root: View, viewLifecycleOwner1: LifecycleOwner) {
        rootView = root
        viewLifecycleOwner = viewLifecycleOwner1
        setUpInitial()
        setUpButtonsBehaviour()
        setUpDatePickerResult()
    }

    private fun setUpInitial() {

        val tvDeadlineValue: TextView = rootView.findViewById(R.id.tv_deadline_value)
        val deadlineSwitch: SwitchCompat = rootView.findViewById(R.id.deadlineSwitch)
        val etNoteText: TextView = rootView.findViewById(R.id.et_noteText)
        val deleteGroup: View = rootView.findViewById(R.id.deleteGroup)
        val delete: TextView = rootView.findViewById(R.id.delete)
        val ivTrash: ImageView = rootView.findViewById(R.id.ivTrash)
        val tvPriorityValue: TextView = rootView.findViewById(R.id.tv_priority_value)


        viewLifecycleOwner.lifecycleScope.launch {
            vm.toDoNoteByIdForEdit.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect { noteData ->
                vm.editNote(noteData)

                etNoteText.text = noteData.text

                when (noteData.priority) {

                    Priority.Standart -> {
                        tvPriorityValue.text = activity.getString(com.example.todoapp.R.string.priority_standart)
                        tvPriorityValue.setTextAppearance(R.style.TextView_Body_Grey_Small)
                    }

                    Priority.Low -> {
                        tvPriorityValue.text = activity.getString(com.example.todoapp.R.string.priority_low)
                        tvPriorityValue.setTextAppearance(R.style.TextView_Body_Grey_Small)
                    }

                    Priority.High -> {
                        tvPriorityValue.text = activity.getString(com.example.todoapp.R.string.priority_hight)
                        tvPriorityValue.setTextAppearance(R.style.TextView_Body_Red)
                    }
                }

                if (noteData.deadline != 0L) {
                    tvDeadlineValue.text =
                        SimpleDateFormat("dd MMMM yyyy", activity.resources.configuration.locales.get(0))
                            .format(noteData.deadline)
                    deadlineSwitch.isChecked = true
                } else {
                    deadlineSwitch.isChecked = false
                    tvDeadlineValue.visibility = View.GONE
                }
                isUserTriggeredDeadLineSwitch = true

                if (noteData.text.isEmpty()) {
                    isUpdated = false
                    deleteGroup.isEnabled = false

                } else {
                    deleteGroup.isEnabled = true
                    delete.setTextAppearance(R.style.TextView_Body_Red)
                    ivTrash.setColorFilter(androidx.core.content.ContextCompat.getColor(activity,R.color.L_color_red))
                }
            }
        }
    }

    private fun setUpButtonsBehaviour() {
        val tvDeadlineValue: TextView = rootView.findViewById(R.id.tv_deadline_value)
        val deadlineSwitch: SwitchCompat = rootView.findViewById(R.id.deadlineSwitch)
        val etNoteText: TextView = rootView.findViewById(R.id.et_noteText)
        val deleteGroup: View = rootView.findViewById(R.id.deleteGroup)
        val ibClose: ImageView = rootView.findViewById(R.id.ib_close)
        val tvSave: TextView = rootView.findViewById(R.id.tv_save)
        deleteGroup.setOnClickListener {
            vm.delete(vm.getEditNote().id)
            findNavController(rootView).popBackStack()
        }
        ibClose.setOnClickListener {
            findNavController(rootView).popBackStack()
        }


        deadlineSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isUserTriggeredDeadLineSwitch) {
                if (isChecked) {
                    vm.setShowDataPicker(true)
                } else {
                    vm.editNote(vm.getEditNote().copy(deadline = 0L))
                    tvDeadlineValue.visibility = View.GONE
                }
            }
        }


        tvSave.setOnClickListener {
            var editingToDoNote = vm.getEditNote()

            editingToDoNote = editingToDoNote.copy(text = etNoteText.text.toString())
            editingToDoNote =
                if (editingToDoNote.createDate == Date(0)) {
                    editingToDoNote.copy(createDate = Date(), updateDate = Date())
                } else {
                    editingToDoNote.copy(updateDate = Date())
                }
            if (checkBeforeSave(editingToDoNote)) {
                if (isUpdated) {
                    vm.updateToDoNote(editingToDoNote)
                } else {
                    vm.addNewNote(editingToDoNote)
                }
                findNavController(rootView).popBackStack()
            }
        }
    }

    private fun setUpDatePickerResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            vm.datePickerFragmentResultListener.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect { bundle ->
                if (bundle.containsKey("SELECTED_DATE")) {
                    val chosedDate = bundle.getLong("SELECTED_DATE")
                    vm.editNote(vm.getEditNote().copy(deadline = chosedDate))
                    val selectedDate =
                        SimpleDateFormat("dd MMMM yyyy", activity.resources.configuration.locales.get(0)).format(chosedDate)
                    val tvDeadlineValue: TextView = rootView.findViewById(R.id.tv_deadline_value)
                    val deadlineSwitch: SwitchCompat = rootView.findViewById(R.id.deadlineSwitch)
                    tvDeadlineValue.text = selectedDate
                    tvDeadlineValue.visibility = View.VISIBLE
                    isUserTriggeredDeadLineSwitch = false
                    deadlineSwitch.isChecked = true
                    isUserTriggeredDeadLineSwitch = true
                }
            }
        }
    }

    private fun checkBeforeSave(editingToDoNote: ToDoEntity): Boolean {
        return if (editingToDoNote.text.isEmpty()) {
            Toast.makeText(activity, R.string.emptyText_Toast, Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

}