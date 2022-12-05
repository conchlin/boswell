package enums

enum class TrunkResultType(val result: Byte) {
    // error messages
    FullInventory(10),
    InsufficientMesos(11),
    OneOfAKind(12),
    FullTrunk(17),
    //trunk actions
    RetrieveItem(9),
    DepositItem(13),
    Arrange(15),
    MesoAction(19),
    OpenStorage(22)
}