package enums

enum class ShopResultType(val result: Int) {
    Success(0),
    InsufficientMesos(2),
    InventoryFull(3),
    NotInStock(5),
    Recharge(8)
}