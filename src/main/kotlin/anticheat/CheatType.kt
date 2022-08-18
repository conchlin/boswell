package anticheat

enum class CheatType {
    MOB_COUNT(1),
    GENERAL(1),
    FIX_DAMAGE(1),
    DAMAGE_HACK(15, 60 * 1000),
    DISTANCE_HACK(10, 120 * 1000),
    PORTAL_DISTANCE(1),
    PACKET_EDIT(1),
    ACC_HACK(1),
    CREATION_GENERATOR(1),
    HIGH_HP_HEALING(1),
    FAST_HP_HEALING(1),
    FAST_MP_HEALING(1),
    GACHA_EXP(1),
    TUBI(1),
    SHORT_ITEM_VAC(1),
    ITEM_VAC(1),
    MOB_VAC(1),
    FAST_ITEM_PICKUP(1),
    FAST_ATTACK(1),
    MPCON(1),
    SUMMON_DAMAGE(1),
    ILLEGAL_HP_HEALING(1),
    SKILL_PACKET_EDIT(1),
    ILLEGAL_MP_HEALING(1),
    ATTACKING_WITHOUT_TAKING_DMG(1);

    var maximum: Int
        private set

    var expire: Long
        private set

    /**
     * @param points the amount of offenses needed to trigger the system
     */
    constructor(points: Int) {
        maximum = points
        expire = -1
    }

    /**
     * @param points the amount of offenses needed to trigger the system
     * @param expire duration of time in which points will expire
     */
    constructor(points: Int = 1, expire: Long = -1) {
        maximum = points
        this.expire = expire
    }
}