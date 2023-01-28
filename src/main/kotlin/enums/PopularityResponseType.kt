package enums

enum class PopularityResponseType(val value: Int) {
    GiveSuccess(0),
    UserNotExist(1),
    UnderLevel15(2),
    AlreadyUsedDay(3),
    AlreadyUsedMonth(4),
    ReceiveSuccess(5),
    UnexpectedError(6)
}