package network.packet

import network.opcode.SendOpcode
import server.maps.MapleReactor
import tools.data.output.MaplePacketLittleEndianWriter

class ReactorPool {

    // naming this packet for readability purposes when called in java
    companion object Packet {

        fun onReactorChangeState(reactor: MapleReactor, stance: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            val pos = reactor.position
            mplew.writeShort(SendOpcode.ReactorChangeState.value)
            mplew.writeInt(reactor.objectId)
            mplew.write(reactor.state)
            mplew.writePos(pos)
            mplew.write(stance)
            mplew.writeShort(0)
            mplew.write(5) // frame delay, set to 5 since there doesn't appear to be a fixed formula for it

            return mplew.packet
        }

        fun onReactorEnterField(reactor: MapleReactor): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            val pos = reactor.position
            mplew.writeShort(SendOpcode.ReactorEnterField.value)
            mplew.writeInt(reactor.objectId)
            mplew.writeInt(reactor.id)
            mplew.write(reactor.state)
            mplew.writePos(pos)
            mplew.write(0)
            mplew.writeShort(0)

            return mplew.packet
        }

        fun onReactorLeaveField(reactor: MapleReactor): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            val pos = reactor.position
            mplew.writeShort(SendOpcode.ReactorLeaveField.value)
            mplew.writeInt(reactor.objectId)
            mplew.write(reactor.state)
            mplew.writePos(pos)

            return mplew.packet
        }
    }
}