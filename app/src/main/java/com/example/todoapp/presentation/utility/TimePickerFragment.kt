package com.example.todoapp.presentation.utility

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.todoapp.R
import java.util.Calendar


class TimePickerFragment() : DialogFragment(), TimePickerDialog.OnTimeSetListener {


    private val calendar = Calendar.getInstance()
    private val selectedTimeBundle = Bundle()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return TimePickerDialog(
            requireActivity(), R.style.MyDatePickerDialogTheme, this, hour, minute, true
        )
    }

        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val chosedDate = calendar.time.time
            selectedTimeBundle.putLong("SELECTED_TIME", chosedDate)
            setFragmentResult("FRAGMENT_TIME_RESULT_KEY", selectedTimeBundle)
        }

        override fun onDismiss(dialog: DialogInterface) {
            selectedTimeBundle.putLong("DISMISSED_TIME", 0L)
            setFragmentResult("FRAGMENT_TIME_RESULT_KEY", selectedTimeBundle)
            super.onDismiss(dialog)
        }
    }
