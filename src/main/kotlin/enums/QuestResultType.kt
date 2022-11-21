package enums

enum class QuestResultType (val result: Int) {
    AddTime(6),
    RemoveTime(7),
    UpdateInfo(8),
    UpdateNext(8),
    Expire(15),
}