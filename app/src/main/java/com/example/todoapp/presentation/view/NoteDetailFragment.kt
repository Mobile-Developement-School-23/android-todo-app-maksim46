package com.example.todoapp.presentation.view


import AppTheme
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoNotDisturbAlt
import androidx.compose.material.icons.filled.LowPriority
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.todoapp.R
import com.example.todoapp.ToDoAppApp
import com.example.todoapp.databinding.FragmentNoteBinding
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.ToDoEntity
import com.example.todoapp.presentation.utils.LocalMyColors
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

/**
 * Second fragment of the application.
 * Contains logic of ContexMenu and DataPickerFragment management.
 */
class NoteDetailFragment : Fragment() {
    private val binding by viewBinding(FragmentNoteBinding::bind)
    private val vm: MainFragmentViewModel by activityViewModels()

    /*    @Inject
        lateinit var noteDetailViewController: NoteDetailViewController*/

    private val component by lazy {
        (requireActivity().application as ToDoAppApp).component.fragmentNoteDetailComponent()
            .create(this, this.requireActivity())
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        component.inject(this)
        val view = ComposeView(requireContext()).apply {
            setContent {
                AppTheme() {
                    NoteDetailScreen()
                }
            }
        }
        return view
    }


    @OptIn(ExperimentalMaterialApi::class)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter", "SuspiciousIndentation")
    @Composable
    fun NoteDetailScreen() {


        val (isUpdated, setIsUpdated) = remember { mutableStateOf(false) }

        val (noteId, setNoteId) = remember { mutableStateOf("0") }

        val (noteText, setNoteText) = remember { mutableStateOf("") }

        var (notePriority, setPriority) = remember { mutableStateOf(Priority.Standart) }
        var priorityExpand by remember { mutableStateOf(false) }

        val (deadline, setDeadline) = remember { mutableStateOf(0L) }
        var isDeadlineSwitchEnable by remember { mutableStateOf(false) }

        val (isDone, setIsDone) = remember { mutableStateOf(false) }
        val (createDate, setCreateDate) = remember { mutableStateOf(Date(0)) }
        val (updateDate, setUpdateDate) = remember { mutableStateOf(Date(0)) }

        val (noteData, setNoteData) = remember { mutableStateOf(ToDoEntity("0", "", Priority.Standart, 0, false, Date(0), Date(0))) }

        var isVisible by remember { mutableStateOf(false) }

        val dateDialogState = rememberMaterialDialogState()

        val bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
        val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)
        val scope = rememberCoroutineScope()
        val selectedPriority by remember { mutableStateOf(notePriority) }

        LaunchedEffect(vm.toDoNoteByIdForEdit) {
            viewLifecycleOwner.lifecycleScope.launch {
                vm.toDoNoteByIdForEdit.flowWithLifecycle(
                    viewLifecycleOwner.lifecycle,
                    Lifecycle.State.STARTED
                ).collect { nData ->
                    setNoteId(nData.id)
                    setNoteText(nData.text)
                    if (nData.text.isNotEmpty()) {
                        setIsUpdated(true)
                    }
                    setPriority(nData.priority)
                    setDeadline(nData.deadline)
                    if (nData.deadline != 0L) {
                        isDeadlineSwitchEnable = true
                    }
                    setIsDone(nData.isDone)
                    setCreateDate(nData.createDate)
                    setUpdateDate(nData.updateDate)
                    setNoteData(nData)
                }
            }
        }
        data class PriorityItem(val title: String, val icon: ImageVector, val priority: Priority)


        val bottomSheetPriorityItems = listOf(
            PriorityItem(title = stringResource(R.string.priority_standart), icon = Icons.Default.DoNotDisturbAlt, Priority.Standart),
            PriorityItem(title = stringResource(R.string.priority_low), icon = Icons.Default.LowPriority, Priority.Low),
            PriorityItem(title = stringResource(R.string.priority_hight), icon = Icons.Default.PriorityHigh, Priority.High),
        )


        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            //sheetShape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            sheetBackgroundColor = LocalMyColors.current.colorBackSecondary,
            sheetElevation = 16.dp,
            drawerElevation = 16.dp,
            sheetContent = {
                Column(
                    content = {

                        Spacer(modifier = Modifier.padding(16.dp))
                        Text(
                            text = stringResource(R.string.choose_priority),
                            modifier = Modifier
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.body1,
                            color = LocalMyColors.current.colorBlue,
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                        ) {
                            items(bottomSheetPriorityItems.size, itemContent = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp)
                                        .clickable {
                                            setPriority(bottomSheetPriorityItems[it].priority)
                                        },
                                ) {
                                    Spacer(modifier = Modifier.padding(8.dp))
                                    Icon(
                                        bottomSheetPriorityItems[it].icon,
                                        bottomSheetPriorityItems[it].title,
                                        tint = if (notePriority == bottomSheetPriorityItems[it].priority) LocalMyColors.current.colorBlue else LocalMyColors.current.colorTertiary
                                    )
                                    Spacer(modifier = Modifier.padding(8.dp))
                                    Text(text = bottomSheetPriorityItems[it].title,
                                        color = if (notePriority == bottomSheetPriorityItems[it].priority) LocalMyColors.current.colorBlue else LocalMyColors.current.colorTertiary)
                                }

                            })
                        }
                    },
                    modifier = Modifier
                        .border(
                            width = 2.dp,
                            color = LocalMyColors.current.colorSupportSeparator,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .fillMaxWidth()
                        .height(250.dp)

                        .background(LocalMyColors.current.colorBackSecondary)
                        .padding(16.dp),

                    )
            },


//////////////////////////////////////////////////////////////////////////////


        ) {
            Column(
                modifier = Modifier
                    .background(LocalMyColors.current.colorBackPrimary)
                    .fillMaxWidth()


            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()

                ) {
                    Column(
                        modifier = Modifier.weight(0.3f)
                    ) {

                        IconButton(onClick = {
                            !isVisible
                            findNavController().popBackStack()
                        })
                        {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = LocalMyColors.current.colorPrimary
                            )
                        }

                    }
                    TextButton(
                        onClick = {

                            if (createDate == Date(0)) {
                                setCreateDate(Date())
                                setUpdateDate(Date(0))
                            } else {
                                setUpdateDate(Date(0))
                            }
                            if (checkBeforeSave(noteText)) {
                                if (isUpdated) {
                                    vm.updateToDoNote(ToDoEntity(noteId, noteText, notePriority, deadline, isDone, createDate, updateDate))
                                } else {
                                    vm.addNewNote(ToDoEntity(noteId, noteText, notePriority, deadline, isDone, createDate, updateDate))
                                }
                                !isVisible
                                findNavController().popBackStack()
                            }
                        },
                        Modifier.padding(end = 8.dp)
                    )
                    {
                        Text(
                            stringResource(id = R.string.save_button),
                            style = MaterialTheme.typography.button,
                            color = LocalMyColors.current.colorBlue
                        )
                    }
                }


                //////////////////////////////////////

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = LocalMyColors.current.colorBackPrimary)
                )
                {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            backgroundColor = LocalMyColors.current.colorBackSecondary,
                            shape = RoundedCornerShape(8.dp),
                            elevation = 4.dp
                        ) {
                            TextField(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                value = noteText,
                                onValueChange = { setNoteText(it) },
                                textStyle = LocalTextStyle.current.copy(
                                    color = LocalMyColors.current.colorPrimary
                                ),
                                singleLine = false,
                                maxLines = Int.MAX_VALUE,
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = LocalMyColors.current.colorBackSecondary,
                                    cursorColor = MaterialTheme.colors.onSurface,
                                    focusedIndicatorColor = LocalMyColors.current.colorBackSecondary,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }


                        //////////////////////////////////////
                        Column {
                            Text(
                                text = stringResource(id = R.string.priority),
                                style = MaterialTheme.typography.body1,

                                modifier = Modifier.padding(top = 26.dp, start = 16.dp),
                                color = LocalMyColors.current.colorPrimary
                            )

                            Text(text =
                            when (notePriority) {
                                Priority.Standart -> stringResource(R.string.priority_standart)
                                Priority.Low -> stringResource(R.string.priority_low)
                                Priority.High -> stringResource(R.string.priority_hight)
                            },
                                color = if (noteData.priority == Priority.High) {
                                    LocalMyColors.current.colorRed
                                } else {
                                    LocalMyColors.current.colorSecondary
                                },
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier
                                    .padding(top = 16.dp, bottom = 16.dp, start = 16.dp)
                                    .clickable { priorityExpand = true }
                            )

                            DropdownMenu(
                                expanded = priorityExpand,
                                onDismissRequest = { priorityExpand = false },
                                modifier = Modifier
                                    .background(LocalMyColors.current.colorBackSecondary)
                                    .width(200.dp)
                            ) {
                                Text(
                                    stringResource(R.string.priority_standart),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            notePriority = Priority.Standart
                                            priorityExpand = false
                                        }
                                        .padding(4.dp),
                                    color = LocalMyColors.current.colorPrimary
                                )
                                Divider()
                                Text(
                                    stringResource(R.string.priority_low),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            notePriority = Priority.Low
                                            priorityExpand = false
                                        }
                                        .padding(4.dp),
                                    color = LocalMyColors.current.colorPrimary
                                )
                                Divider()
                                Text(
                                    stringResource(R.string.priority_hight),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            notePriority = Priority.High
                                            priorityExpand = false
                                        }
                                        .padding(4.dp),
                                    color = LocalMyColors.current.colorRed
                                )
                            }

                        }
                        //////////////////////////////////////

                        Divider(
                            modifier = Modifier.padding(16.dp),
                            color = LocalMyColors.current.colorTertiary
                        )

                        //////////////////////////////////////

                        Text(
                            stringResource(id = R.string.deadline),
                            style = MaterialTheme.typography.body1,
                            color = LocalMyColors.current.colorPrimary,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.weight(0.3f)
                            ) {

                                Text(
                                    text = if (deadline != 0L) {
                                        SimpleDateFormat(
                                            "dd MMMM yyyy",
                                            requireActivity().resources.configuration.locales.get(0)
                                        ).format(deadline)
                                    } else {
                                        ""
                                    },
                                    color = LocalMyColors.current.colorBlue,
                                    style = MaterialTheme.typography.body2,
                                    modifier = Modifier.padding(start = 16.dp)
                                )

                            }
                            Switch(
                                checked = isDeadlineSwitchEnable,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = LocalMyColors.current.colorBlue,
                                    checkedTrackColor = LocalMyColors.current.colorBlue,
                                ),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        dateDialogState.show()
                                        isDeadlineSwitchEnable = true
                                    } else {
                                        setDeadline(0L)
                                        isDeadlineSwitchEnable = false
                                    }
                                },
                                modifier = Modifier.padding(start = 16.dp)
                            )
                            MaterialDialog(

                                dialogState = dateDialogState,
                                buttons = {

                                    positiveButton(
                                        text = "Ok"
                                    )
                                    negativeButton(text = "Cancel") {
                                        isDeadlineSwitchEnable = false
                                    }
                                }
                            ) {
                                datepicker(
                                    initialDate = LocalDate.now(),
                                    title = "Pick a date",
                                    colors = DatePickerDefaults.colors(

                                        headerBackgroundColor = LocalMyColors.current.colorBlue,
                                        dateActiveBackgroundColor = LocalMyColors.current.colorBlue,

                                        ),
                                    allowedDateValidator = { date ->
                                        !date.isBefore(LocalDate.now())

                                    }
                                ) {
                                    setDeadline((Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant())).time)
                                }
                            }
                        }

                        //////////////////////////////////////////////

                        Divider(
                            modifier = Modifier.padding(16.dp),
                            color = LocalMyColors.current.colorTertiary
                        )

                        /////////////////////////////////////////////

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    if (isUpdated) {
                                        vm.delete(noteId)
                                        findNavController().popBackStack()
                                    }
                                }
                        ) {
                            IconButton(
                                onClick = {
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_trash),
                                    contentDescription = stringResource(id = R.string.delete),
                                    tint = if (isUpdated) {
                                        LocalMyColors.current.colorRed
                                    } else {
                                        LocalMyColors.current.colorTertiary
                                    }
                                )
                            }
                            Text(
                                text = stringResource(id = R.string.delete),
                                style = MaterialTheme.typography.subtitle1,
                                color = if (isUpdated) {
                                    LocalMyColors.current.colorRed
                                } else {
                                    LocalMyColors.current.colorTertiary
                                }
                            )
                        }
                    }
                }
            }

        }

    }


    private fun checkBeforeSave(text: String): Boolean {
        return if (text.isEmpty()) {
            Toast.makeText(activity, R.string.emptyText_Toast, Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

}


