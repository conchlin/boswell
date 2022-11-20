package network.packet

import network.opcode.SendOpcode
import server.maps.AffectedArea
import tools.data.output.MaplePacketLittleEndianWriter

class AffectedAreaPool {

    companion object Packet {

        fun onAffectedAreaCreated(oid: Int, ownerCid: Int, skill: Int, level: Int, mist: AffectedArea): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.AffectedAreaCreated.value)
            mplew.writeInt(oid)
            // mob mist = 0, player poison = 1, smokescreen = 2, unknown = 3, recovery = 4
            mplew.writeInt(if (mist.isMobMist) 0 else if (mist.isPoisonMist) 1 else if (mist.isRecoveryMist) 4 else 2)
            mplew.writeInt(ownerCid)
            mplew.writeInt(skill)
            mplew.write(level)
            mplew.writeShort(mist.skillDelay) // Skill delay
            mplew.writeInt(mist.box.x)
            mplew.writeInt(mist.box.y)
            mplew.writeInt(mist.box.x + mist.box.width)
            mplew.writeInt(mist.box.y + mist.box.height)
            mplew.writeInt(0)

            return mplew.packet
        }

        fun onAffectedAreaRemoved(oid: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.AffectedAreaRemoved.value)
            mplew.writeInt(oid)

            return mplew.packet
        }
    }
}