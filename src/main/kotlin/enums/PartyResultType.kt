package enums

enum class PartyResultType(val result: Int) {
    // operations
    Invite(4),
    SearchInvite(4),
    SilentUpdate(7), // handles login/out status too
    Create(8),
    Disband(12),
    Leave(12),
    Expel(12),
    Join(15),
    ChangeLeader(27),
    TownPortal(35),

    // party messages
    LevelRequirement(10),
    Nonexistent(13),
    AlreadyJoined(16),
    Full(17),
    CannotFindUser(19),
    UserBlockInvite(21),
    UserHasOtherInvite(22),
    UserDenyInvite(23),
    MapCannotExpel(25),
    LeaderTransfer(28),
    LeaderTransfer2(29),
    LeaderTransferChannel(30),
}