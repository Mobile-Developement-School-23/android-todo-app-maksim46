Привет
Все изменения в пулл реквесте

Для работы авторизации через яндекс, нужно чтобы на устройстве было установлено любое приложения яндекса. В эмулятор apk можно установить просто drag-and-drop-нув apk в него.

и нашел опечатку в коде. Чтобы удаление из офлайна нормально работало в class LocalNoteDataRepositoryImpl 
verride fun getToDoNoteListForSynk(doneStatus: Boolean): Flow<List<ToDoEntity>> {
        val toDoNoteList = myToDoListDao.getAllToDoList(doneStatus)  нужно заменить на >>>>>  val toDoNoteList = myToDoListDao.getAllToDoListForSynk(doneStatus)

дедлайн прошел поэтому комиты не кидаю

ну и напомнить хочу что архитектура и Di это следующее ДЗ, то что сейчас у меня - это не конечная версия
