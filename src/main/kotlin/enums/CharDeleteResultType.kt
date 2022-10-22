package enums

enum class CharDeleteResultType(val state: Int) {
    DeleteOk(0),
    CreationError(9),
    InvalidBDay(12),
    IncorrectPic(14)
}