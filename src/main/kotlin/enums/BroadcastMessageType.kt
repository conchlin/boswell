package enums

enum class BroadcastMessageType (val type: Int) {
    Notice(0),
    Popup(1),
    Megaphone(2),
    SuperMegaphone(3),
    Banner(4), // whatever that top scrolling message is called
    PinkText(5),
    BlueText(6),
    BroadcastNPC(7),
    ItemMegaphone(8),
    MultiMegaphone(9),
    Gachapon(11)
}