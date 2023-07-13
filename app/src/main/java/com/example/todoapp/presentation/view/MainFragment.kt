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
import com.example.todoapp.data.repository.LocalNoteDataRepository
import com.example.todoapp.databinding.FragmentMainBinding
import com.example.todoapp.domain.ReminderWorker.NoteNotificationReceiver
import com.example.todoapp.domain.ReminderWorker.NotificationScheduler
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
    lateinit var noteDataRepository: LocalNoteDataRepository

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var noteNotificationReceiver: NoteNotificationReceiver


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
       // notificationScheduler.setNotify()
        //    setNotify()

        /*
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
        */


        /*        val service = NoteNotificationService(requireContext())
                service.showNotification(Counter.value)*/




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


    /*    private suspend fun getListForNotify(now: Long, in24Hours: Long): List<ToDoEntity> {
        val list = mutableListOf<ToDoEntity>()
        vm.getNotesForNotify(now, in24Hours)
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect {
                Log.d("NOTIF_LIST5", it.toString())
                list.addAll(it) }

        return list
    }*/


    /*    private fun getListForNotify(now: Long, in24Hours: Long): Deferred<List<ToDoEntity>> {
        return CoroutineScope(Dispatchers.IO).async {
            val list = mutableListOf<ToDoEntity>()
            vm.getNotesForNotify(now, in24Hours)
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect {
                    Log.d("NOTIF_LIST5", it.toString())
                    list.addAll(it)
                }
            return@async list
        }
    }*/


}


/*
    suspend fun getTasksForNotification(now: Long, in24Hours: Long): List<ToDoEntity> {
        val tasksForNotification = mutableListOf<ToDoEntity>()
        vm.getNotesForNotify(now, in24Hours).collect() { list ->
            tasksForNotification.addAll(list)
            Log.d("NOTIF_LIST5", list.toString())
        }
        return tasksForNotification
    }

    fun setNotify() {
        val timeOfCurrentDay = getStartAndEndOfCurrentDay()
        val selectedDate = SimpleDateFormat("dd MMMM yyyy HH mm ss", activity?.resources?.configuration?.locales?.get(0) ?: null)
        val now = timeOfCurrentDay.first
        val in24Hours = timeOfCurrentDay.second

        CoroutineScope(Dispatchers.Main).launch {
            val list = getTasksForNotification(now, in24Hours)
            val intent = Intent(context, NoteNotificationReceiver::class.java)
            val alarmManager = requireContext().getSystemService(ALARM_SERVICE) as AlarmManager

            val calendar: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 16)
                set(Calendar.MINUTE, 37)
                set(Calendar.SECOND, 0)
            }

            list.forEach { toDoEntity ->
                Log.d("NOTIF_LIST3_ENT11", "${toDoEntity.text}  ${selectedDate.format(toDoEntity.deadline)}")
                val extras = Bundle()
                extras.putString("ID", (toDoEntity.id))
                extras.putString("text", (toDoEntity.text))
                extras.putInt("Priority", (toDoEntity.priority.value))
                intent.putExtras(extras)

                val requestCode = (System.currentTimeMillis() % 100000).toInt()

                val pendingIntent = PendingIntent.getBroadcast(activity, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
*/

/*  fun setNotify() {

      val timeOfCurrentDay = getStartAndEndOfCurrentDay()
      //   val list = mutableListOf<NoteData>()
      val selectedDate =
          SimpleDateFormat("dd MMMM yyyy HH mm ss", activity?.resources?.configuration?.locales?.get(0) ?: null)
      val now = timeOfCurrentDay.first
      val in24Hours = timeOfCurrentDay.second
   *//*   val now = System.currentTimeMillis()
        val in24Hours = now + TimeUnit.HOURS.toMillis(24)*//*
        Log.d("NOTIF_старт", " ${selectedDate.format(now)}")
        Log.d("NOTIF_24", "  ${selectedDate.format(in24Hours)}")
        CoroutineScope(Dispatchers.Main).launch {

            vm.getNotesForNotify(now, in24Hours).collect() { list ->
                Log.d("NOTIF_LIST5", list.toString())


                *//*        *//**//*        viewLifecycleOwner.lifecycleScope.launch {
                    vm.getNotesForNotify(now, in24Hours).flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                        .collect { list ->*//**//*
      //  viewLifecycleOwner.lifecycleScope.launch {*//*

                //  val list=  getListForNotify(now, in24Hours)
                //  val list = getListForNotify(now, in24Hours).await()
                //Log.d("NOTIF_LIST3", list.toString())

                val intent = Intent(context, NoteNotificationReceiver::class.java)
                val alarmManager = requireContext().getSystemService(ALARM_SERVICE) as AlarmManager

                val calendar: Calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 16)  //сработает в 00
                set(Calendar.MINUTE, 13)
                set(Calendar.SECOND, 0)
                //   add(Calendar.SECOND, 5)
                }


                list.forEach { toDoEntity ->
                    Log.d("NOTIF_LIST3_ENT11", "${toDoEntity.text}  ${selectedDate.format(toDoEntity.deadline)}")
                    val extras = Bundle()
                    extras.putString("ID", (toDoEntity.id))
                    extras.putString("text", (toDoEntity.text))
                    extras.putInt("Priority", (toDoEntity.priority.value))
                    intent.putExtras(extras)


                    //   calendar.add(Calendar.SECOND, 10)

                    val requestCode = (System.currentTimeMillis() % 100000).toInt()  //чтобы в паралель

                    val pendingIntent = PendingIntent.getBroadcast(activity, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
        }
    }*/













