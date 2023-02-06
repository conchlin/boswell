package network.packet

import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter

class MapLoadable {

    companion object Packet {

        /**
         * Changes the current background effect to either render or not.
         * Data is still missing, so this is pretty binary at the moment in how it
         * behaves.
         *
         * @param remove remove or add the specified layer.
         * @param layer the targeted layer for removal or addition.
         * @param transition the time it takes to transition the effect.
         *
         * @return a packet to change the background effect of a specified layer.
         */
        fun onSetBackEffect(remove: Boolean, layer: Int, transition: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SetBackEffect.value)
            mplew.writeBool(remove)
            mplew.writeInt(0) // not sure what this int32 does yet
            mplew.write(layer)
            mplew.writeInt(transition)

            return mplew.packet
        }

        // the two functions below are unimplemented

        fun onSetMapObjectVisible(obId: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SetMapObjectVisible.value)
            mplew.write(obId)

            return mplew.packet
        }

        fun onClearBackEffect(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ClearBackEffect.value)

            return mplew.packet
        }
    }
}