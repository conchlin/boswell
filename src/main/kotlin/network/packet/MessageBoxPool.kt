package network.packet

import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter
import java.awt.Point

class MessageBoxPool {

    companion object Packet {

        fun onCreateFailed(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CreateFailed.value)

            return mplew.packet
        }

        fun onMessageBoxEnterField(
            oid: Int,
            itemid: Int,
            name: String?,
            msg: String?,
            pos: Point,
            ft: Int
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MessageBoxEnterField.value)
            mplew.writeInt(oid)
            mplew.writeInt(itemid)
            mplew.writeMapleAsciiString(msg)
            mplew.writeMapleAsciiString(name)
            mplew.writeShort(pos.x)
            mplew.writeShort(ft)

            return mplew.packet
        }

        fun onMessageBoxLeaveField(objectid: Int, animationType: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MessageBoxLeaveField.value)
            mplew.write(animationType) // 0 is 10/10, 1 just vanishes
            mplew.writeInt(objectid)

            return mplew.packet
        }
    }
}