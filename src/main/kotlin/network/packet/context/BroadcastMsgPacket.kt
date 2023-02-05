package network.packet.context

import client.MapleCharacter
import client.inventory.Item
import enums.BroadcastMessageType
import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil

class BroadcastMsgPacket {

    companion object Packet {

        /**
         * packet responsible for handling the various items/actions that broadcast
         * messages to the server
         *
         * @param type see BroadcastMessageType.kt
         * @param msg text
         * @param banner is this that top scrolling announcement?
         * @param args see descriptions
         */
        fun onBroadcastMsg(type: Int, msg: String?, banner: Boolean, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.BroadcastMsg.value)
            mplew.write(type)
            if (banner) {
                mplew.write(1)
            }
            mplew.writeMapleAsciiString(msg)
            when (type) {
                BroadcastMessageType.SuperMegaphone.type -> {
                    mplew.write(args[0] - 1) // channel
                    mplew.writeBool(args[1] == 0) //megaEar
                }
                BroadcastMessageType.BlueText.type -> {
                    mplew.writeInt(0)
                }
                BroadcastMessageType.BroadcastNPC.type -> {
                    mplew.writeInt(args[0]) // npcId
                }
            }
            return mplew.packet
        }

        /**
         * Simplified onBroadcastMsg used for most generic messages
         *
         * @param type
         * @param message
         */
        fun onBroadcastMsg(type: Int, message: String?): ByteArray? {
            return onBroadcastMsg(type, message, false)
        }

        /**
         * Broadcast a server banner message
         *
         * @param message
         */
        fun onBroadcastBanner(message: String?): ByteArray? {
            return onBroadcastMsg(BroadcastMessageType.Banner.type, message, true)
        }

        /**
         * broadcast gachapon item message - only triggers for certain items
         *
         * @param user
         * @param item
         * @param town
         */
        fun onBroadcastGachapon(user: MapleCharacter, item: Item, town: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.BroadcastMsg.value)
            mplew.write(BroadcastMessageType.Gachapon.type)
            mplew.writeMapleAsciiString(user.name + " : got a(n)")
            mplew.writeInt(0)
            mplew.writeMapleAsciiString(town)
            PacketUtil.addItemInfoZeroPos(mplew, item)
            return mplew.packet
        }
    }
}