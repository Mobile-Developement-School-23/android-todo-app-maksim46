package com.example.todoapp.presentation.view


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.todoapp.R
import com.example.todoapp.ToDoAppApp
import com.example.todoapp.data.repository.LocalNoteDataRepository
import com.example.todoapp.databinding.FragmentMainBinding
import com.example.todoapp.domain.CurrentThemeStorage
import com.example.todoapp.domain.ReminderWorker.NoteNotificationReceiver
import com.example.todoapp.domain.ReminderWorker.NotificationScheduler
import com.example.todoapp.presentation.utility.SyncWM
import com.example.todoapp.presentation.utility.ViewModelFactory
import com.example.todoapp.presentation.utility.YandexLoginHandler
import com.google.android.material.snackbar.Snackbar
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthSdk
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main fragment of the application.
 */

private val NOTIFICATION_PERMISSION_REQUEST = 1

class MainFragment : Fragment(R.layout.fragment_main) {

    private val binding by viewBinding(FragmentMainBinding::bind)
    private val vm: MainFragmentViewModel by activityViewModels() { viewModelFactory }
    private val yandexLoginSdk by lazy { YandexAuthSdk(requireContext(), YandexAuthOptions(requireContext(), true)) }
    private var isPermissionRequested = true

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var mainViewController: MainViewController

    @Inject
    lateinit var noteDataRepository: LocalNoteDataRepository

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var noteNotificationReceiver: NoteNotificationReceiver

    @Inject
    lateinit var currentThemeStorage: CurrentThemeStorage

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
        applyTheme(currentThemeStorage.getTheme())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        mainViewController.setUpViews(view, viewLifecycleOwner)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requestPermission()



        binding.btSetting.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_bottomSheetFragment)
        }


        syncWM.startSynchWM()
        notificationScheduler.startReminderNotification()


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


    private fun requestPermission() {

        Log.d("PERMISSION11", "zapros")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED) -> {
                    Log.d("PERMISSION11", "granted")
                }

                (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.POST_NOTIFICATIONS)) -> {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                        Log.d(
                            "PERMISSION11", "${
                                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                            }"
                        )

                        AlertDialog.Builder(requireContext()).apply {
                            setTitle("")
                            setMessage("Возможность отправлять уведомления, позволяет напоминать Вам о важных делах ")
                            setPositiveButton("OK") { dialog, id ->
                                requestPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            }
                            setNegativeButton("Отклонить") { dialog, id ->

                            }
                        }.create().show()
                    }

                }

                else -> {

                    requestPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        }
    }


    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("PERMISSION11", "predostavl")
        } else if (isPermissionRequested) {
            isPermissionRequested = false

            requestPermission()

            Log.d("PERMISSION11", "otkloneno")
        }
    }



    fun applyTheme(themePreference: String?) {
        Log.d("THEME", "APPLY $themePreference")
        when (themePreference) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}














