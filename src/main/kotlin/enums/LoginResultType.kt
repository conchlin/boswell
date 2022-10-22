package enums

enum class LoginResultType(val reason: Int) {
    Blocked(3),
    IncorrectPass(4),
    WrongID(5),
    SystemError0(6),
    AlreadyLoggedIn(7),
    SystemError1(8),
    SystemError2(9),
    TooManyConnections(10),
    AgeRequirement(11),
    MasterIP(13),
    WrongGatewayKorean(14),
    ProcessingErrorKorean(15),
    VerifyAccountEmail0(16),
    WrongGateway(17),
    VerifyAccountEmail1(21),
    LicenseAgreement(23),
    EuropeNotice(25),
    TrialVersion(27)
}