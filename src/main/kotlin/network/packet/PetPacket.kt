package network.packet

import client.MapleCharacter
import client.inventory.MaplePet
import network.opcode.SendOpcode
import server.movement.LifeMovementFragment
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil
import java.awt.Point

class PetPacket {

    // naming this packet for readability purposes when called in java
    companion object Packet {

        fun onPetActivated(chr: MapleCharacter, pet: MaplePet?, remove: Boolean, hunger: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PetActivated.value)
            mplew.writeInt(chr.id)
            mplew.write(chr.getPetIndex(pet))
            if (remove) {
                mplew.write(0)
                mplew.write(if (hunger) 1 else 0)
            } else {
                addPetInfo(mplew, pet, true)
            }

            return mplew.packet
        }

        /**
         * @param chr MapleCharacter
         * @param pet MaplePet
         * @param p new point position of movement
         * @param moves movement size to write
         */
        fun onMove(chr: MapleCharacter, pet: MaplePet?, p: Point?, moves: List<LifeMovementFragment?>?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PetMove.value)
            mplew.writeInt(chr.id)
            mplew.write(chr.getPetIndex(pet))
            mplew.writePos(p)
            PacketUtil.serializeMovementList(mplew, moves)

            return mplew.packet
        }

        fun onAction(cid: Int, index: Byte, act: Int, text: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PetAction.value)
            mplew.writeInt(cid)
            mplew.write(index)
            mplew.write(0)
            mplew.write(act)
            mplew.writeMapleAsciiString(text)
            mplew.write(0)

            return mplew.packet
        }

        fun onNameChange(chr: MapleCharacter, newname: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PetNameChange.value)
            mplew.writeInt(chr.id)
            mplew.write(0)
            mplew.writeMapleAsciiString(newname)
            mplew.write(0)

            return mplew.packet
        }

        fun onLoadExceptionList(cid: Int, petId: Int, petIdx: Byte, data: List<Int?>): ByteArray {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PetLoadExceptionList.value)
            mplew.writeInt(cid)
            mplew.write(petIdx)
            mplew.writeLong(petId.toLong())
            mplew.write(data.size)
            for (ids in data) {
                mplew.writeInt(ids!!)
            }

            return mplew.packet
        }

        /**
         * @param userId
         * @param foodAction whether it is a feeding action
         * @param success Randomizer.nextInt(100) < petCommand.getProbability()
         * @param animation depending on command value
         * @param balloonType seems to be always false perhaps this is not needed
         */
        fun onActionCommand(userId: Int,
                            index: Byte,
                            foodAction: Boolean,
                            success: Boolean,
                            animation: Int,
                            balloonType: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PetActionCommand.value)
            mplew.writeInt(userId)
            mplew.write(index)
            if (foodAction) {
                mplew.write(1)
                mplew.writeBool(success)
                mplew.writeBool(balloonType)
            } else {
                mplew.write(0)
                mplew.write(animation)
                mplew.writeBool(success)
                mplew.writeBool(balloonType)
            }
            return mplew.packet
        }

        private fun addPetInfo(mplew: MaplePacketLittleEndianWriter, pet: MaplePet?, showpet: Boolean) {
            mplew.write(1)
            if (showpet) mplew.write(0)
            mplew.writeInt(pet!!.itemId)
            mplew.writeMapleAsciiString(pet.name)
            mplew.writeLong(pet.uniqueId.toLong())
            mplew.writePos(pet.pos)
            mplew.write(pet.stance)
            mplew.writeInt(pet.fh)
        }
    }
}