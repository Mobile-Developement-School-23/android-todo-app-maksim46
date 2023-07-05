
UPD 05.07 случайно кинул комит в старую ветку

UPD сейчас нашел ошибку, при первой установке приложения происходи крш, т.к. null в дефолте стоит у shared preff в class LastSuccessSync нужно поменять
 fun getLastSuccessSync(): String? { val sharedPrefs = context?.getSharedPreferences(SP_FILENAME, Context.MODE_PRIVATE)
 return sharedPrefs?.getString(LAST_SYNC_TIME_KEY, null) }  >>>>>>>>>>>>>>>>>  return sharedPrefs?.getString(LAST_SYNC_TIME_KEY, " ")

телеграм @maxim_rolich

Привет Все изменения в пулл реквесте (века DZ-3)

Для работы авторизации через яндекс, нужно чтобы на устройстве было установлено любое приложения яндекса. В эмулятор apk можно установить просто drag-and-drop-нув apk в него.

и нашел опечатку в коде. Чтобы удаление из офлайна нормально работало в class LocalNoteDataRepositoryImpl 
verride fun getToDoNoteListForSynk(doneStatus: Boolean): Flow<List>
{ val toDoNoteList = myToDoListDao.getAllToDoList(doneStatus) нужно заменить на >>>>> val toDoNoteList = myToDoListDao.getAllToDoListForSynk(doneStatus)

дедлайн прошел поэтому комиты не кидаю

ну и напомнить хочу что архитектура и Di это следующее ДЗ, то что сейчас у меня - это не конечная версия
