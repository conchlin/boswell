package enums

enum class UIType(val value: Byte) {

    // TODO decimals based completely on list below. test to see if these are right
    /**
     * Sends a UI utility.
     * 0x01 - Equipment Inventory.
     * 0x02 - Stat Window.
     * 0x03 - Skill Window.
     * 0x05 - Keyboard Settings.
     * 0x06 - Quest window.
     * 0x09 - Monsterbook Window.
     * 0x0A - Char Info
     * 0x0B - Guild BBS
     * 0x12 - Monster Carnival Window
     * 0x16 - Party Search.
     * 0x17 - Item Creation Window.
     * 0x1A - My Ranking O.O
     * 0x1B - Family Window
     * 0x1C - Family Pedigree
     * 0x1D - GM Story Board /funny shet
     * 0x1E - Envelop saying you got mail from an admin.
     * 0x1F - Medal Window
     * 0x20 - Maple Event (???)
     * 0x21 - Invalid Pointer Crash
     */

    EQUIPMENT(1),
    STAT(2),
    SKILL(3),
    MINI_MAP(4),
    KEY_CONFIG(5),
    QUEST(6),
    MONSTER_BOOK(9),
    USER_INFO(10),
    GUILD_BBS(11),
    MONSTER_CARNIVAL(18),
    PARTY_SEARCH(22),
    MAKER(23),
    RANKING(26),
    FAMILY(27),
    FAMILY_TREE(28),
    STORY_BOARD(29),
    ADMIN_MAIL(30),
    MEDAL(31)

}