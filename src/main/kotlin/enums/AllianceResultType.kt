package enums

enum class AllianceResultType(val result: Int) {
    Invite(3),
    ShowInfo(12),
    GuildInfo(13),
    LogInOut(14),
    UpdateInfo(15),
    RemoveGuild(16),
    AddGuild(18),
    JobLevelUpdate(24),
    AllianceRank(26),
    Notice(28),
    Disband(29)
}