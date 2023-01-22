package network.packet.wvscontext

import client.MapleFamilyEntry
import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter

class FamilyPacket {

    /**
     * family is not fully functional at the moment. Here are the
     * packet structures for when it is fixed
     * todo this whole file is one big todo
     */

    companion object Packet {

        /**
         * Loads the family pedigree chart
         *
         * @param chrid the id of the user
         * @param members the list of members within the family pedigree
         */
        fun onChartResult(chrid: Int, members: Map<Int?, MapleFamilyEntry?>?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FamilyChartResult.value)
            // todo
            return mplew.packet
        }

        /**
         * Request the family info which is done when a user logs in
         *
         * @param fam the specific family entry pertaining to the user
         */
        fun onInfoResult(fam: MapleFamilyEntry): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FamilyInfoResult.value)
            mplew.writeInt(fam.reputation)
            mplew.writeInt(fam.totalReputation)
            mplew.writeInt(fam.todaysRep)
            mplew.writeShort(fam.juniors)
            mplew.writeShort(fam.totalJuniors)
            mplew.writeShort(0) //Unknown
            mplew.writeInt(fam.id)
            mplew.writeMapleAsciiString(fam.familyName)
            mplew.writeInt(0)
            mplew.writeShort(0)
            return mplew.packet
        }

        /**
         * Handles family error messaging
         * please see FamilyResultType.kt for valid inputs
         */
        fun onFamilyMessage(result: Int, mesos: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(6)
            mplew.writeShort(SendOpcode.FamilyResult.value)
            mplew.writeInt(result)
            mplew.writeInt(mesos)
            return mplew.packet
        }

        /**
         * Sends join packet to user
         *
         * @param playerId id of the user being invited
         * @param inviter the name of the user sending the invite
         */
        fun onJoinRequest(playerId: Int, inviter: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FamilyJoinRequest.value)
            mplew.writeInt(playerId)
            mplew.writeMapleAsciiString(inviter)
            return mplew.packet
        }

        /**
         * Result packet of a join request
         *
         * @param accepted whether the user has accepted the family invite
         * @param added not sure
         */
        fun onJoinRequestResult(accepted: Boolean, added: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FamilyJoinRequestResult.value)
            mplew.write(if (accepted) 1 else 0)
            mplew.writeMapleAsciiString(added)
            return mplew.packet
        }

        /**
         * todo this seems redundant so check if this is the right packet
         */
        fun onJoinAccepted(name: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FamilyJoinAccepted.value)
            mplew.writeMapleAsciiString(name)
            mplew.writeInt(0)
            return mplew.packet
        }

        /**
         * Family packet that loads the privilege list
         * This is always the same for every family?
         */
        fun onPrivilegeList(): ByteArray? {
            val title = arrayOf(
                "Family Reunion",
                "Summon Family",
                "My Drop Rate 1.5x (15 min)",
                "My EXP 1.5x (15 min)",
                "Family Bonding (30 min)",
                "My Drop Rate 2x (15 min)",
                "My EXP 2x (15 min)",
                "My Drop Rate 2x (30 min)",
                "My EXP 2x (30 min)",
                "My Party Drop Rate 2x (30 min)",
                "My Party EXP 2x (30 min)"
            )
            val description = arrayOf(
                "[Target] Me\n[Effect] Teleport directly to the Family member of your choice.",
                "[Target] 1 Family member\n[Effect] Summon a Family member of choice to the map you're in.",
                "[Target] Me\n[Time] 15 min.\n[Effect] Monster drop rate will be increased #c1.5x#.\n*  If the Drop Rate event is in progress, this will be nullified.",
                "[Target] Me\n[Time] 15 min.\n[Effect] EXP earned from hunting will be increased #c1.5x#.\n* If the EXP event is in progress, this will be nullified.",
                "[Target] At least 6 Family members online that are below me in the Pedigree\n[Time] 30 min.\n[Effect] Monster drop rate and EXP earned will be increased #c2x#. \n* If the EXP event is in progress, this will be nullified.",
                "[Target] Me\n[Time] 15 min.\n[Effect] Monster drop rate will be increased #c2x#.\n* If the Drop Rate event is in progress, this will be nullified.",
                "[Target] Me\n[Time] 15 min.\n[Effect] EXP earned from hunting will be increased #c2x#.\n* If the EXP event is in progress, this will be nullified.",
                "[Target] Me\n[Time] 30 min.\n[Effect] Monster drop rate will be increased #c2x#.\n* If the Drop Rate event is in progress, this will be nullified.",
                "[Target] Me\n[Time] 30 min.\n[Effect] EXP earned from hunting will be increased #c2x#. \n* If the EXP event is in progress, this will be nullified.",
                "[Target] My party\n[Time] 30 min.\n[Effect] Monster drop rate will be increased #c2x#.\n* If the Drop Rate event is in progress, this will be nullified.",
                "[Target] My party\n[Time] 30 min.\n[Effect] EXP earned from hunting will be increased #c2x#.\n* If the EXP event is in progress, this will be nullified."
            )
            val repCost = intArrayOf(3, 5, 7, 8, 10, 12, 15, 20, 25, 40, 50)
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FamilyPrivilegeList.value)
            mplew.writeInt(11)
            for (i in 0..10) {
                mplew.write(if (i > 4) i % 2 + 1 else i)
                mplew.writeInt(repCost[i] * 100)
                mplew.writeInt(1)
                mplew.writeMapleAsciiString(title[i])
                mplew.writeMapleAsciiString(description[i])
            }
            return mplew.packet
        }

        /**
         * Family packet that increases family reputation
         *
         * @param gain amount of rep to gain
         * @param mode type of reputation to gain?
         */
        fun onFamousPointIncResult(gain: Int, mode: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FamilyFamousPointIncResult.value)
            mplew.writeInt(gain)
            mplew.writeShort(0)
            return mplew.packet
        }
    }
}