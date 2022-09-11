package network.packet

import client.MapleCharacter
import client.inventory.MaplePet
import network.opcode.SendOpcode
import server.movement.LifeMovementFragment
import tools.data.output.LittleEndianWriter
import tools.data.output.MaplePacketLittleEndianWriter
import java.awt.Point

class PetPacket {

    // naming this packet for readability purposes when called in java
    companion object Packet {

        fun showPet(chr: MapleCharacter, pet: MaplePet?, remove: Boolean, hunger: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SPAWN_PET.value)
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
         * This is the packet for pet movement
         *
         * @param MapleCharacter chr
         * @param MaplePet pet
         * @param Point p
         * @param List<LifeMovementFragment> moves
         *
         * @return MaplePacketLittleEndianWriter mplew
        </LifeMovementFragment> */
        fun movePet(chr: MapleCharacter, pet: MaplePet?, p: Point?, moves: List<LifeMovementFragment?>?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MOVE_PET.value)
            mplew.writeInt(chr.id)
            mplew.write(chr.getPetIndex(pet))
            mplew.writePos(p)
            serializeMovementList(mplew, moves)

            return mplew.packet
        }

        fun petChat(cid: Int, index: Byte, act: Int, text: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PET_CHAT.value)
            mplew.writeInt(cid)
            mplew.write(index)
            mplew.write(0)
            mplew.write(act)
            mplew.writeMapleAsciiString(text)
            mplew.write(0)

            return mplew.packet
        }

        fun changePetName(chr: MapleCharacter, newname: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PET_NAMECHANGE.value)
            mplew.writeInt(chr.id)
            mplew.write(0)
            mplew.writeMapleAsciiString(newname)
            mplew.write(0)

            return mplew.packet
        }

        fun loadExceptionList(cid: Int, petId: Int, petIdx: Byte, data: List<Int?>): ByteArray {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PET_EXCEPTION_LIST.value)
            mplew.writeInt(cid)
            mplew.write(petIdx)
            mplew.writeLong(petId.toLong())
            mplew.write(data.size)
            for (ids in data) {
                mplew.writeInt(ids!!)
            }

            return mplew.packet
        }

        fun petFoodResponse(cid: Int, index: Byte, success: Boolean, balloonType: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PET_COMMAND.value)
            mplew.writeInt(cid)
            mplew.write(index)
            mplew.write(1)
            mplew.writeBool(success)
            mplew.writeBool(balloonType)

            return mplew.packet
        }

        fun commandResponse(cid: Int, index: Byte, talk: Boolean, animation: Int, balloonType: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PET_COMMAND.value)
            mplew.writeInt(cid)
            mplew.write(index)
            mplew.write(0)
            mplew.write(animation)
            mplew.writeBool(!talk)
            mplew.writeBool(balloonType)

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

        private fun serializeMovementList(lew: LittleEndianWriter, moves: List<LifeMovementFragment?>?) {
            lew.write(moves!!.size)
            for (move in moves) {
                move!!.serialize(lew)
            }
        }
    }
}