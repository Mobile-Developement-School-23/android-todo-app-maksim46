package com.example.todoapp.presentation


import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.todoapp.R
import com.example.todoapp.data.network.SyncWork.SyncWorker
import com.example.todoapp.databinding.FragmentMainBinding
import com.example.todoapp.domain.model.InfoForNavigationToScreenB
import com.example.todoapp.domain.model.NoteData
import com.example.todoapp.presentation.utils.PopupResultListener
import com.example.todoapp.presentation.utils.PopupWindowsCreator
import com.example.todoapp.presentation.utils.applyCustom
import com.example.todoapp.presentation.utils.startAnimation
import com.google.android.material.snackbar.Snackbar
import com.yandex.authsdk.YandexAuthException
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthSdk
import com.yandex.authsdk.YandexAuthToken

import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MainFragment : Fragment(R.layout.fragment_main), PopupResultListener {

    private val binding by viewBinding(FragmentMainBinding::bind)
    private val vm: MainFragmentViewModel by activityViewModels() { viewModelFactory }
    private val adapter by lazy { MainRVAdapter() }
    private val component by lazy { (requireActivity().application as ToDoAppApp).component }
    private val yandexLoginSdk by lazy { YandexAuthSdk(requireContext(), YandexAuthOptions(requireContext(), true)) }
    var tmpErrMessage = ""

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()


        val workRequest = PeriodicWorkRequest.Builder(SyncWorker::class.java, 15, TimeUnit.MINUTES, 5, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        val workManager = WorkManager.getInstance(requireContext())
        workManager.enqueueUniquePeriodicWork(
            "SYnkWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        workManager.getWorkInfoByIdLiveData(workRequest.id).observe(viewLifecycleOwner, Observer { workInfo ->
            if (workInfo != null && workInfo.state == WorkInfo.State.FAILED) {
                Log.d("WORKM", "WORKER-fail")

            }
        })




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
            swiperefresh.setOnRefreshListener {
               vm.syncNotes()
      /*          if (!responce.status) {
                    Snackbar.make(binding.rvMain, "Нет интернета. Данные не синхронизированны", Snackbar.LENGTH_LONG).show()
                }*/
                //.applyCustom {
                //   if (this.second) {

                /*val thi = SimpleDateFormat("dd MMMM yyyy hh mm", resources.configuration.locales.get(0)).format(this.first)
                Toast.makeText(context, thi, Toast.LENGTH_SHORT).show()*/
      /*          Handler(Looper.getMainLooper()).postDelayed({
                    swiperefresh.isRefreshing = false
                }, 1000)*/
                //     }
                //   }
                //  Toast.makeText(context, "обновляется", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    swiperefresh.isRefreshing = false
                }, 2000)
            }



            btHide.setOnClickListener { vm.flipDoneVisibility() }
            itemTouchHelper.attachToRecyclerView(rvMain)



            btYALoggin.setOnClickListener {

                val loginOptionsBuilder = YandexAuthLoginOptions.Builder()
                val intent = yandexLoginSdk.createLoginIntent(loginOptionsBuilder.build())

                loginResultLauncher.launch(intent)
            }

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

            vm.getErrorMessage().flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { errMessage ->
                if ((errMessage.isNotEmpty()) && (errMessage != tmpErrMessage)) {
                    tmpErrMessage = errMessage
                    Snackbar.make(binding.rvMain, tmpErrMessage, Snackbar.LENGTH_LONG).show()
                    //   Toast.makeText(context, tmpErrMessage, Toast.LENGTH_SHORT).show()

                }
            }
        }

        /*        viewLifecycleOwner.lifecycleScope.launch {
                    var lastSuccessSync = ""
                    vm.getLastResponce().flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { lastResponce ->
                        if (lastResponce.status) {
                            lastSuccessSync = SimpleDateFormat("dd MMMM yyyy hh mm", resources.configuration.locales.get(0)).format(lastResponce.date)}

                            val message = if (lastSuccessSync.isEmpty()) {
                                "База данных не синхронизированна"
                            } else {
                                "База данных не синхронизированна. Последняя синхронизация $lastSuccessSync"
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                        }
                    }*/
        viewLifecycleOwner.lifecycleScope.launch {
            var lastSuccessSync = ""
            vm.getLastResponce().flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { lastResponce ->
                Log.d(
                    "RESPONSE",
                    "${lastResponce.status} ${
                        SimpleDateFormat(
                            " dd MMMM yyyy hh mm ",
                            resources.configuration.locales.get(0)
                        ).format(lastResponce.date)
                    }"
                )
                var message=""
                if (lastResponce.status) {
                    message = "Дела синхронизированы"
                    lastSuccessSync = SimpleDateFormat("dd MMMM yyyy hh mm", resources.configuration.locales.get(0)).format(lastResponce.date)
                } else {
                     message = if (lastSuccessSync.isEmpty()) {
                        "Дела не синхронизированы"
                    } else {
                        "Дела не синхронизированы. Версия данных от $lastSuccessSync"
                    }

                   // Snackbar.make(binding.rvMain, message, Snackbar.LENGTH_LONG).show()
                    /*            .setAction("Обновить") {
                                    vm.syncNotes()*/
                    //   } .show()
                    // Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                Snackbar.make(binding.rvMain, message, Snackbar.LENGTH_LONG).show()
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

        /*        viewLifecycleOwner.lifecycleScope.launch {
                    vm.combineNotesFlow.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { combinedList ->
                        combinedList.forEach { entity ->
                            println(entity)
                        }
                    }
                }*/


        viewLifecycleOwner.lifecycleScope.launch {
            vm.yaLogin.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { yaLogin ->
                binding.tvLoginName.text = yaLogin
                // Toast.makeText(context, "${getString(R.string.hello)}+$ yaLogin", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onPopupResult(action: PopupWindowsCreator.CallbackAction, result: NoteData) {
        when (action) {
            PopupWindowsCreator.CallbackAction.Done -> vm.changeDoneStatus((result as NoteData.ToDoItem))
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

    private val loginResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val yandexAuthToken: YandexAuthToken? = yandexLoginSdk.extractToken(result.resultCode, result.data)
            if (yandexAuthToken != null) {
                vm.yaLogin(yandexAuthToken.value)

                println(yandexAuthToken.value)
                println("SUCCESS LOG")
            }
        } catch (e: YandexAuthException) {
            println("ERROR+${e.toString()}")
        }
    }


    override fun onPause() {
        super.onPause()
        popUpWindows.dismiss()
    }
}





