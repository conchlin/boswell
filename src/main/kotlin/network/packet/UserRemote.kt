package network.packet

import client.BuffValueHolder
import client.MapleBuffStat
import client.MapleCharacter
import client.MapleMount
import constants.skills.Buccaneer
import constants.skills.DarkKnight
import constants.skills.ThunderBreaker
import enums.UserEffectType
import net.server.guild.MapleGuild
import network.opcode.SendOpcode
import tools.Pair
import tools.data.output.LittleEndianWriter
import tools.data.output.MaplePacketLittleEndianWriter
import java.awt.Point
import java.util.*

class UserRemote {

    companion object Packet {

        // smh some of this kotlin default formatting is ugly af

        fun closeRangeAttack(
            chr: MapleCharacter?,
            skill: Int,
            skilllevel: Int,
            stance: Int,
            numAttackedAndDamage: Int,
            damage: Map<Int?, List<Int?>?>?,
            speed: Int,
            direction: Int,
            display: Int
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CLOSE_RANGE_ATTACK.value)
            addAttackBody(
                mplew,
                chr!!,
                skill,
                skilllevel,
                stance,
                numAttackedAndDamage,
                0,
                damage,
                speed,
                direction,
                display
            )
            return mplew.packet
        }

        fun rangedAttack(
            chr: MapleCharacter?,
            skill: Int,
            skilllevel: Int,
            stance: Int,
            numAttackedAndDamage: Int,
            projectile: Int,
            damage: Map<Int?, List<Int?>?>?,
            speed: Int,
            direction: Int,
            display: Int
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.RANGED_ATTACK.value)
            addAttackBody(
                mplew,
                chr!!, skill, skilllevel, stance, numAttackedAndDamage, projectile, damage, speed, direction, display
            )
            mplew.writeInt(0)
            return mplew.packet
        }

        fun magicAttack(
            chr: MapleCharacter?,
            skill: Int,
            skilllevel: Int,
            stance: Int,
            numAttackedAndDamage: Int,
            damage: Map<Int?, List<Int?>?>?,
            charge: Int,
            speed: Int,
            direction: Int,
            display: Int
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MAGIC_ATTACK.value)
            addAttackBody(
                mplew,
                chr!!,
                skill,
                skilllevel,
                stance,
                numAttackedAndDamage,
                0,
                damage,
                speed,
                direction,
                display
            )
            if (charge != -1) {
                mplew.writeInt(charge)
            }
            return mplew.packet
        }

        private fun addAttackBody(
            lew: LittleEndianWriter,
            chr: MapleCharacter,
            skill: Int,
            skilllevel: Int,
            stance: Int,
            numAttackedAndDamage: Int,
            projectile: Int,
            damage: Map<Int?, List<Int?>?>?,
            speed: Int,
            direction: Int,
            display: Int
        ) {
            lew.writeInt(chr.id)
            lew.write(numAttackedAndDamage)
            lew.write(0x5B) //?
            lew.write(skilllevel)
            if (skilllevel > 0) {
                lew.writeInt(skill)
            }
            lew.write(display)
            lew.write(direction)
            lew.write(stance)
            lew.write(speed)
            lew.write(0x0A)
            lew.writeInt(projectile)
            if (damage != null) {
                for (oned in damage.keys) {
                    val onedList = damage[oned]
                    if (onedList != null) {
                        if (oned != null) {
                            lew.writeInt(oned)
                        }
                        lew.write(0x0)
                        if (skill == 4211006) {
                            lew.write(onedList.size)
                        }
                        for (eachd in onedList) {
                            if (eachd != null) {
                                lew.writeInt(eachd)
                            }
                        }
                    }
                }
            }
        }


        fun skillEffect(
            from: MapleCharacter,
            skillId: Int,
            level: Int,
            flags: Byte,
            speed: Int,
            direction: Byte
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SKILL_EFFECT.value)
            mplew.writeInt(from.id)
            mplew.writeInt(skillId)
            mplew.write(level)
            mplew.write(flags)
            mplew.write(speed)
            mplew.write(direction) //Mmmk

            return mplew.packet
        }

        fun skillCancel(from: MapleCharacter, skillId: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CANCEL_SKILL_EFFECT.value)
            mplew.writeInt(from.id)
            mplew.writeInt(skillId)

            return mplew.packet
        }

        fun damagePlayer(
            skill: Int,
            monsteridfrom: Int,
            cid: Int,
            damage: Int,
            fake: Int,
            direction: Int,
            pgmr: Boolean,
            pgmr_1: Int,
            is_pg: Boolean,
            oid: Int,
            pos_x: Int,
            pos_y: Int
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.DAMAGE_PLAYER.value)
            mplew.writeInt(cid)
            mplew.write(skill)
            mplew.writeInt(damage)
            if (skill != -4) {
                mplew.writeInt(monsteridfrom)
                mplew.write(direction)
                if (pgmr) {
                    mplew.write(pgmr_1)
                    mplew.write(if (is_pg) 1 else 0)
                    mplew.writeInt(oid)
                    mplew.write(6)
                    mplew.writeShort(pos_x)
                    mplew.writeShort(pos_y)
                    mplew.write(0)
                } else {
                    mplew.writeShort(0)
                }
                mplew.writeInt(damage)
                if (fake > 0) {
                    mplew.writeInt(fake)
                }
            } else {
                mplew.writeInt(damage)
            }
            return mplew.packet
        }

        fun facialExpression(from: MapleCharacter, expression: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(10)
            mplew.writeShort(SendOpcode.FACIAL_EXPRESSION.value)
            mplew.writeInt(from.id)
            mplew.writeInt(expression)

            return mplew.packet
        }

        fun itemEffect(characterid: Int, itemid: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SHOW_ITEM_EFFECT.value)
            mplew.writeInt(characterid)
            mplew.writeInt(itemid)

            return mplew.packet
        }

        fun showChair(characterid: Int, itemid: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SHOW_CHAIR.value)
            mplew.writeInt(characterid)
            mplew.writeInt(itemid)

            return mplew.packet
        }

        fun onRemoteUserEffect(charId: Int, effect: Byte, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.value)
            mplew.writeInt(charId)
            mplew.write(effect)
            when (effect) {
                UserEffectType.RECOVERY.effect,
                UserEffectType.MAKER.effect -> {
                    mplew.write(args[0])
                }
                UserEffectType.PET_LEVEL_UP.effect -> {
                    mplew.write(0)
                    mplew.write(args[0])
                }
                UserEffectType.BUYBACK.effect -> {
                    mplew.writeInt(0)
                }
            }

            return mplew.packet
        }

        fun onRemoteUserEffect(charId: Int, effect: Byte, path: String): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.value)
            mplew.writeInt(charId)
            mplew.write(effect)
            when (effect) {
                UserEffectType.SHOW_INFO.effect -> {
                    mplew.writeMapleAsciiString(path)
                    mplew.writeInt(1)
                }
            }

            return mplew.packet
        }

        /** todo add UPDATE_CHAR_LOOK **/

        fun showBuffEffect(cid: Int, skillid: Int, effectid: Int): ByteArray? {
            return showBuffEffect(cid, skillid, effectid, 3.toByte())
        }

        fun showBuffEffect(cid: Int, skillid: Int, effectid: Int, direction: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.value)
            mplew.writeInt(cid)
            mplew.write(effectid) //buff level
            mplew.writeInt(skillid)
            mplew.write(direction)
            mplew.write(1)
            mplew.writeLong(0)

            return mplew.packet
        }

        fun showBuffEffect(
            cid: Int,
            skillid: Int,
            skilllv: Int,
            effectid: Int,
            direction: Byte
        ): ByteArray? {   // updated packet structure found thanks to Rien dev team
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.value)
            mplew.writeInt(cid)
            mplew.write(effectid)
            mplew.writeInt(skillid)
            mplew.write(0)
            mplew.write(skilllv)
            mplew.write(direction)

            return mplew.packet
        }

        fun showBerserk(cid: Int, skilllevel: Int, Berserk: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.value)
            mplew.writeInt(cid)
            mplew.write(1)
            mplew.writeInt(DarkKnight.BERSERK)
            mplew.write(0xA9)
            mplew.write(skilllevel)
            mplew.write(if (Berserk) 1 else 0)

            return mplew.packet
        }

        fun showForeignEffect(effect: Int): ByteArray? {
            return showForeignEffect(-1, effect)
        }

        fun showForeignEffect(cid: Int, effect: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.value)
            mplew.writeInt(cid)
            mplew.write(effect)

            return mplew.packet
        }

        fun showMonsterRiding(cid: Int, mount: MapleMount): ByteArray? { //Gtfo with this, this is just giveForeignBuff
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.value)
            mplew.writeInt(cid)
            val temp: MutableList<MapleBuffStat> = ArrayList()
            temp.add(MapleBuffStat.MONSTER_RIDING)
            writeLongMaskFromList(mplew, temp)
            mplew.writeShort(0)
            mplew.writeInt(mount.itemId)
            mplew.writeInt(mount.skillId)
            mplew.writeInt(0) //Server Tick value.
            mplew.writeShort(0)
            mplew.write(0) //Times you have been buffed

            return mplew.packet
        }

        fun giveForeignBuff(cid: Int, statups: List<Pair<MapleBuffStat?, BuffValueHolder>>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.value)
            mplew.writeInt(cid)
            writeLongMask(mplew, statups)
            for (statup in statups) {
                if (statup.getLeft()?.isDisease == true) {
                    if (statup.getLeft() == MapleBuffStat.POISON) {
                        mplew.writeShort(statup.getRight().value)
                    }
                    mplew.writeShort(statup.getRight().sourceID)
                    mplew.writeShort(statup.getRight().sourceLevel)
                } else {
                    mplew.writeInt(statup.getRight().value)
                }
            }
            mplew.writeShort(0)
            mplew.writeShort(0)

            return mplew.packet
        }
        fun giveForeignChairSkillEffect(cid: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.value)
            mplew.writeInt(cid)
            writeLongMaskChair(mplew)
            mplew.writeShort(0)
            mplew.writeShort(0)
            mplew.writeShort(100)
            mplew.writeShort(1)
            mplew.writeShort(0)
            mplew.writeShort(900)
            mplew.skip(7)

            return mplew.packet
        }

        fun giveForeignPirateBuff(
            cid: Int,
            buffid: Int,
            time: Int,
            statups: List<Pair<MapleBuffStat?, BuffValueHolder>>
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            val infusion = buffid == Buccaneer.SPEED_INFUSION || buffid == ThunderBreaker.SPEED_INFUSION
            mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.value)
            mplew.writeInt(cid)
            writeLongMask(mplew, statups)
            mplew.writeShort(0)
            for (statup in statups) {
                mplew.writeInt(statup.getRight().value)
                mplew.writeInt(buffid)
                mplew.skip(if (infusion) 10 else 5)
                mplew.writeShort(time)
            }
            mplew.writeShort(0)
            mplew.write(2)

            return mplew.packet
        }

        fun cancelForeignDebuff(cid: Int, mask: Long): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.value)
            mplew.writeInt(cid)
            mplew.writeLong(0)
            mplew.writeLong(mask)

            return mplew.packet
        }

        fun cancelForeignBuff(cid: Int, statups: List<MapleBuffStat>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.value)
            mplew.writeInt(cid)
            writeLongMaskFromList(mplew, statups)

            return mplew.packet
        }

        fun cancelForeignChairSkillEffect(cid: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(19)
            mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.value)
            mplew.writeInt(cid)
            writeLongMaskChair(mplew)

            return mplew.packet
        }

        fun updatePartyMemberHP(cid: Int, curhp: Int, maxhp: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.UPDATE_PARTYMEMBER_HP.value)
            mplew.writeInt(cid)
            mplew.writeInt(curhp)
            mplew.writeInt(maxhp)

            return mplew.packet
        }

        /**
         * Guild Name & Mark update packet, thanks to Arnah (Vertisy)
         *
         * @param guildName The Guild name, blank for nothing.
         */
        fun guildNameChanged(chrid: Int, guildName: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GUILD_NAME_CHANGED.value)
            mplew.writeInt(chrid)
            mplew.writeMapleAsciiString(guildName)

            return mplew.packet
        }

        fun guildMarkChanged(chrid: Int, guild: MapleGuild): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GUILD_MARK_CHANGED.value)
            mplew.writeInt(chrid)
            mplew.writeShort(guild.logoBG)
            mplew.write(guild.logoBGColor)
            mplew.writeShort(guild.logo)
            mplew.write(guild.logoColor)

            return mplew.packet
        }

        fun throwGrenade(
            cid: Int,
            p: Point,
            keyDown: Int,
            skillId: Int,
            skillLevel: Int
        ): ByteArray? { // packets found thanks to GabrielSin
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.THROW_GRENADE.value)
            mplew.writeInt(cid)
            mplew.writeInt(p.x)
            mplew.writeInt(p.y)
            mplew.writeInt(keyDown)
            mplew.writeInt(skillId)
            mplew.writeInt(skillLevel)

            return mplew.packet
        }

        private fun writeLongMaskFromList(mplew: MaplePacketLittleEndianWriter, statups: List<MapleBuffStat>) {
            val mask = IntArray(4)
            for (statup in statups) {
                mask[statup.set.toInt()] = mask[statup.set.toInt()] or statup.mask
            }
            for (i in 3 downTo 0) { // for (int i = 3; i >= 0; i--) {
                mplew.writeInt(mask[i])
            }
        }

        private fun writeLongMaskChair(mplew: MaplePacketLittleEndianWriter) {
            mplew.writeInt(0)
            mplew.writeInt(262144)
            mplew.writeLong(0)
        }

        private fun writeLongMask(
            mplew: MaplePacketLittleEndianWriter,
            statups: List<Pair<MapleBuffStat?, BuffValueHolder>>
        ) {
            val mask = IntArray(4)
            for (statup in statups) {
                mask[statup.left?.set?.toInt()!!] = mask[statup.left!!.set.toInt()] or statup.left!!.mask
            }
            for (i in 3 downTo 0) {
                mplew.writeInt(mask[i])
            }
        }
    }
}