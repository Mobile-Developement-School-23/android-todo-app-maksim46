package com.example.todoapp.presentation.view


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.todoapp.R
import com.example.todoapp.ToDoAppApp
import com.example.todoapp.databinding.FragmentMainBinding
import com.example.todoapp.presentation.utils.ViewModelFactory
import com.example.todoapp.di.NoteFragmentComponent
import com.example.todoapp.di.NoteFragmentViewComponent
import com.example.todoapp.presentation.utils.SyncWM
import com.example.todoapp.presentation.utils.YandexLoginHandler
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthSdk
import kotlinx.coroutines.launch
import javax.inject.Inject


//class MainFragment : Fragment(R.layout.fragment_main), PopupResultListener {
class MainFragment : Fragment(R.layout.fragment_main) {
    private val binding by viewBinding(FragmentMainBinding::bind)
    private val vm: MainFragmentViewModel by activityViewModels() { viewModelFactory }
  //  private val adapter by lazy { MainRVAdapter() }
    private val component by lazy { (requireActivity().application as ToDoAppApp).component }
    private val yandexLoginSdk by lazy { YandexAuthSdk(requireContext(), YandexAuthOptions(requireContext(), true)) }


    @Inject
    lateinit var popUpWindows: PopupWindow

    @Inject
    lateinit var itemTouchHelper: ItemTouchHelper

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var fragmentViewComponent: NoteFragmentViewComponent? = null
    private lateinit var fragmentComponent: NoteFragmentComponent


    private lateinit var yandexLoginHandler: YandexLoginHandler
    private val loginResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        yandexLoginHandler.handleResult(result)
    }


    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent = NoteFragmentComponent(component, this, vm)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        fragmentViewComponent = NoteFragmentViewComponent(
            popUpWindows,
            itemTouchHelper,
            fragmentComponent,
            view,
            viewLifecycleOwner
        ).apply { mainFragmentViewController.setUpViews() }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        SyncWM(requireContext()).startSynchWM()

        yandexLoginHandler = YandexLoginHandler(loginResultLauncher, yandexLoginSdk) { token ->
            vm.yaLogin(token)
        }


        viewLifecycleOwner.lifecycleScope.launch {
            vm.yaLoginClick.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { yaLogin ->
                if (yaLogin) {
                    yandexLoginHandler.login()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        popUpWindows.dismiss()
    }

}






/*        viewLifecycleOwner.lifecycleScope.launch {
            vm.listOfNotesFlow.flowWithLifecycle(
                viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED
            ).collect { dataFromDB ->
                adapter.submit(dataFromDB)

            }
        }*/

/*   viewLifecycleOwner.lifecycleScope.launch {
       vm.numberOfDone.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { amountOfDone ->
           binding.tvSubtitle.text = getString(R.string.subhead, amountOfDone)
       }
   }

   viewLifecycleOwner.lifecycleScope.launch {
       var lastSuccessSync = ""
       vm.getLastResponce().flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { lastResponce ->
           var message = ""
           if (lastResponce.status) {
               message = getString(R.string.synch)
               lastSuccessSync = SimpleDateFormat("dd MMMM yyyy HH mm", resources.configuration.locales.get(0)).format(lastResponce.date)
               LastSuccessSync().setLastSuccessSync(lastSuccessSync.toString())
           } else {
               lastSuccessSync = LastSuccessSync().getLastSuccessSync()!!
               message = "${getString(R.string.not_synch)} $lastSuccessSync"
           }
           Snackbar.make(binding.rvMain, message, Snackbar.LENGTH_LONG).show()
           swipeRefreshLayout.isRefreshing = false
       }
   }

   viewLifecycleOwner.lifecycleScope.launch {
       vm.getErrorMessage().flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { onErrMessage ->
           if (onErrMessage.isNotEmpty()) {
               Snackbar.make(binding.rvMain, tmpErrMessage, Snackbar.LENGTH_SHORT).show()
           }
       }
   }

   viewLifecycleOwner.lifecycleScope.launch {
       vm.corErrorMessage.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { corErrorMessage ->
           if (!corErrorMessage.status) {
               Snackbar.make(binding.rvMain, getString(R.string.unexpected_error), Snackbar.LENGTH_SHORT).show()
           }
       }
   }

   viewLifecycleOwner.lifecycleScope.launch {
       vm.isDoneVisible.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { isVisible ->
           when (isVisible) {
               true -> binding.btHide.setImageResource(R.drawable.ic_eye_off)
               false -> binding.btHide.setImageResource(R.drawable.ic_eye)

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

   viewLifecycleOwner.lifecycleScope.launch {
       vm.yaLogin.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { yaLogin ->
           binding.tvLoginName.text = yaLogin
       }
   }
}*/


/*    override fun onPopupResult(action: PopupWindowsCreator.CallbackAction, result: NoteData) {
        when (action) {
            PopupWindowsCreator.CallbackAction.Done -> vm.changeDoneStatus((result as NoteData.ToDoItem))
            PopupWindowsCreator.CallbackAction.Update -> vm.onNavigateAction( InfoForNavigationToScreenB( (result as NoteData.ToDoItem).id.toInt(),  navigateToScreenB = true ) )
            PopupWindowsCreator.CallbackAction.Delete -> { vm.delete((result as NoteData.ToDoItem).id) }
        }
    }*/


/*     private val loginResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val yandexAuthToken: YandexAuthToken? = yandexLoginSdk.extractToken(result.resultCode, result.data)
            if (yandexAuthToken != null) {
                vm.yaLogin(yandexAuthToken.value)
            }
        } catch (e: YandexAuthException) {
            println("ERROR+${e.toString()}")
        }
    }*/









