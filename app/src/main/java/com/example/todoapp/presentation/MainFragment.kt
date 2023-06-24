package com.example.todoapp.presentation


import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentMainBinding
import com.example.todoapp.domain.model.InfoForNavigationToScreenB
import com.example.todoapp.domain.model.NoteData
import com.example.todoapp.presentation.utils.PopupResultListener
import com.example.todoapp.presentation.utils.PopupWindowsCreator
import com.example.todoapp.presentation.utils.startAnimation
import kotlinx.coroutines.launch
import javax.inject.Inject


class MainFragment : Fragment(R.layout.fragment_main), PopupResultListener {

    private val binding by viewBinding(FragmentMainBinding::bind)
    private val vm: MainFragmentViewModel by activityViewModels() { viewModelFactory }
    private val adapter by lazy { MainRVAdapter() }
    private val component by lazy { (requireActivity().application as ToDoAppApp).component }

    @Inject
    lateinit var popUpWindows: PopupWindow

    @Inject
    lateinit var itemTouchHelper: ItemTouchHelper

    @Inject
    lateinit var viewModelFactory: ViewModelFactory


    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        with(binding) {
            rvMain.layoutManager = LinearLayoutManager(requireActivity())
            rvMain.adapter = adapter

            fab.setOnClickListener {
                fab.visibility = View.INVISIBLE
                animCircle.visibility = View.VISIBLE

                animCircle.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fab_anim).apply {
                    interpolator = AccelerateDecelerateInterpolator()
                }) {
                    vm.onNavigateAction(InfoForNavigationToScreenB(navigateToScreenB = true))
                    binding.animCircle.visibility = View.INVISIBLE
                }
            }


            btHide.setOnClickListener { vm.flipDoneVisibility() }
            itemTouchHelper.attachToRecyclerView(rvMain)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.listOfNotesFlow.flowWithLifecycle(
                viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED
            ).collect { dataFromDB ->
                adapter.submit(dataFromDB)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.numberOfDone.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { amountOfDone ->
                binding.tvSubtitle.text = getString(R.string.subhead, amountOfDone)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.isDoneVisible.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { isVisible ->
                when (isVisible) {
                    true -> binding.btHide.setImageResource(R.drawable.ic_eye_off)
                    false -> binding.btHide.setImageResource(R.drawable.ic_eye)
                      //tmp
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.popupAction.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { popupData ->
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    val popupView = when (popupData.popupType) {
                        PopupWindowsCreator.PopupType.Info -> layoutInflater.inflate(R.layout.popup_info, null)
                        PopupWindowsCreator.PopupType.Action -> layoutInflater.inflate(R.layout.popup_note_action, null)
                        PopupWindowsCreator.PopupType.Empty -> throw IllegalArgumentException("Invalid PopupType Provided")
                    }
                    PopupWindowsCreator.createPopup(popUpWindows, popupView, popupData, this@MainFragment)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.navigateAction.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect {
                if (it.navigateToScreenB) {
                    findNavController().navigate(MainFragmentDirections.actionMainFragmentToNoteDetailFragment())
                }
            }
        }
    }


    override fun onPopupResult(action: PopupWindowsCreator.CallbackAction, result: NoteData) {
        when (action) {
            PopupWindowsCreator.CallbackAction.Done -> vm.changeDoneStatus((result as NoteData.ToDoItem).id)
            PopupWindowsCreator.CallbackAction.Update -> vm.onNavigateAction(
                InfoForNavigationToScreenB(
                    (result as NoteData.ToDoItem).id.toInt(),
                    navigateToScreenB = true
                )
            )
            PopupWindowsCreator.CallbackAction.Delete -> {
                vm.delete((result as NoteData.ToDoItem).id)
            }
        }
    }


    override fun onPause() {
        super.onPause()
        popUpWindows.dismiss()
    }
}


