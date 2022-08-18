/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client;

public enum MapleBuffStat {
    WATK(0),
    WDEF(1),
    MATK(2),
    MDEF(3),
    ACC(4),
    AVOID(5),
    HANDS(6),
    SPEED(7),
    JUMP(8),
    MAGIC_GUARD(9),
    DARKSIGHT(10),
    BOOSTER(11),
    POWERGUARD(12),
    HYPERBODYHP(13),
    HYPERBODYMP(14),
    INVINCIBLE(15),
    SOULARROW(16),
    STUN(17, true),
    POISON(18, true),
    SEAL(19, true),
    DARKNESS(20, true),
    COMBO(21),
    WK_CHARGE(22),
    DRAGONBLOOD(23),
    HOLY_SYMBOL(24),
    MESOUP(25),
    SHADOWPARTNER(26),
    PICKPOCKET(27),
    MESOGUARD(28),
    THAW(29),
    WEAKEN(30, true),
    CURSE(31, true),
    SLOW(32),
    MORPH(33),
    RECOVERY(34),
    MAPLE_WARRIOR(35),
    STANCE(36),
    SHARP_EYES(37),
    MANA_REFLECTION(38),
    SEDUCE(39, true),
    SHADOW_CLAW(40),
    INFINITY(41),
    HOLY_SHIELD(42),
    HAMSTRING(43),
    BLIND(44),
    CONCENTRATE(45),
    BAN_MAP(46),
    ECHO_OF_HERO(47),
    MESO_UP_BY_ITEM(48),
    GHOST_MORPH(49),
    BARRIER(50),
    CONFUSE(51, true),
    ITEM_UP_BY_ITEM(52),
    RESPECT_PIMMUNE(53),
    RESPECT_MIMMUNE(54),
    DEFENSE_ATT(55),
    DEFENSE_STATE(56),
    INC_EFFECT_HP_POTION(57),
    INC_EFFECT_MP_POTION(58),
    BERSERK_FURY(59),//Dojang_Berserk
    DIVINE_BODY(60),//Dojang_Invincible
    SPARK(61),
    DOJANG_SHIELD(62),
    SOUL_MASTER_FINAL(63),
    WIND_BREAKER_FINAL(64),
    ELEMENTAL_RESET(65),
    WIND_WALK(66),
    EVENT_RATE(67),
    ARAN_COMBO(68),
    COMBO_DRAIN(69),
    COMBO_BARRIER(70),
    BODY_PRESSURE(71),
    SMART_KNOCKBACK(72),
    REPEAT_EFFECT(73),
    EXP_BUFF_RATE(74),
    STOP_POTION(75),
    STOP_MOTION(76),
    FEAR(77),
    EVAN_SLOW(78),
    MAGIC_SHIELD(79),
    MAGIC_RESIST(80),
    SOUL_STONE(81),
    ENERGY_CHARGE(82),
    DASH_SPEED(83), // correct (speed)
    DASH_JUMP(84), // correct (jump)
    MONSTER_RIDING(85),
    SPEED_INFUSION(86),
    HOMING_BEACON(87),
    ZOMBIFY(88, true),
    SUMMON(98),
    PUPPET(99);

    private final int mask;
    private final byte set;
    private final boolean disease;

    private MapleBuffStat(int shift) {
        this(shift, false);
    }

    private MapleBuffStat(int shift, boolean isDisease) {
        if (shift == 98 || shift == 99) {
            long stat = ((shift >> 32) & 0xffffffffL);
            if (stat == 0) {
                stat = (shift & 0xffffffffL);
            }

            this.mask = (int) stat;
        } else {
            this.mask = 1 << (shift % 32);
        }
        this.set = (byte) Math.floor(shift / 32);
        this.disease = isDisease;
    }

    public int getMask() {
        return mask;
    }

    public boolean isDisease() {
        return disease;
    }

    public byte getSet() {
        return set;
    }
}
