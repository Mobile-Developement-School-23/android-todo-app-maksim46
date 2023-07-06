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
import com.example.todoapp.data.model.onErrorModel
import com.example.todoapp.domain.model.InfoForNavigationToScreenB
import com.example.todoapp.domain.model.NoteData
import com.example.todoapp.di.NoteFragmentComponent
import com.example.todoapp.presentation.utils.LastSuccessSync
import com.example.todoapp.presentation.utils.PopupWindowsCreator
import com.example.todoapp.presentation.utils.startAnimation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class MainViewController(

    private val popUpWindows: PopupWindow,
    private val itemTouchHelper: ItemTouchHelper,
    private val component: NoteFragmentComponent,
    private val activity: Activity,
    private val rootView: View,
    private val rvAdapter: RVListAdapter,
    private val viewLifecycleOwner: LifecycleOwner,
    private val vm: MainFragmentViewModel,
    // private  val bindings:View
) {
    lateinit var swipeRefreshLayout: SwipeRefreshLayout





    fun setUpViews() {

        setUpArticlesList()
        setUpSwipeToRefresh()
        setUpFab()
        setUpBtHide()
        setUpBtYaLoggin()
        setUpNumberOfDone()
        setUpGetLastResponce()
        setUpGetErrorMessage()
       // setUpCorErrorMessage()
        setUpIsDoneVisible()
        setUpPopupAction()
        setUpYaLogin()
        setUpNav()
    }

    fun setUpArticlesList() {


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

    fun setUpFab() {
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


    fun setUpBtHide() {
        val btHide: ImageButton = rootView.findViewById(R.id.bt_hide)

        btHide.setOnClickListener { vm.flipDoneVisibility() }


    }

    fun setUpBtYaLoggin() {
        val btYALoggin: ImageButton = rootView.findViewById(R.id.bt_YALoggin)
        btYALoggin.setOnClickListener {
            vm.yaLoginClick(true)
        }

    }


    fun setUpNumberOfDone() {
        val tvSubtitle: TextView = rootView.findViewById(R.id.tv_subtitle)
        viewLifecycleOwner.lifecycleScope.launch {
            vm.numberOfDone.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { amountOfDone ->
                tvSubtitle.text = activity.getString(R.string.subhead, amountOfDone)
            }
        }
    }

    fun setUpGetLastResponce() {
        viewLifecycleOwner.lifecycleScope.launch {
            var lastSuccessSync = ""
            vm.getLastResponse().flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { lastResponce ->
                var message = ""
                if (lastResponce.status) {
                    message = activity.getString(R.string.synch)
                    lastSuccessSync =
                        SimpleDateFormat("dd MMMM yyyy HH mm", rootView.resources.configuration.locales.get(0)).format(lastResponce.date)
                    LastSuccessSync().setLastSuccessSync(lastSuccessSync.toString())
                } else {
                    lastSuccessSync = LastSuccessSync().getLastSuccessSync()!!
                    message = "${activity.getString(R.string.not_synch)} $lastSuccessSync"
                }
                Snackbar.make(rootView.findViewById(R.id.rv_main), message, Snackbar.LENGTH_LONG).show()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    fun setUpGetErrorMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            vm.getErrorMessage().flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { onErrMessage ->
                var errMessage = ""
                when (onErrMessage) {
                    onErrorModel.ER_400 -> errMessage = activity.getString(R.string.er_400)
                    onErrorModel.ER_401 -> errMessage = activity.getString(R.string.er_401)
                    onErrorModel.ER_404 -> errMessage = activity.getString(R.string.er_404)
                    onErrorModel.ER_UNKNOWN -> errMessage = activity.getString(R.string.er_unexpected)
                    onErrorModel.ER_INTERNAL -> errMessage = activity.getString(R.string.er_internal)
                    else -> {  errMessage = ""   }
                }
                if (errMessage.isNotEmpty()) {
                    Snackbar.make(rootView.findViewById(R.id.rv_main), errMessage, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
/*    fun setUpCorErrorMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            vm.corErrorMessage.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { corErrorMessage ->
                var errMessage = ""
                when (onErrMessage) {
                    onErrorModel.ER_400 -> errMessage = activity.getString(R.string.er_400)
                    onErrorModel.ER_401 -> errMessage = activity.getString(R.string.er_401)
                    onErrorModel.ER_404 -> errMessage = activity.getString(R.string.er_404)
                    onErrorModel.ER_UNKNOWN -> errMessage = activity.getString(R.string.er_unexpected)
                    onErrorModel.ER_INTERNAL -> errMessage = activity.getString(R.string.er_internal)
                    else -> {  errMessage = ""   }
                }
                if (errMessage.isNotEmpty()) {
                    Snackbar.make(rootView.findViewById(R.id.rv_main), activity.getString(R.string.unexpected_error), Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }*/

    fun setUpIsDoneVisible() {
        val btHide: ImageButton = rootView.findViewById(R.id.bt_hide)

        viewLifecycleOwner.lifecycleScope.launch {
            vm.isDoneVisible.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { isVisible ->
                when (isVisible) {
                    true -> btHide.setImageResource(R.drawable.ic_eye_off)
                    false -> btHide.setImageResource(R.drawable.ic_eye)

                }
            }
        }
    }

    fun setUpPopupAction() {
        viewLifecycleOwner.lifecycleScope.launch {
            vm.popupAction.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { popupData ->
                if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    val popupView = when (popupData.popupType) {
                        PopupWindowsCreator.PopupType.Info -> activity.layoutInflater.inflate(R.layout.popup_info, null)
                        PopupWindowsCreator.PopupType.Action -> activity.layoutInflater.inflate(R.layout.popup_note_action, null)
                        PopupWindowsCreator.PopupType.Empty -> throw IllegalArgumentException("Invalid PopupType Provided")
                    }

                    PopupWindowsCreator.createPopup(popUpWindows, popupView, popupData) { action, data ->
                        when (action) {
                            PopupWindowsCreator.CallbackAction.Done -> vm.changeDoneStatus((data as NoteData.ToDoItem))
                            PopupWindowsCreator.CallbackAction.Update -> vm.onNavigateAction(
                                InfoForNavigationToScreenB(
                                    (data as NoteData.ToDoItem).id.toInt(),
                                    navigateToScreenB = true
                                )
                            )

                            PopupWindowsCreator.CallbackAction.Delete -> {
                                vm.delete((data as NoteData.ToDoItem).id)
                            }
                        }
                    }
                }
            }
        }
    }


    fun setUpNav() {
        viewLifecycleOwner.lifecycleScope.launch {
            vm.navigateAction.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect {
                if (it.navigateToScreenB) {
                    findNavController(rootView).navigate(MainFragmentDirections.actionMainFragmentToNoteDetailFragment())
                }
            }
        }
    }


    fun setUpYaLogin() {

        val tvLoginName: TextView = rootView.findViewById(R.id.tv_loginName)
        viewLifecycleOwner.lifecycleScope.launch {
            vm.yaLogin.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { yaLogin ->
                tvLoginName.text = yaLogin
            }
        }
    }


    fun setUpSwipeToRefresh() {
        swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh)
        swipeRefreshLayout.setOnRefreshListener {
            vm.syncNotes()
        }
    }
}





