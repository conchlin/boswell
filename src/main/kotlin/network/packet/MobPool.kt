package network.packet

import client.status.MonsterStatus
import client.status.MonsterStatusEffect
import network.opcode.SendOpcode
import server.life.MapleMonster
import server.movement.LifeMovementFragment
import tools.MaplePacketCreator
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil
import java.awt.Point

class MobPool {

    companion object Packet {

        fun onMove(
            useSkill: Int,
            action: Int,
            delay: Int,
            skillInfo: Int,
            oid: Int,
            pos: Point?,
            moves: List<LifeMovementFragment?>?
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MobMove.value)
            mplew.writeInt(oid)
            mplew.write(useSkill)
            mplew.write(delay)
            mplew.write(action)
            mplew.writeInt(skillInfo)
            mplew.writePos(pos)
            PacketUtil.serializeMovementList(mplew, moves)

            return mplew.packet
        }

        /**
         * Gets a response to a move monster packet.
         *
         * @param objectid The ObjectID of the monster being moved.
         * @param moveid The movement ID.
         * @param currentMp The current MP of the monster.
         * @param useSkills Can the monster use skills?
         * @return The move response packet.
         */
        fun moveMonsterResponse(objectid: Int, moveid: Short, currentMp: Int, useSkills: Boolean): ByteArray? {
            return moveMonsterResponse(objectid, moveid, currentMp, useSkills, 0, 0)
        }

        /**
         * Gets a response to a move monster packet.
         *
         * @param objectid The ObjectID of the monster being moved.
         * @param moveid The movement ID.
         * @param currentMp The current MP of the monster.
         * @param useSkills Can the monster use skills?
         * @param skillId The skill ID for the monster to use.
         * @param skillLevel The level of the skill to use.
         * @return The move response packet.
         */
        fun moveMonsterResponse(
            objectid: Int,
            moveid: Short,
            currentMp: Int,
            useSkills: Boolean,
            skillId: Int,
            skillLevel: Int
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(13)
            mplew.writeShort(SendOpcode.CtrlAck.value)
            mplew.writeInt(objectid)
            mplew.writeShort(moveid.toInt())
            mplew.writeBool(useSkills)
            mplew.writeShort(currentMp)
            mplew.write(skillId)
            mplew.write(skillLevel)

            return mplew.packet
        }

        fun onStatSet(oid: Int, mse: MonsterStatusEffect): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MobStatSet.value)
            mplew.writeInt(oid)
            MaplePacketCreator.mobStat(mplew, mse)
            val skill = if (mse.playerSkill != null) mse.playerSkill else mse.mobSkill
            mplew.writeShort(skill.delay) //tDelay
            mplew.write(0) //v3->m_nCalcDamageStatIndex
            if (mse.isMovementAffectingSkill) {
                mplew.write(0)
            }

            return mplew.packet
        }

        fun onStatReset(oid: Int, stats: Map<MonsterStatus?, Int?>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MobStatReset.value)
            mplew.writeInt(oid)
            MaplePacketCreator.writeMonsterStatMask(mplew, stats)
            mplew.write(0) //m_nCalcDamageStatIndex
            if (MaplePacketCreator.isMovementAffectingSkill(stats)) {
                mplew.write(0) //v5 = CVecCtrlUser::AddMovementInfo(v3->m_pvc.p, &v3->m_secondaryStat, &v3->m_character, 0);
            }

            return mplew.packet
        }

        /**
         * This packet sends the data of all the monsters that were affected by a
         * mob skill
         */
        fun onAffected(oid: Int, skillId: Int, delay: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MobAffected.value)
            mplew.writeInt(oid)
            mplew.writeInt(skillId) //nSkillId
            mplew.writeShort(delay) //tDelay

            return mplew.packet
        }

        fun healMonster(mob: MapleMonster, heal: Int): ByteArray? {
            return onDamaged(mob, false, -heal, 0)
        }

        /**
         * handles both regular and friendly fire damage done to a monster
         * friendly fire being mob damage done to other mobs
         *
         * @param monster MapleMonster instance
         * @param friendly source of damage dealt (either from mob true or from user false)
         * @param damage int value representing damage dealt
         * @param args either damage type for regular damage or remainingHp for friendly
         */
        fun onDamaged(monster: MapleMonster, friendly: Boolean, damage: Int, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MobDamaged.value)
            mplew.writeInt(monster.objectId)
            if (friendly) {
                mplew.write(1) // direction ?
                mplew.writeInt(damage)
                mplew.writeInt(args[0]) // remainingHP
                mplew.writeInt(monster.maxHp)
            } else {
                mplew.write(args[0]) // damage type
                mplew.writeInt(damage)
                if (args[0] != 2) { // damage type
                    mplew.writeInt(monster.hp)
                    mplew.writeInt(monster.maxHp)
                }
            }

            return mplew.packet
        }

        /**
         * This packet shows a special effect depending on the skill id
         *
         * @param oid The object id of the monster
         * @param skillId The skill that has been used
         */
        fun onSpecialEffectBySkill(oid: Int, skillId: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SpecialEffectBySkill.value)
            mplew.writeInt(oid)
            mplew.writeInt(skillId)

            return mplew.packet
        }

        /**
         *
         * @param oid
         * @param remhppercentage
         * @return
         */
        fun onHpIndicator(oid: Int, remhppercentage: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.HPIndicator.value)
            mplew.writeInt(oid)
            mplew.write(remhppercentage)

            return mplew.packet
        }

        fun onCatchEffect(mobOid: Int, success: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CatchEffect.value)
            mplew.writeInt(mobOid)
            mplew.write(success)

            return mplew.packet
        }

        fun onEffectByItem(mobOid: Int, itemid: Int, success: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.EffectByItem.value)
            mplew.writeInt(mobOid)
            mplew.writeInt(itemid)
            mplew.write(success)

            return mplew.packet
        }
    }
}