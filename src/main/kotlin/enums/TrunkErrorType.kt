package enums

enum class TrunkErrorType(val error: Byte) {
    FullInventory(10),
    InsufficientMesos(11),
    OneOfAKind(12),
    FullTrunk(17)
}