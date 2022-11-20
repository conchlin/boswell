package network.packet

import client.MapleKeyBinding
import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter

class FuncKeyMappedMan {

    companion object Packet {

        fun onFuncKeyMappedItemInit(keybindings: Map<Int?, MapleKeyBinding?>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FuncKeyMappedItemInit.value)
            mplew.write(0)
            for (x in 0..89) {
                val binding = keybindings[Integer.valueOf(x)]
                if (binding != null) {
                    mplew.write(binding.type)
                    mplew.writeInt(binding.action)
                } else {
                    mplew.write(0)
                    mplew.writeInt(0)
                }
            }
            return mplew.packet
        }

        fun onPetConsumeItemInit(itemId: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PetConsumeItemInit.value)
            mplew.writeInt(itemId)

            return mplew.packet
        }

        fun onPetConsumeMPItemInit(itemId: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(6)
            mplew.writeShort(SendOpcode.PetConsumeMPItemInit.value)
            mplew.writeInt(itemId)

            return mplew.packet
        }
    }
}