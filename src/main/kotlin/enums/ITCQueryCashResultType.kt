package enums
enum class ITCQueryCashResultType (val type: Int) {
    ConfirmSell(29),
    TransferInventory(33),
    NotYetSold(35),
    ConfirmTransfer(39),
    ConfirmBuy(51),
    FailBuy(52),
    WantList(61)
}