package enums

enum class ShopLinkResultType(val type: Int) {
    Success(0),
    Closed(1),
    FullCapacity(2),
    TooManyRequests(3),
    Dead(4),
    RestrictedTrade(7),
    CannotEnter(17),
    StoreMaintenance(18),
    WrongLocation(23)
}