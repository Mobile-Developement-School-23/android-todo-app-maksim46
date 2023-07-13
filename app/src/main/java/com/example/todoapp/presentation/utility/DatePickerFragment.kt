package com.example.todoapp.presentation.utility

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.todoapp.R
import java.util.Calendar
import java.util.Date

/**
 * DialogFragment of [NoteDetailFragment] for pick deadline date
 */

private const val DAY_IN_MS=86400000
class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private val calendar = Calendar.getInstance()
    private val selectedDateBundle = Bundle()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(requireActivity(), R.style.MyDatePickerDialogTheme, this, year, month, day)
        dpd.datePicker.minDate = Date().time
        return dpd
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val chosedDate = calendar.time.time
        selectedDateBundle.putLong("SELECTED_DATE", chosedDate)
        setFragmentResult("FRAGMENT_DATE_RESULT_KEY", selectedDateBundle)
    }

    override fun onDismiss(dialog: DialogInterface) {
        selectedDateBundle.putLong("DISMISSED_DATE", 0L)
        setFragmentResult("FRAGMENT_DATE_RESULT_KEY", selectedDateBundle)
        super.onDismiss(dialog)
    }

}


