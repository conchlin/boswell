package enums

enum class UserEffectType(val effect : Int) {
    /**
     * 0 = Levelup
     * 6 = Exp did not drop (Safety Charms)
     * 7 = Enter portal sound
     * 8 = Job change
     * 9 = Quest complete
     * 10 = Recovery
     * 11 = Buff effect
     * 14 = Monster book pickup
     * 15 = Equipment levelup
     * 16 = Maker Skill Success
     * 17 = Buff effect w/ sfx
     * 19 = Exp card [500, 200, 50]
     * 21 = Wheel of destiny
     * 26 = Spirit Stone
     *
     * @param effect
     * @return
     */

    LEVEL_UP(1),
    PORTAL_SE(7),
    JOB_CHANGE(8),
    QUEST_COMPLETE(9),
    RECOVERY(10),
    BUFF_EFFECT(11),
    MONSTERBOOK(14),
    EQUIP_LEVEL_UP(15),
    MAKER(16),
    BUFF_EFFECT_SFX(17),
    EXP_CARD(19),
    WHEEL_DESTINY(21),
    SPRIRIT_STONE(26)
}