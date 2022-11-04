package enums

enum class GuildResultType(val result: Int) {
    NewGuild(3),
    InviteGuild(5),
    LoadBBS(6), // onGuildBBSPacket
    ShowBBS(7), // onGuildBBSPacket
    GuildInfo(26),
    NameTaken(28),
    CannotFindUser(31),
    CreationFail(35),
    AgreementError(36),
    GuildFormationError(38),
    JoinGuild(39),
    InGuild(40),
    TooManyMembers(41),
    CannotFindUserChannel(42),
    LeaveGuild(44),
    UserNotInGuild(45),
    Expelled(47),
    UserNotInGuild2(48),
    Disband(50),
    DisbandingError(52),
    NotAcceptingInvites(53),
    AlreadyInvited(54),
    DeniedInvite(55),
    AdminError(56),
    IncreasingSizeError(57),
    IncreaseCapacity(58),
    LevelJobChange(60),
    MemberLogin(61),
    GuildRank(62),
    RankChange(64),
    EmblemChange(66),
    Notice(68),
    GuildPoint(72),
    ShowRank(73)
}