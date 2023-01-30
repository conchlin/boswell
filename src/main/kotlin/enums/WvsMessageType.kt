package enums

enum class WvsMessageType(val type: Int) {
    Silent(0),
    Quest(1),
    Expiration(2),
    ExpGain(3),
    Popularity(4),
    MesosGain(5),
    GuildPoint(6),
    Item(7),
    // 8 ???
    InfoText(9),
    Area(10),
}