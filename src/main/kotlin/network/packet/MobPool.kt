package network.packet

import client.status.MonsterStatus
import client.status.MonsterStatusEffect
import network.opcode.SendOpcode
import server.life.MapleMonster
import server.movement.LifeMovementFragment
import tools.MaplePacketCreator
import tools.data.output.LittleEndianWriter
import tools.data.output.MaplePacketLittleEndianWriter
import java.awt.Point

class MobPool {

    companion object Packet {

        fun moveMonster(
            useskill: Int,
            action: Int,
            delay: Int,
            skillInfo: Int,
            oid: Int,
            pos: Point?,
            moves: List<LifeMovementFragment?>?
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MOVE_MONSTER.value)
            mplew.writeInt(oid)
            mplew.write(useskill)
            mplew.write(delay)
            mplew.write(action)
            mplew.writeInt(skillInfo)
            mplew.writePos(pos)
            serializeMovementList(mplew, moves)

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
            mplew.writeShort(SendOpcode.MOVE_MONSTER_RESPONSE.value)
            mplew.writeInt(objectid)
            mplew.writeShort(moveid.toInt())
            mplew.writeBool(useSkills)
            mplew.writeShort(currentMp)
            mplew.write(skillId)
            mplew.write(skillLevel)

            return mplew.packet
        }

        fun applyMonsterStatus(oid: Int, mse: MonsterStatusEffect): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.APPLY_MONSTER_STATUS.value)
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

        fun cancelMonsterStatus(oid: Int, stats: Map<MonsterStatus?, Int?>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CANCEL_MONSTER_STATUS.value)
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
        fun affectedMonster(oid: Int, skillId: Int, delay: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.AFFECTED_MONSTER.value)
            mplew.writeInt(oid)
            mplew.writeInt(skillId) //nSkillId
            mplew.writeShort(delay) //tDelay

            return mplew.packet
        }

        fun healMonster(mob: MapleMonster?, heal: Int): ByteArray? {
            return damageMonster(mob, 0, -heal)
        }

        fun damageMonster(mob: MapleMonster?, type: Int, damage: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.DAMAGE_MONSTER.value)
            mplew.writeInt(mob!!.objectId)
            mplew.write(type)
            mplew.writeInt(damage)
            if (type != 2) {
                mplew.writeInt(mob.hp)
                mplew.writeInt(mob.maxHp)
            }

            return mplew.packet
        }

        fun mobDamageMobFriendly(mob: MapleMonster, damage: Int, remainingHp: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.DAMAGE_MONSTER.value)
            mplew.writeInt(mob.objectId)
            mplew.write(1) // direction ?
            mplew.writeInt(damage)
            mplew.writeInt(remainingHp)
            mplew.writeInt(mob.maxHp)

            return mplew.packet
        }

        /**
         * This packet shows a special effect depending on the skill id
         *
         * @param oid The object id of the monster
         * @param skillId The skill that has been used
         */
        fun monsterSpecialEffect(oid: Int, skillId: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SPECIAL_EFFECT_BY_SKILL.value)
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
        fun showMonsterHP(oid: Int, remhppercentage: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SHOW_MONSTER_HP.value)
            mplew.writeInt(oid)
            mplew.write(remhppercentage)

            return mplew.packet
        }

        fun catchMonster(
            mobOid: Int,
            success: Byte
        ): ByteArray? {   // updated packet structure found thanks to Rien dev team
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CATCH_MONSTER.value)
            mplew.writeInt(mobOid)
            mplew.write(success)

            return mplew.packet
        }

        fun catchMonster(mobOid: Int, itemid: Int, success: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CATCH_MONSTER_WITH_ITEM.value)
            mplew.writeInt(mobOid)
            mplew.writeInt(itemid)
            mplew.write(success)

            return mplew.packet
        }

        private fun serializeMovementList(lew: LittleEndianWriter, moves: List<LifeMovementFragment?>?) {
            if (moves != null) {
                lew.write(moves.size)
            }
            if (moves != null) {
                for (move in moves) {
                    move?.serialize(lew)
                }
            }
        }
    }
}