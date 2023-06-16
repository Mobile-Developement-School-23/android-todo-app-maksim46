package com.example.todoapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.todoapp.data.repository.NoteDataRepositoryImpl
import com.example.todoapp.di.ApplicationScope
import com.example.todoapp.domain.model.*
import com.example.todoapp.presentation.utils.*
import com.example.todoapp.presentation.utils.itemTouchHelper.IntItemTouchHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@ApplicationScope
class MainFragmentViewModel @Inject constructor(
    private val noteDataRepositoryImpl: NoteDataRepositoryImpl
) : ViewModel(), IntItemTouchHelper {


    private val _listOfNotesFlow = MutableStateFlow<List<NoteData>>(emptyList())
    val listOfNotesFlow: StateFlow<List<NoteData>> = _listOfNotesFlow.asStateFlow()

    private val _numberOfDone = MutableStateFlow<Int>(0)
    val numberOfDone: StateFlow<Int> = _numberOfDone.asStateFlow()

    private val _isDoneVisible = MutableStateFlow<Boolean>(false)
    val isDoneVisible: StateFlow<Boolean> = _isDoneVisible.asStateFlow()

    private val _popupAction = MutableStateFlow<PopupWindowsCreator.PopupData>(PopupWindowsCreator.PopupData())
    val popupAction: StateFlow<PopupWindowsCreator.PopupData> = _popupAction.asStateFlow()

    private val _navigateAction = MutableStateFlow<InfoForNavigationToScreenB>(InfoForNavigationToScreenB())
    val navigateAction: StateFlow<InfoForNavigationToScreenB> = _navigateAction.asStateFlow()

    private val _toDoNoteByIdForEdit = MutableStateFlow<ToDoEntity>(ToDoEntity("0", "", Priority.Standart, 0, false, Date(0), Date(0)))
    val toDoNoteByIdForEdit: StateFlow<ToDoEntity> = _toDoNoteByIdForEdit.asStateFlow()

    private lateinit var getListJob: Job


    init {
        getListOfNotes()
        countNumberOfDone()

        viewModelScope.launch(Dispatchers.IO) {
            isDoneVisible.collect {
                getListJob.cancel()
                getListOfNotes()
            }
        }
    }


    private fun getListOfNotes() {                                                                      // функция громоздская, но оставил так т.к. выполняет одно законченное действие - получает список заметок из БД. Промежуточные данные нигде больше не нужны.
            getListJob = viewModelScope.launch {
            (noteDataRepositoryImpl.getToDoNoteList(_isDoneVisible.value)).collect { toDoEntityList ->
                _listOfNotesFlow.update {
                    mutableListOf<NoteData>().apply {
                        addAll(toDoEntityList.toListOfNoteData().map { noteData ->
                            (noteData as NoteData.ToDoItem).copy(
                                onNoteClick = ::onNoteClickAction,
                                onInfoClick = ::onInfoClickAction
                            )
                        })
                        add(NoteData.FooterItem(onAddClick = ::onFooterClickAction))
                    }
                }
            }
        }
    }

    private fun countNumberOfDone() {
        viewModelScope.launch(Dispatchers.IO) {
            _numberOfDone.emitAll(
                noteDataRepositoryImpl.getNumberOfDone()
            )
        }
    }


    private fun addNewNote(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDataRepositoryImpl.addToDoNote(
                NoteData.ToDoItem(text = text, createDate = Date(), updateDate = Date())
                    .toEntity()
            )

        }
    }

    fun flipDoneVisibility() {
        _isDoneVisible.update { it.not() }
    }

    private fun onNoteClickAction(note: ClickData) {
        when (note.pressType) {
            PressType.LONG -> viewModelScope.launch(Dispatchers.IO) {
                _popupAction.update {
                    PopupWindowsCreator.PopupData(
                        NoteData.ToDoItem(id = note.id),
                        view = note.view,
                        popupType = PopupWindowsCreator.PopupType.Action
                    )
                }
            }
            PressType.SHORT -> onNavigateAction(InfoForNavigationToScreenB(note.id.toInt(), navigateToScreenB = true))
        }
    }

    fun changeDoneStatus(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDataRepositoryImpl.updateDoneStatus(id.toInt())
        }
    }


    private fun onInfoClickAction(popupData: PopupWindowsCreator.PopupData) {
        _popupAction.update { popupData }
    }

    private fun onFooterClickAction(text: String) {
        addNewNote(text)
    }

    fun delete(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDataRepositoryImpl.deleteToDoNote(id)
        }
    }

    override fun onItemSwiped(position: Int, direction: Int) {
        val swipedNoteId = (listOfNotesFlow.value[position] as? NoteData.ToDoItem)?.id
        when (direction) {
            ItemTouchHelper.LEFT -> swipedNoteId?.let { delete(it) }
            ItemTouchHelper.RIGHT -> swipedNoteId?.let {
                changeDoneStatus(it)
            }
        }
    }

    fun onNavigateAction(navInfo: InfoForNavigationToScreenB) {
        getToDoNoteForEdit(navInfo.id)
        _navigateAction.update { navInfo }
        _navigateAction.update { navInfo.copy(navigateToScreenB = false) }
    }

    private fun getToDoNoteForEdit(id: Int) {
        if (id > 0) {
            viewModelScope.launch(Dispatchers.IO) {
                _toDoNoteByIdForEdit.update { noteDataRepositoryImpl.getToDoNote(id.toString()) }
            }
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                _toDoNoteByIdForEdit.update {
                    NoteData.ToDoItem().toEntity()
                }
            }
        }
    }

    fun updateToDoNote(note: ToDoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDataRepositoryImpl.insertToDoNote(note)
        }

    }
}





