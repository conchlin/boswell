package network.packet

import network.opcode.SendOpcode
import server.life.MapleNPC
import server.life.MaplePlayerNPC
import tools.Pair
import tools.data.input.SeekableLittleEndianAccessor
import tools.data.output.MaplePacketLittleEndianWriter

class NpcPool {

    // naming this packet for readability purposes when called in java
    companion object Packet {

        /** cNpcPool::onPacket **/

        fun spawnNPC(life: MapleNPC): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(24)
            mplew.writeShort(SendOpcode.SPAWN_NPC.value)
            mplew.writeInt(life.objectId)
            mplew.writeInt(life.id)
            mplew.writeShort(life.position.x)
            mplew.writeShort(life.cy)
            if (life.f == 1) {
                mplew.write(0)
            } else {
                mplew.write(1)
            }
            mplew.writeShort(life.fh)
            mplew.writeShort(life.rx0)
            mplew.writeShort(life.rx1)
            mplew.write(1)

            return mplew.packet
        }

        fun removeNPC(objectId: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.REMOVE_NPC.value)
            mplew.writeInt(objectId)

            return mplew.packet
        }

        fun spawnNPCRequestController(life: MapleNPC, MiniMap: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(23)
            mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.value)
            mplew.write(1)
            mplew.writeInt(life.objectId)
            mplew.writeInt(life.id)
            mplew.writeShort(life.position.x)
            mplew.writeShort(life.cy)
            if (life.f == 1) {
                mplew.write(0)
            } else {
                mplew.write(1)
            }
            mplew.writeShort(life.fh)
            mplew.writeShort(life.rx0)
            mplew.writeShort(life.rx1)
            mplew.writeBool(MiniMap)

            return mplew.packet
        }

        fun removeNPCController(objectid: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.value)
            mplew.write(0)
            mplew.writeInt(objectid)

            return mplew.packet
        }

        fun spawnPlayerNPC(npc: MaplePlayerNPC): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.value)
            mplew.write(1)
            mplew.writeInt(npc.objectId)
            mplew.writeInt(npc.scriptId)
            mplew.writeShort(npc.position.x)
            mplew.writeShort(npc.cy)
            mplew.write(npc.direction)
            mplew.writeShort(npc.fh)
            mplew.writeShort(npc.rX0)
            mplew.writeShort(npc.rX1)
            mplew.write(1)

            return mplew.packet
        }

        /** cNpcPool::onNpcPacket **/

        //npc_action is handled in NPCAnimationHandler

        /**
         * the following need to be implemented
         * NPC_UPDATE_LIMITED_INFO(261),  //OnUpdateLimitedInfo(261)
         */

        /**
         * use this to call a linked npc special action from npc.wz/info/link (?)
         *
         * @param objectId object id of npc with linked special action
         * @param action name of action
         */
        fun onSetSpecialAction(objectId: Int, action: String): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.NPC_SET_SPECIAL_ACTION.value)
            mplew.writeInt(objectId)
            mplew.writeAsciiString(action)

            return mplew.packet
        }

        fun setNPCScriptable(scriptNpcDescriptions: Set<Pair<Int?, String?>>): ByteArray? { // thanks to GabrielSin
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SET_NPC_SCRIPTABLE.value)
            mplew.write(scriptNpcDescriptions.size)
            for (p in scriptNpcDescriptions) {
                mplew.writeInt(p.getLeft()!!)
                mplew.writeMapleAsciiString(p.getRight())
                mplew.writeInt(0) // start time
                mplew.writeInt(Int.MAX_VALUE) // end time
            }

            return mplew.packet
        }
    }
}