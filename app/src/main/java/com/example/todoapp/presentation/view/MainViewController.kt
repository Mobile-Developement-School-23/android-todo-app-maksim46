package com.example.todoapp.presentation.view

import android.app.Activity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.todoapp.R
import com.example.todoapp.data.model.OnErrorModel
import com.example.todoapp.domain.model.InfoForNavigationToScreenB
import com.example.todoapp.domain.model.NoteData
import com.example.todoapp.presentation.utility.LastSuccessSync
import com.example.todoapp.presentation.utility.PopupWindowsHandler
import com.example.todoapp.presentation.utility.startAnimation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject
/**
 * Contains logic for main screen views configuration.
 *
 * The class is voluminous, but has single responsibility
 */
class MainViewController @Inject constructor(
    private val popUpWindows: PopupWindow,
    private val itemTouchHelper: ItemTouchHelper,
    private val activity: Activity,
    private val rvAdapter: RVListAdapter,
    private val vm: MainFragmentViewModel,
    private val lastSuccessSynch: LastSuccessSync,
) {
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var rootView: View
    private lateinit var viewLifecycleOwner: LifecycleOwner

    fun setUpViews(root: View, viewLifecycleOwner1: LifecycleOwner) {
        rootView = root
        viewLifecycleOwner = viewLifecycleOwner1
        setUpArticlesList()
        setUpSwipeToRefresh()
        setUpFab()
        setUpBtHide()
        setUpBtYaLogin()
        setUpNumberOfDone()
        setUpGetLastResponce()
        setUpGetErrorMessage()
        setUpIsDoneVisible()
        setUpPopupAction()
        setUpYaLogin()
        setUpNav()
    }

    private fun setUpArticlesList() {
        val recyclerView: RecyclerView = rootView.findViewById(R.id.rv_main)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = rvAdapter
        itemTouchHelper.attachToRecyclerView(recyclerView)
        viewLifecycleOwner.lifecycleScope.launch {
            vm.listOfNotesFlow.flowWithLifecycle(
                viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED
            ).collect { dataFromDB ->
                rvAdapter.submitList(dataFromDB)
            }
        }
    }

    private fun setUpFab() {
        val fab: FloatingActionButton = rootView.findViewById(R.id.fab)
        val animCircle: View = rootView.findViewById(R.id.animCircle)
        fab.setOnClickListener {
            fab.visibility = View.INVISIBLE
            animCircle.visibility = View.VISIBLE
            animCircle.startAnimation(AnimationUtils.loadAnimation(rootView.context, R.anim.fab_anim).apply {
                interpolator = AccelerateDecelerateInterpolator()
            }) {
                vm.onNavigateAction(InfoForNavigationToScreenB(navigateToScreenB = true))
                animCircle.visibility = View.INVISIBLE
            }
        }
    }

    private fun setUpBtHide() {
        val btHide: ImageButton = rootView.findViewById(R.id.bt_hide)
        btHide.setOnClickListener { vm.flipDoneVisibility() }
    }

    private fun setUpBtYaLogin() {
        val btYALogin: ImageButton = rootView.findViewById(R.id.bt_YALoggin)
        btYALogin.setOnClickListener {
            vm.yaLoginClick(true)
        }
    }

    private fun setUpNumberOfDone() {
        val tvSubtitle: TextView = rootView.findViewById(R.id.tv_subtitle)
        viewLifecycleOwner.lifecycleScope.launch {
            vm.numberOfDone.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { amountOfDone ->
                tvSubtitle.text = activity.getString(R.string.subhead, amountOfDone)
            }
        }
    }

    private fun setUpGetLastResponce() {
        viewLifecycleOwner.lifecycleScope.launch {
            var lastSuccessSync = ""
            vm.getLastResponse().flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { lastResponce ->
                var message = ""
                if (lastResponce.status) {
                    message = activity.getString(R.string.synch)
                    lastSuccessSync =
                        SimpleDateFormat("dd MMMM yyyy HH mm", rootView.resources.configuration.locales.get(0)).format(lastResponce.date)
                    lastSuccessSynch.setLastSuccessSync(lastSuccessSync.toString())
                } else {
                    lastSuccessSync = lastSuccessSynch.getLastSuccessSync()!!
                    message = "${activity.getString(R.string.not_synch)} $lastSuccessSync"
                }
                Snackbar.make(rootView.findViewById(R.id.rv_main), message, Snackbar.LENGTH_LONG).show()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun setUpGetErrorMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            vm.getErrorMessage().flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { onErrMessage ->
                var errMessage = ""
                errMessage = when (onErrMessage) {
                    OnErrorModel.ER_400 -> activity.getString(R.string.er_400)
                    OnErrorModel.ER_401 -> activity.getString(R.string.er_401)
                    OnErrorModel.ER_404 -> activity.getString(R.string.er_404)
                    OnErrorModel.ER_UNKNOWN -> activity.getString(R.string.er_unexpected)
                    OnErrorModel.ER_INTERNAL -> activity.getString(R.string.er_internal)
                    else -> { "" }
                }
                if (errMessage.isNotEmpty()) {
                    Snackbar.make(rootView.findViewById(R.id.rv_main), errMessage, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setUpIsDoneVisible() {
        val btHide: ImageButton = rootView.findViewById(R.id.bt_hide)
        viewLifecycleOwner.lifecycleScope.launch {
            vm.isDoneVisible.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { isVisible ->
                when (isVisible) {
                    true -> btHide.setImageResource(R.drawable.ic_eye_off)
                    false -> btHide.setImageResource(R.drawable.ic_eye)
                }
            }
        }//
    }

    private fun setUpPopupAction() {
        viewLifecycleOwner.lifecycleScope.launch {
            vm.popupAction.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { popupData ->
                if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    val popupView = when (popupData.popupType) {
                        PopupWindowsHandler.PopupType.Info -> activity.layoutInflater.inflate(R.layout.popup_info, null)
                        PopupWindowsHandler.PopupType.Action -> activity.layoutInflater.inflate(R.layout.popup_note_action, null)
                        PopupWindowsHandler.PopupType.Empty -> throw IllegalArgumentException("Invalid PopupType Provided")
                    }
                    PopupWindowsHandler.createPopup(popUpWindows, popupView, popupData) { action, data ->
                        when (action) {
                            PopupWindowsHandler.CallbackAction.Done -> vm.changeDoneStatus((data as NoteData.ToDoItem))
                            PopupWindowsHandler.CallbackAction.Update -> vm.onNavigateAction(
                                InfoForNavigationToScreenB(
                                    (data as NoteData.ToDoItem).id.toInt(),
                                    navigateToScreenB = true)
                            )
                            PopupWindowsHandler.CallbackAction.Delete -> {
                                vm.delete((data as NoteData.ToDoItem).id)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setUpNav() {
        viewLifecycleOwner.lifecycleScope.launch {
            vm.navigateAction.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect {
                if (it.navigateToScreenB) {
                    findNavController(rootView).navigate(MainFragmentDirections.actionMainFragmentToNoteDetailFragment())
                }
            }
        }
    }

    private fun setUpYaLogin() {
        val tvLoginName: TextView = rootView.findViewById(R.id.tv_loginName)
        viewLifecycleOwner.lifecycleScope.launch {
            vm.yaLogin.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { yaLogin ->
                tvLoginName.text = yaLogin
            }
        }
    }

    private fun setUpSwipeToRefresh() {
        swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh)
        swipeRefreshLayout.setOnRefreshListener {
            vm.syncNotes()
        }
    }
}





