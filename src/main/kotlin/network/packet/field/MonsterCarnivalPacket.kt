package network.packet.field

import client.MapleCharacter
import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter

class MonsterCarnivalPacket {

    companion object Packet {

        /**
         * packet responsible for starting the CPQ match
         */
        fun onEnter(chr: MapleCharacter, team: Int, opposition: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(25)
            mplew.writeShort(SendOpcode.CarnivalEnter.value)
            mplew.write(team)
            mplew.writeShort(chr.cp)
            mplew.writeShort(chr.getTotalCP())
            mplew.writeShort(chr.monsterCarnival.getCP(team))
            mplew.writeShort(chr.monsterCarnival.getTotalCP(team))
            mplew.writeShort(chr.monsterCarnival.getCP(opposition))
            mplew.writeShort(chr.monsterCarnival.getTotalCP(opposition))
            mplew.writeShort(0)
            mplew.writeLong(0)

            return mplew.packet
        }

        fun onPersonalCP(curCP: Int, totalCP: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CarnivalPersonalCP.value)
            mplew.writeShort(curCP)
            mplew.writeShort(totalCP)

            return mplew.packet
        }

        fun onTeamCP(curCP: Int, totalCP: Int, team: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CarnivalTeamCP.value)
            mplew.write(team)
            mplew.writeShort(curCP)
            mplew.writeShort(totalCP)

            return mplew.packet
        }

        /**
         * packet that handles the mob summons for CPQ
         * summon lists are bound to specific tabs and function keys
         *
         * @param name name of user summoning
         * @param tab specify which list to choose from
         * @param num specify which item of list to summon
         */
        fun onSummon(name: String?, tab: Int, num: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CarnivalSummon.value)
            mplew.write(tab)
            mplew.write(num)
            mplew.writeMapleAsciiString(name)

            return mplew.packet
        }

        /**
         * packet responsible for PQ messages sent to users
         *
         * @param message
         */
        fun onRequestResult(message: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(3)
            mplew.writeShort(SendOpcode.CarnivalRequestResult.value)
            mplew.write(message)

            return mplew.packet
        }

        /**
         * packet responsible for handling CP loss on user death
         *
         * @param name user that has died
         * @param lostCP amount to remove from score
         * @param team team which the user is on
         */
        fun onProcessForDeath(name: String?, lostCP: Int, team: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CarnivalProcessForDeath.value)
            mplew.write(team)
            mplew.writeMapleAsciiString(name)
            mplew.write(lostCP)

            return mplew.packet
        }
    }
}