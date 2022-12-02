package enums

enum class WhisperResultType (val result: Int) {
    WhisperReply(10),
    WhisperSend(12),
    FindReply(9),
    FindBuddy(72) // and there goes the nice sequential order :(
}