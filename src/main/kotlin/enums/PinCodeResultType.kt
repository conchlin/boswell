package enums

enum class PinCodeResultType(val mode: Byte) {
    Accepted(0),
    Register(1),
    Invalid(2),
    ConnectionFail(3),
    Request(4)
}