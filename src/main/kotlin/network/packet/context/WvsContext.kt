package network.packet.context

import client.*
import client.inventory.Equip
import client.inventory.InventoryOperation
import constants.GameConstants
import constants.skills.Buccaneer
import constants.skills.ThunderBreaker
import enums.PopularityResponseType
import enums.WvsMessageType
import network.opcode.SendOpcode
import server.skills.SkillMacro
import tools.Pair
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil
import java.awt.Point
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class WvsContext {

    companion object Packet {

        private val EmptyStatUpdate = emptyList<Pair<MapleStat, Int>>()

        /**
         *  packet responsible for the various inventory actions
         *
         *  @param announce is inventory action broadcast to user (silent if false)
         *  @param mods list of modifications to be applied to inventory
         */
        fun onInventoryOperation(announce: Boolean, mods: List<InventoryOperation>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.InventoryOperation.value)
            mplew.writeBool(announce)
            mplew.write(mods.size)
            var addMovement = -1
            for (mod in mods) {
                mplew.write(mod.mode)
                mplew.write(mod.inventoryType)
                mplew.writeShort((if (mod.mode == 2) mod.oldPosition else mod.position).toInt())
                when (mod.mode) {
                    0 -> { //add item
                        PacketUtil.addItemInfoZeroPos(mplew, mod.item)
                    }

                    1 -> { //update quantity
                        mplew.writeShort(mod.quantity.toInt())
                    }

                    2 -> { //move
                        mplew.writeShort(mod.position.toInt())
                        if (mod.position < 0 || mod.oldPosition < 0) {
                            addMovement = if (mod.oldPosition < 0) 1 else 2
                        }
                    }

                    3 -> { //remove
                        if (mod.position < 0) {
                            addMovement = 2
                        }
                    }

                    4 -> { //itemexp
                        val equip = mod.item as Equip
                        mplew.writeInt(equip.itemExp)
                    }
                }
                mod.clear()
            }
            if (addMovement > -1) {
                mplew.write(addMovement)
            }
            return mplew.packet
        }

        /**
         * packet responsible for modifying the inventory size
         *
         * @param type differentiates between inventory types
         * @param newSize new inventory size of specified inventory type
         */
        fun onInventoryGrow(type: Int, newSize: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.InventoryGrow.value)
            mplew.write(type)
            mplew.write(newSize)

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

        /**
         * packet responsible for applying certain bytes to a character
         * the only instance of this in v83 is the Aran tutorial
         *
         * @param statArray array of bytes to apply to character
         */
        fun onForcedStatSet(statArray: ByteArray): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ForcedStatSet.value)
            mplew.write(statArray)

            return mplew.packet
        }

        /**
         * packet responsible for resetting and forced stat scenarios
         */
        fun onForcedStatReset(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(2)
            mplew.writeShort(SendOpcode.ForcedStatReset.value)

            return mplew.packet
        }

        /**
         * packet responsible for any changes that are made to player skills
         *
         * @param skillId
         * @param currentLevel of skill id being affected
         * @param maxLevel of skill id being affected
         * @param duration of skill id being affected
         */
        fun onChangeSkillRecordResult(skillId: Int, currentLevel: Int, maxLevel: Int, duration: Long): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ChangeSkillRecordResult.value)
            mplew.write(1)
            mplew.writeShort(1)
            mplew.writeInt(skillId)
            mplew.writeInt(currentLevel)
            mplew.writeInt(maxLevel)
            mplew.writeLong(PacketUtil.getTime(duration))
            mplew.write(4)

            return mplew.packet
        }

        /**
         * Packet that handles the receiving and giving of popularity
         *
         * @param response use PopularityResponseType as input
         * @param name username
         * @param args mode and amount
         */
        fun onGivePopularityResult(response: Int, name: String?, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GivePopularityResult.value)
            mplew.write(response)
            when (response) {
                PopularityResponseType.GiveSuccess.value -> {
                    mplew.writeMapleAsciiString(name)
                    mplew.write(args[0]) // mode
                    mplew.writeShort(args[1]) // new fame amount
                    mplew.writeShort(0)
                }

                PopularityResponseType.ReceiveSuccess.value -> {
                    mplew.writeMapleAsciiString(name)
                    mplew.write(args[0]) // mode
                }
            }

            return mplew.packet
        }

        /**
         * packet responsible for all messaging pertaining to the WvsContext
         * (see WvsMessageType for more info)
         *
         * @param type WvsMessageType
         * @param args  Expiration - itemId
         *              Popularity - gain
         *              GuildPoint - GP change
         *              Item - itemId
         */
        fun onMessage(type: Int, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.WvsMessage.value)
            mplew.write(type)
            when (type) {
                WvsMessageType.Expiration.type,
                WvsMessageType.Popularity.type,
                WvsMessageType.GuildPoint.type,
                WvsMessageType.Item.type -> {
                    mplew.writeInt(args[0])
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
                mplew.writeMapleAsciiString(notes.getString("from") + " ")
                mplew.writeMapleAsciiString(notes.getString("message"))
                mplew.writeLong(PacketUtil.getTime(notes.getLong("timestamp")))
                mplew.write(notes.getByte("fame")) //FAME :D
                notes.next()
            }
            return mplew.packet
        }


        /**
         * packet responsible for map travel when using (vip) teleport rocks
         *
         * @param user MapleCharacter instance
         * @param delete do we save the map?
         * @param vip vip or regular teleport rock
         */
        fun onMapTransferResult(user: MapleCharacter, delete: Boolean, vip: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MapTransferResult.value)
            mplew.write(if (delete) 2 else 3)
            if (vip) {
                mplew.write(1)
                val map = user.vipTrockMaps
                for (i in 0..9) {
                    mplew.writeInt(map[i])
                }
            } else {
                mplew.write(0)
                val map = user.trockMaps
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

        fun onSetTamingMobInfo(charId: Int, mount: MapleMount, levelUp: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SetTamingMobInfo.value)
            mplew.writeInt(charId)
            mplew.writeInt(mount.level)
            mplew.writeInt(mount.exp)
            mplew.writeInt(mount.tiredness)
            mplew.write(if (levelUp) 1.toByte() else 0.toByte())

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

        fun onSkillLearnItemResult(
            user: MapleCharacter,
            skillId: Int,
            maxLevel: Int,
            canUse: Boolean,
            success: Boolean
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SkillLearnItemResult.value)
            mplew.writeInt(user.id)
            mplew.write(1)
            mplew.writeInt(skillId)
            mplew.writeInt(maxLevel)
            mplew.write(if (canUse) 1 else 0)
            mplew.write(if (success) 1 else 0)

            return mplew.packet
        }

        fun onGatherItemResult(inv: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(4)
            mplew.writeShort(SendOpcode.GatherItemResult.value)
            mplew.write(0)
            mplew.write(inv)
            return mplew.packet
        }

        fun onSortItemResult(inv: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(4)
            mplew.writeShort(SendOpcode.SortItemResult.value)
            mplew.write(0)
            mplew.write(inv)
            return mplew.packet
        }

        /**
         * packet responsible for user reports
         *
         * Possible values for `mode`:
         * 0: You have successfully reported the user.
         * 1: Unable to locate the user.
         * 2: You may only report users 10 times a day.
         * 3: You have been reported to the GM's by a user.
         * 4: Your request did not go through for unknown reasons. Please try again later.
         *
         * @param mode
         */
        fun onSueCharacterResult(mode: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SueCharacterResult.value)
            mplew.write(mode)
            return mplew.packet
        }

        fun onTradeMoneyLimit(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.TradeMoneyLimit.value)
            return mplew.packet
        }

        fun onSetGender(chr: MapleCharacter): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(3)
            mplew.writeShort(SendOpcode.SetGender.value)
            mplew.write(chr.gender)
            return mplew.packet
        }

        /**
         * packet responsible for creating or destroying a town portal (mystic door)
         * @param townId The ID of the town the portal goes to.
         * @param targetId The ID of the target.
         * @param pos Where to put the portal.
         * @param remove destroy it
         */
        fun onTownPortal(townId: Int, targetId: Int, pos: Point?, remove: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(14)
            mplew.writeShort(SendOpcode.TownPortal.value)
            if (remove) {
                mplew.writeInt(999999999)
                mplew.writeInt(999999999)
            } else {
                mplew.writeInt(townId)
                mplew.writeInt(targetId)
                mplew.writePos(pos)
            }
            return mplew.packet
        }

        fun onIncubatorResult(): ByteArray? { //lol
            val mplew = MaplePacketLittleEndianWriter(8)
            mplew.writeShort(SendOpcode.IncubatorResult.value)
            mplew.skip(6)
            return mplew.packet
        }

        /**
         * packet responsible for adding cards to monster book
         *
         * @param complete is set full
         * @param cardId
         * @param cardAmount current amount (?/5)
         */
        fun onSetCard(complete: Boolean, cardId: Int, cardAmount: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(11)
            mplew.writeShort(SendOpcode.MonsterBookSetCard.value)
            mplew.write(if (complete) 0 else 1)
            mplew.writeInt(cardId)
            mplew.writeInt(cardAmount)
            return mplew.packet
        }

        /**
         * packet responsible for modifying the monster book cover
         *
         * @param cardId monster card
         */
        fun onSetCover(cardId: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(6)
            mplew.writeShort(SendOpcode.MonsterBookSetCover.value)
            mplew.writeInt(cardId)
            return mplew.packet
        }

        /**
         *  packet responsible for level up notifications to the guild or family
         *
         * Possible values for <code>type</code>:
         * <br> 0: <Family> ? has reached Lv. ?.<br> - The Reps you have received from ? will be reduced in half.
         * 1: <Family> ? has reached Lv. ?.<br>
         * 2: <Guild> ? has reached Lv. ?.<br>
         *
         * @param type
         * @param level
         * @param userName
         */
        fun onNotifyLevelUp(type: Int, level: Int, userName: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.NotifyLevelUp.value)
            mplew.write(type)
            mplew.writeInt(level)
            mplew.writeMapleAsciiString(userName)
            return mplew.packet
        }

        /**
         * packet responsible for notifying marriage to the guild or family
         */
        fun onNotifyWedding(type: Int, userName: String): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.NotifyWedding.value)
            mplew.write(type) // 0: guild, 1: family
            mplew.writeMapleAsciiString("> $userName")
            return mplew.packet
        }

        /**
         * packet responsible for notifying job change to the guild or family
         */
        fun onNotifyJobChange(type: Int, job: Int, userName: String): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.NotifyJobChange.value)
            mplew.write(type) // 0: guild, 1: family
            mplew.writeInt(job)
            mplew.writeMapleAsciiString("> $userName")
            return mplew.packet
        }

        /**
         * packet responsible for broadcasting an avatar megaphone to the server
         *
         * @param user
         * @param medal id of medal user has equipped
         * @param channel channel #
         * @param itemId which mega is being broadcast
         * @param message the lines of text
         * @param ear is ear shown
         */
        fun onSetAvatarMegaphone(
            user: MapleCharacter,
            medal: String,
            channel: Int,
            itemId: Int,
            message: List<String?>,
            ear: Boolean
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SetAvatarMegaphone.value)
            mplew.writeInt(itemId)
            mplew.writeMapleAsciiString(medal + user.name)
            for (s in message) {
                mplew.writeMapleAsciiString(s)
            }
            mplew.writeInt(channel - 1) // channel
            mplew.writeBool(ear)
            PacketUtil.addCharLook(mplew, user, true)
            return mplew.packet
        }

        /**
         * packet responsible for removing avatar mega from screen
         */
        fun onClearAvatarMegaphone(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ClearAvatarMegaphone.value)
            mplew.write(1)
            return mplew.packet
        }

        fun onFakeGMNotice(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FakeGMNotice.value)
            mplew.write(0) //doesn't even matter what value
            return mplew.packet
        }

        /**
         * packet responsible for broadcasting the yellow messages on the users screen
         *
         * @param msg
         */
        fun onScriptProgressMessage(msg: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ScriptProgressMessage.value)
            mplew.writeMapleAsciiString(msg)
            return mplew.packet
        }

        /**
         * packet responsible for showing player the GM police popup window when being banned
         *
         * @param text ban message
         */
        fun onDataCRCCheckFailed(text: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.DataCRCCheckFailed.value)
            mplew.writeMapleAsciiString(text)
            return mplew.packet
        }

        /**
         * packet responsible for loading macro skills of characters
         *
         * @param macros
         */
        fun onMacroSysDataInit(macros: Array<SkillMacro?>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MacroSysDataInit.value)
            var count = 0
            for (i in 0..4) {
                if (macros[i] != null) {
                    count++
                }
            }
            mplew.write(count)
            for (i in 0..4) {
                val macro = macros[i]
                if (macro != null) {
                    mplew.writeMapleAsciiString(macro.name)
                    mplew.write(macro.shout)
                    mplew.writeInt(macro.skill1)
                    mplew.writeInt(macro.skill2)
                    mplew.writeInt(macro.skill3)
                }
            }
            return mplew.packet
        }
    }
}