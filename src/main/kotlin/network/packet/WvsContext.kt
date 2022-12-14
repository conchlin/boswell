package network.packet

import client.*
import constants.GameConstants
import constants.skills.Buccaneer
import constants.skills.ThunderBreaker
import enums.FameResponseType
import network.opcode.SendOpcode
import tools.Pair
import tools.data.output.MaplePacketLittleEndianWriter
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class WvsContext {

    companion object Packet {

        val EmptyStatUpdate = emptyList<Pair<MapleStat, Int>>()

        fun onInventoryGrow(type: Int, newLimit: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.InventoryGrow.value)
            mplew.write(type)
            mplew.write(newLimit)

            return mplew.packet
        }

        /**
         * Gets an update for specified stats.
         *
         * @param stats The list of stats to update.
         * @param itemReaction Result of an item reaction(?)
         *
         * @return The stat update packet.
         */
        fun updatePlayerStats(
            stats: List<Pair<MapleStat, Int>>,
            itemReaction: Boolean, chr: MapleCharacter?
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.STAT_CHANGED.value)
            mplew.write(if (itemReaction) 1 else 0)
            var updateMask = 0
            for (statupdate in stats) {
                updateMask = updateMask or statupdate.getLeft().value
            }
            if (stats.size > 1) {
                Collections.sort(stats) { o1: Pair<MapleStat, Int>, o2: Pair<MapleStat, Int> ->
                    val val1 = o1.getLeft().value
                    val val2 = o2.getLeft().value
                    if (val1 < val2) -1 else if (val1 == val2) 0 else 1
                }
            }
            mplew.writeInt(updateMask)
            for (statupdate in stats) {
                if (statupdate.getLeft().value < 1) {
                    continue  //just to reduce nested shiets
                }
                when (statupdate.getLeft().value) {
                    0x1 -> mplew.writeShort(statupdate.getRight().toShort().toInt())
                    0x8000 -> if (GameConstants.hasSPTable(chr!!.job)) {
                        mplew.write(chr.remainingSp)
                        var i = 0
                        while (i < chr.remainingSps.size) {
                            if (chr.getRemainingSpBySkill(i) > 0) {
                                mplew.write(i + 1)
                                mplew.write(chr.getRemainingSpBySkill(i))
                            }
                            i++
                        }
                    } else {
                        mplew.writeShort(statupdate.getRight().toShort().toInt())
                    }
                    else -> if (statupdate.getLeft().value <= 0x4) {
                        mplew.writeInt(statupdate.getRight())
                    } else if (statupdate.getLeft().value < 0x20) {
                        mplew.write(statupdate.getRight().toShort().toInt())
                    } else if (statupdate.getLeft().value < 0xFFFF) {
                        mplew.writeShort(statupdate.getRight().toShort().toInt())
                    } else {
                        mplew.writeInt(statupdate.getRight())
                    }
                }
            }
            return mplew.packet
        }

        fun petStatUpdate(chr: MapleCharacter): ByteArray? {
            // this actually does nothing... packet structure and stats needs to be uncovered
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.STAT_CHANGED.value)
            var mask = 0
            mask = mask or MapleStat.PET.value
            mplew.write(0)
            mplew.writeInt(mask)
            val pets = chr.pets
            for (i in 0..2) {
                if (pets[i] != null) {
                    mplew.writeLong(pets[i]!!.uniqueId.toLong())
                } else {
                    mplew.writeLong(0)
                }
            }
            mplew.write(0)

            return mplew.packet
        }

        /**
         * Gets an empty stat update.
         *
         * @return The empty stat update packet.
         */
        fun enableActions(): ByteArray? {
            return updatePlayerStats(EmptyStatUpdate, true, null)
        }

        /**
         * Gets an update for specified stats.
         *
         * @param stats The stats to update.
         *
         * @return The stat update packet.
         */
        fun updatePlayerStats(stats: List<Pair<MapleStat, Int>>, chr: MapleCharacter?): ByteArray? {
            return updatePlayerStats(stats, false, chr)
        }

        /**
         * It is important that statups is in the correct order (see declaration
         * order in MapleBuffStat) since this method doesn't do automagical
         * reordering.
         *
         * @param buffid
         * @param bufflength
         * @param statups
         * @return
         */
        fun giveBuff(buffid: Int, bufflength: Int, statups: List<Pair<MapleBuffStat?, BuffValueHolder>>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GIVE_BUFF.value)
            var special = false
            writeLongMask(mplew, statups)
            for (statup in statups) {
                if (statup.getLeft() == MapleBuffStat.MONSTER_RIDING || statup.getLeft() == MapleBuffStat.HOMING_BEACON) {
                    special = true
                }
                if (statup.getLeft()?.isDisease == true) {
                    mplew.writeShort(statup.getRight().value)
                    mplew.writeShort(statup.getRight().sourceID)
                    mplew.writeShort(statup.getRight().sourceLevel)
                } else if (statup.getLeft() == MapleBuffStat.SHARP_EYES) {
                    mplew.write(statup.getRight().value)
                    mplew.write(statup.getRight().sourceLevel)
                    mplew.writeInt(buffid)
                } else {
                    mplew.writeShort(statup.getRight().value)
                    mplew.writeInt(buffid)
                }
                mplew.writeInt(bufflength)
            }
            mplew.writeInt(0)
            mplew.write(0)
            mplew.writeInt(statups[0].getRight().value) //Homing beacon ...
            if (special) {
                mplew.skip(3)
            }
            return mplew.packet
        }

        private fun writeLongMask(
            mplew: MaplePacketLittleEndianWriter,
            statups: List<Pair<MapleBuffStat?, BuffValueHolder>>
        ) {
            val mask = IntArray(4)
            for (statup in statups) {
                mask[statup.left?.set?.toInt()!!] = mask[statup.left?.set?.toInt()!!] or statup.left?.mask!!
            }
            for (i in 3 downTo 0) {
                mplew.writeInt(mask[i])
            }
        }

        private fun writeLongMaskFromList(mplew: MaplePacketLittleEndianWriter, statups: List<MapleBuffStat>) {
            val mask = IntArray(4)
            for (statup in statups) {
                mask[statup.set.toInt()] = mask[statup.set.toInt()] or statup.mask
            }
            for (i in 3 downTo 0) {
                mplew.writeInt(mask[i])
            }
        }

        fun givePirateBuff(
            statups: List<Pair<MapleBuffStat?, BuffValueHolder>>,
            buffid: Int,
            duration: Int
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            val infusion = buffid == Buccaneer.SPEED_INFUSION || buffid == ThunderBreaker.SPEED_INFUSION
            mplew.writeShort(SendOpcode.GIVE_BUFF.value)
            writeLongMask(mplew, statups)
            mplew.writeShort(0)
            for (stat in statups) {
                mplew.writeInt(stat.getRight().value)
                mplew.writeInt(buffid)
                mplew.skip(if (infusion) 10 else 5)
                mplew.writeShort(duration)
            }
            mplew.skip(3)
            return mplew.packet
        }

        fun cancelBuff(statups: List<MapleBuffStat>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CANCEL_BUFF.value)
            writeLongMaskFromList(mplew, statups)
            mplew.write(1) //?

            return mplew.packet
        }

        fun aranGodlyStats(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FORCED_STAT_SET.value)
            mplew.write(
                byteArrayOf(
                    0x1F.toByte(),
                    0x0F.toByte(),
                    0,
                    0,
                    0xE7.toByte(),
                    3,
                    0xE7.toByte(),
                    3,
                    0xE7.toByte(),
                    3,
                    0xE7.toByte(),
                    3,
                    0xFF.toByte(),
                    0,
                    0xE7.toByte(),
                    3,
                    0xE7.toByte(),
                    3,
                    0x78.toByte(),
                    0x8C.toByte()
                )
            )
            return mplew.packet
        }

        fun resetForcedStats(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(2)
            mplew.writeShort(SendOpcode.FORCED_STAT_RESET.value)

            return mplew.packet
        }

        /**
         * Packet that handles the receiving and giving of fame
         *
         * response: use FameResponseType as input
         */
        fun onFameResponse(response: Int, name: String?, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FAME_RESPONSE.value)
            mplew.write(response)
            when (response) {
                FameResponseType.GiveSuccess.value -> {
                    mplew.writeMapleAsciiString(name)
                    mplew.write(args[0]) // mode
                    mplew.writeShort(args[1]) // new fame amount
                    mplew.writeShort(0)
                }
                FameResponseType.ReceiveSuccess.value -> {
                    mplew.writeMapleAsciiString(name)
                    mplew.write(args[0]) // mode
                }
            }

            return mplew.packet
        }

        fun noteSendMsg(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(3)
            mplew.writeShort(SendOpcode.MEMO_RESULT.value)
            mplew.write(4)
            return mplew.packet
        }

        /*
         *  0 = Player online, use whisper
         *  1 = Check player's name
         *  2 = Receiver inbox full
     */
        fun noteError(error: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(4)
            mplew.writeShort(SendOpcode.MEMO_RESULT.value)
            mplew.write(5)
            mplew.write(error)
            return mplew.packet
        }

        @Throws(SQLException::class)
        fun showNotes(notes: ResultSet, count: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MEMO_RESULT.value)
            mplew.write(3)
            mplew.write(count)
            for (i in 0 until count) {
                mplew.writeInt(notes.getInt("id"))
                mplew.writeMapleAsciiString(notes.getString("from") + " ") //Stupid nexon forgot space lol
                mplew.writeMapleAsciiString(notes.getString("message"))
                //mplew.writeLong(MaplePacketCreator.getTime(notes.getLong("timestamp"))) todo add time/expiration stuff here
                mplew.write(notes.getByte("fame")) //FAME :D
                notes.next()
            }
            return mplew.packet
        }

        fun trockRefreshMapList(chr: MapleCharacter, delete: Boolean, vip: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MAP_TRANSFER_RESULT.value)
            mplew.write(if (delete) 2 else 3)
            if (vip) {
                mplew.write(1)
                val map = chr.vipTrockMaps
                for (i in 0..9) {
                    mplew.writeInt(map[i])
                }
            } else {
                mplew.write(0)
                val map = chr.trockMaps
                for (i in 0..4) {
                    mplew.writeInt(map[i])
                }
            }

            return mplew.packet
        }

        fun enableReport(): ByteArray? { // thanks to snow
            val mplew = MaplePacketLittleEndianWriter(3)
            mplew.writeShort(SendOpcode.CLAIM_STATUS_CHANGED.value)
            mplew.write(1)

            return mplew.packet
        }

        fun updateMount(charid: Int, mount: MapleMount, levelup: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SET_TAMING_MOB_INFO.value)
            mplew.writeInt(charid)
            mplew.writeInt(mount.level)
            mplew.writeInt(mount.exp)
            mplew.writeInt(mount.tiredness)
            mplew.write(if (levelup) 1.toByte() else 0.toByte())

            return mplew.packet
        }

        fun getShowQuestCompletion(id: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.QUEST_CLEAR.value)
            mplew.writeShort(id)

            return mplew.packet
        }

        fun hiredMerchantBox(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.value) // header.
            mplew.write(0x07)

            return mplew.packet
        }

        fun retrieveFirstMessage(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.value) // header.
            mplew.write(0x09)

            return mplew.packet
        }

        fun remoteChannelChange(ch: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.value) // header.
            mplew.write(0x10)
            mplew.writeInt(0) //No idea yet
            mplew.write(ch)

            return mplew.packet
        }

        fun skillBookResult(
            chr: MapleCharacter,
            skillid: Int,
            maxlevel: Int,
            canuse: Boolean,
            success: Boolean
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SKILL_LEARN_ITEM_RESULT.value)
            mplew.writeInt(chr.id)
            mplew.write(1)
            mplew.writeInt(skillid)
            mplew.writeInt(maxlevel)
            mplew.write(if (canuse) 1 else 0)
            mplew.write(if (success) 1 else 0)

            return mplew.packet
        }
    }
}