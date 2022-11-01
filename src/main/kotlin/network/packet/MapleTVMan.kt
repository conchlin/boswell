package network.packet

import client.MapleCharacter
import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil

class MapleTVMan {

    companion object Packet {

        /**
         * Sends MapleTV
         *
         * @param chr The character shown in TV
         * @param messages The message sent with the TV
         * @param type The type of TV
         * @param partner The partner shown with chr
         * @return the SEND_TV packet
         */
        fun sendTV(chr: MapleCharacter, messages: List<String>, type: Int, partner: MapleCharacter?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SEND_TV.value)
            mplew.write(if (partner != null) 3 else 1)
            mplew.write(type) //Heart = 2  Star = 1  Normal = 0
            PacketUtil.addCharLook(mplew, chr, false)
            mplew.writeMapleAsciiString(chr.name)
            if (partner != null) {
                mplew.writeMapleAsciiString(partner.name)
            } else {
                mplew.writeShort(0)
            }
            for (i in messages.indices) {
                if (i == 4 && messages[4].length > 15) {
                    mplew.writeMapleAsciiString(messages[4].substring(0, 15))
                } else {
                    mplew.writeMapleAsciiString(messages[i])
                }
            }
            mplew.writeInt(1337) // time limit
            if (partner != null) {
                PacketUtil.addCharLook(mplew, partner, false)
            }

            return mplew.packet
        }

        /**
         * Removes TV
         *
         * @return The Remove TV Packet
         */
        fun removeTV(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(2)
            mplew.writeShort(SendOpcode.REMOVE_TV.value)

            return mplew.packet
        }

        fun enableTV(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(7)
            mplew.writeShort(SendOpcode.ENABLE_TV.value)
            mplew.writeInt(0)
            mplew.write(0)

            return mplew.packet
        }
    }
}