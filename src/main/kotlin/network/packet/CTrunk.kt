package network.packet

import client.inventory.Item
import enums.TrunkResultType
import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil

class CTrunk {

    companion object Packet {

        /**
         * onTrunkResult sends packet that performs the major storage operations
         *
         * @param result see TrunkResultType
         * @param slots the slots affected
         * @param items the list of items being written to the packet
         */
        fun onTrunkResult(
            result: Byte,
            slots: Byte,
            items: Collection<Item?>,
            vararg args: Int
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.Trunk.value)
            mplew.write(result)
            when (result) {
                TrunkResultType.DepositItem.result,
                TrunkResultType.RetrieveItem.result -> {
                    mplew.write(slots)
                    mplew.writeShort(args[0]) // inventory type
                    mplew.writeShort(0)
                    mplew.writeInt(0)
                    mplew.write(items.size)
                    for (item in items) {
                        PacketUtil.addItemInfoZeroPos(mplew, item)
                    }
                }
                TrunkResultType.Arrange.result -> {
                    mplew.write(slots)
                    mplew.write(124)
                    mplew.skip(10)
                    mplew.write(items.size)
                    for (item in items) {
                        PacketUtil.addItemInfoZeroPos(mplew, item)
                    }
                    mplew.write(0)
                }
                TrunkResultType.OpenStorage.result -> {
                    mplew.writeInt(args[0]) // npc id
                    mplew.write(slots)
                    mplew.writeShort(126)
                    mplew.writeShort(0)
                    mplew.writeInt(0)
                    mplew.writeInt(args[1]) // meso amount
                    mplew.writeShort(0)
                    mplew.write(items.size.toByte())
                    for (item in items) {
                        PacketUtil.addItemInfoZeroPos(mplew, item)
                    }
                    mplew.writeShort(0)
                    mplew.write(0)
                }
            }
            return mplew.packet
        }

        /**
         *  onTrunkResult method that sends a packet to deposit/retrieve meso
         *
         * @param result see TrunkResultType
         * @param slots the slots affected
         * @param meso the amount being moved
         */
        fun onTrunkResult(result: Byte, slots: Byte, meso: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.Trunk.value)
            mplew.write(result)
            when (result) {
                TrunkResultType.MesoAction.result -> {
                    mplew.write(slots)
                    mplew.writeShort(2)
                    mplew.writeShort(0)
                    mplew.writeInt(0)
                    mplew.writeInt(meso)
                }
            }
            return mplew.packet
        }

        /*/**
         * overloaded TrunkResult method that sends a packet to arrange storage items
         *
         * @param result see TrunkResultType
         * @param slots the slots affected
         * @param items the list of items being sorted
         */
        fun onTrunkResult(result: Byte, slots: Byte, items: Collection<Item?>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.Trunk.value)
            mplew.write(result)
            when (result) {
                TrunkResultType.Arrange.result -> {
                    mplew.write(slots)
                    mplew.write(124)
                    mplew.skip(10)
                    mplew.write(items.size)
                    for (item in items) {
                        PacketUtil.addItemInfoZeroPos(mplew, item)
                    }
                    mplew.write(0)
                }
            }
            return mplew.packet
        }*/

        // use TrunkResultType.kt for error values
        fun onTrunkError(i: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.Trunk.value)
            mplew.write(i)

            return mplew.packet
        }

    }
}