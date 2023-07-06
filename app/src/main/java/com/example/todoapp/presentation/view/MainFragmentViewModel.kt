package com.example.todoapp.presentation.view

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.todoapp.data.model.onErrorModel
import com.example.todoapp.data.repository.NoteDataRepository
import com.example.todoapp.di.ApplicationScope
import com.example.todoapp.domain.model.ClickData
import com.example.todoapp.domain.model.InfoForNavigationToScreenB
import com.example.todoapp.domain.model.LastResponse
import com.example.todoapp.domain.model.NoteData
import com.example.todoapp.domain.model.PressType
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.ToDoEntity
import com.example.todoapp.presentation.utils.PopupWindowsCreator
import com.example.todoapp.presentation.utils.itemTouchHelper.IntItemTouchHelper
import com.example.todoapp.presentation.utils.networkConnectivity.ConnectivityObserver
import com.example.todoapp.presentation.utils.networkConnectivity.NetworkConnectivityObserver
import com.example.todoapp.domain.toEntity
import com.example.todoapp.domain.toListOfNoteData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@ApplicationScope
class MainFragmentViewModel @Inject constructor(
    private val connectivityObserver: NetworkConnectivityObserver,
    private val noteDataRepository: NoteDataRepository
) : ViewModel(), IntItemTouchHelper {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

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

    /*    private val _corErrorMessage = MutableSharedFlow<onErrorModel>(0, 16)
        val corErrorMessage: SharedFlow<onErrorModel> = _corErrorMessage.asSharedFlow()*/



    private val _datePickerFragmentResultListener = MutableSharedFlow<Bundle>(0, 16)
    val datePickerFragmentResultListener:SharedFlow<Bundle> = _datePickerFragmentResultListener.asSharedFlow()


    private val _isShowDataPicker = MutableStateFlow<Boolean>(false)
    val isShowDataPicker: StateFlow<Boolean> = _isShowDataPicker.asStateFlow()

    private val _yaLoginClick = MutableStateFlow<Boolean>(false)
    val yaLoginClick: StateFlow<Boolean> = _yaLoginClick.asStateFlow()

    private val _yaLogin = MutableStateFlow<String>("")
    val yaLogin: StateFlow<String> = _yaLogin.asStateFlow()

    var editingToDoNote: ToDoEntity = NoteData.ToDoItem().toEntity()


    private var isOnline = false
    private var previousState = true
    private var currentState = true
    private lateinit var getListJob: Job

    /*    private val handler = CoroutineExceptionHandler { _, exception ->
            _corErrorMessage.tryEmit(onErrorModel.ER_INTERNAL)
            Log.d("CoroutineException", "Caught $exception")
        }*/
    private val handler = CoroutineExceptionHandler { _, exception ->
        noteDataRepository.setErrorMessage(onErrorModel.ER_INTERNAL)
        Log.d("CoroutineException", "Caught $exception")
    }

    init {
        getListOfNotes()
        countNumberOfDone()

        viewModelScope.launch(ioDispatcher + handler) {
            isDoneVisible.collect {
                getListJob.cancel()
                getListOfNotes()
            }
        }
        syncNotes()
        connectivityObserver.observe().onEach {
            when (it) {
                ConnectivityObserver.Status.Available -> {
                    isOnline = true
                    syncNotes()
                }

                else -> {
                    isOnline = false

                }
            }
            isOnline = it == ConnectivityObserver.Status.Available
            previousState = currentState
            currentState = isOnline

        }.launchIn(viewModelScope)

    }

    fun editNote(note: ToDoEntity) {
        editingToDoNote = editingToDoNote.copy(
            id = note.id,
            text = note.text,
            priority = note.priority,
            deadline = note.deadline,
            isDone = note.isDone,
            createDate = note.createDate,
            updateDate = note.updateDate
        )


    }

    fun getEditNote(): ToDoEntity {
        Log.d("SECOND", "GET $editingToDoNote  ")
        return editingToDoNote
    }

    fun setShowDataPicker(isShow: Boolean) {
        _isShowDataPicker.value = true
        _isShowDataPicker.value = false
    }


    private fun getListOfNotes() {
        getListJob = viewModelScope.launch(ioDispatcher + handler) {

            (noteDataRepository.getToDoNoteList(_isDoneVisible.value)).collect { toDoEntityList ->
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

    fun getLastResponse(): Flow<LastResponse> {
        return noteDataRepository.syncStatusResponse
    }

    fun getErrorMessage(): SharedFlow<onErrorModel> {
        return noteDataRepository.onErrorMessage
    }

    fun setDatePickerResult(bundle: Bundle) {
        _datePickerFragmentResultListener.tryEmit(bundle)
    }

/*    fun getDatePickerResult(): SharedFlow<Bundle> {
        return datePickerFragmentResultListener
    }*/

    private fun countNumberOfDone() {
        viewModelScope.launch(ioDispatcher + handler) {
            _numberOfDone.emitAll(
                noteDataRepository.getNumberOfDone()
            )
        }
    }

    fun changeDoneStatus(note: NoteData.ToDoItem) {
        viewModelScope.launch(ioDispatcher + handler) {

            noteDataRepository.updateDoneStatus(note, isOnline)
        }
    }

    fun addNewNote(note: ToDoEntity) {
        viewModelScope.launch(ioDispatcher + handler) {
            noteDataRepository.saveToDoNote(note, isOnline)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch(ioDispatcher + handler) {
            noteDataRepository.deleteToDoNote(id, isOnline)
        }
    }

    fun updateToDoNote(note: ToDoEntity) {          //save
        viewModelScope.launch(ioDispatcher + handler) {
            noteDataRepository.updateToDoNote(note, isOnline)
        }
    }

    fun flipDoneVisibility() {
        _isDoneVisible.update { it.not() }
    }

    fun yaLoginClick(value: Boolean) {
        _yaLoginClick.update { value }
        _yaLoginClick.update { false }
    }

    private fun onNoteClickAction(note: ClickData) {
        when (note.pressType) {
            PressType.LONG -> viewModelScope.launch(ioDispatcher + handler) {
                _popupAction.update {
                    PopupWindowsCreator.PopupData(
                        note = note.noteData,
                        view = note.view,
                        popupType = PopupWindowsCreator.PopupType.Action
                    )
                }
            }

            PressType.SHORT -> onNavigateAction(InfoForNavigationToScreenB((note.noteData as NoteData.ToDoItem).id.toInt(), navigateToScreenB = true))
        }
    }


    private fun onInfoClickAction(popupData: PopupWindowsCreator.PopupData) {
        _popupAction.update { popupData }
    }

    private fun onFooterClickAction(text: String) {
        addNewNote(NoteData.ToDoItem(text = text, createDate = Date(), updateDate = Date()).toEntity())
    }

    private fun getToDoNoteForEdit(id: Int) {
        if (id > 0) {
            viewModelScope.launch(ioDispatcher + handler) {
                _toDoNoteByIdForEdit.update { noteDataRepository.getToDoNote(id.toString()) }
            }
        } else {
            viewModelScope.launch(ioDispatcher + handler) {
                _toDoNoteByIdForEdit.update {
                    NoteData.ToDoItem().toEntity()
                }
            }
        }
    }

    override fun onItemSwiped(position: Int, direction: Int) {
        val swipedNoteId = (listOfNotesFlow.value[position] as? NoteData.ToDoItem)
        when (direction) {
            ItemTouchHelper.LEFT -> swipedNoteId?.let { delete(it.id) }
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

    fun syncNotes() {
        // val vmscope = viewModelScope
        noteDataRepository.syncNotes(isOnline)
    }

    fun yaLogin(token: String) {
        viewModelScope.launch(ioDispatcher + handler) {
            _yaLogin.emitAll(noteDataRepository.yaLogin(token, isOnline))
        }
    }

}






