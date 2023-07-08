package com.example.todoapp.presentation.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.todoapp.R
import com.example.todoapp.ToDoAppApp
import com.example.todoapp.databinding.FragmentMainBinding
import com.example.todoapp.presentation.utility.SyncWM
import com.example.todoapp.presentation.utility.ViewModelFactory
import com.example.todoapp.presentation.utility.YandexLoginHandler
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthSdk
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main fragment of the application.
 */
class MainFragment : Fragment(R.layout.fragment_main) {

    private val binding by viewBinding(FragmentMainBinding::bind)
    private val vm: MainFragmentViewModel by activityViewModels() { viewModelFactory }
    private val yandexLoginSdk by lazy { YandexAuthSdk(requireContext(), YandexAuthOptions(requireContext(), true)) }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var mainViewController: MainViewController

    @Inject
    lateinit var syncWM: SyncWM

    private val component by lazy {
        (requireActivity().application as ToDoAppApp).component.fragmentMainComponent()
            .create(this, this.requireActivity())
    }
    lateinit var yandexLoginHandler: YandexLoginHandler
    private val loginResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        yandexLoginHandler.handleResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        mainViewController.setUpViews(view, viewLifecycleOwner)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        syncWM.startSynchWM()

        yandexLoginHandler = YandexLoginHandler(loginResultLauncher, yandexLoginSdk) { token ->
            vm.yaLogin(token)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.yaLoginClick.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { yaLogin ->
                if (yaLogin) {
                    yandexLoginHandler.login()
                }
            }
        }
    }
}








