package network.packet

import client.MapleCharacter
import client.MapleClient
import client.inventory.MapleInventoryType
import constants.ItemConstants
import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil

class UserPool {

    companion object Packet {

        /**
         * Gets a packet spawning a player as a mapobject to other clients.
         *
         * @param target The client receiving this packet.
         * @param chr The character to spawn to other clients.
         * the map or already is.
         * @return The spawn player packet.
         */
        fun onUserEnterField(target: MapleClient?, chr: MapleCharacter): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SPAWN_PLAYER.value)
            mplew.writeInt(chr.id)
            mplew.write(chr.level) //v83
            mplew.writeMapleAsciiString(chr.name)
            if (chr.guildId < 1) {
                mplew.writeMapleAsciiString("")
                mplew.write(ByteArray(6))
            } else {
                val gs = chr.client.worldServer.getGuildSummary(chr.guildId, chr.world)
                if (gs != null) {
                    mplew.writeMapleAsciiString(gs.name)
                    mplew.writeShort(gs.logoBG.toInt())
                    mplew.write(gs.logoBGColor)
                    mplew.writeShort(gs.logo.toInt())
                    mplew.write(gs.logoColor)
                } else {
                    mplew.writeMapleAsciiString("")
                    mplew.write(ByteArray(6))
                }
            }
            PacketUtil.writeForeignBuffs(mplew, chr)
            mplew.writeShort(chr.job.id)

            /* replace "mplew.writeShort(chr.getJob().getId())" with this snippet for 3rd person FJ animation on all classes
                if (chr.getJob().isA(MapleJob.HERMIT) || chr.getJob().isA(MapleJob.DAWNWARRIOR2) || chr.getJob().isA(MapleJob.NIGHTWALKER2)) {
			mplew.writeShort(chr.getJob().getId());
                } else {
			mplew.writeShort(412);
                }*/
            PacketUtil.addCharLook(mplew, chr, false)
            mplew.writeInt(chr.getInventory(MapleInventoryType.CASH).countById(5110000))
            mplew.writeInt(chr.itemEffect)
            mplew.writeInt(if (ItemConstants.getInventoryType(chr.chair) == MapleInventoryType.SETUP) chr.chair else 0)
            mplew.writePos(chr.position)
            mplew.write(chr.stance)
            mplew.writeShort(chr.fh)
            mplew.write(0) // admin byte
            val pet = chr.pets
            for (i in 0..2) {
                if (pet[i] != null) {
                    PacketUtil.addPetInfo(mplew, pet[i], false)
                }
            }
            mplew.write(0) //end of pets
            if (chr.mount == null) {
                mplew.writeInt(1) // mob level
                mplew.writeLong(0) // mob exp + tiredness
            } else {
                mplew.writeInt(chr.mount.level)
                mplew.writeInt(chr.mount.exp)
                mplew.writeInt(chr.mount.tiredness)
            }
            val mps = chr.playerShop
            if (mps != null && mps.isOwner(chr)) {
                if (mps.hasFreeSlot()) {
                    PacketUtil.addAnnounceBox(mplew, mps, mps.visitors.size)
                } else {
                    PacketUtil.addAnnounceBox(mplew, mps, 1)
                }
            } else {
                val miniGame = chr.miniGame
                if (miniGame != null && miniGame.isOwner(chr)) {
                    if (miniGame.hasFreeSlot()) {
                        PacketUtil.addAnnounceBox(mplew, miniGame, 1, 0)
                    } else {
                        PacketUtil.addAnnounceBox(mplew, miniGame, 2, if (miniGame.isMatchInProgress) 1 else 0)
                    }
                } else {
                    mplew.write(0)
                }
            }
            if (chr.chalkboard != null) {
                mplew.write(1)
                mplew.writeMapleAsciiString(chr.chalkboard)
            } else {
                mplew.write(0)
            }
            PacketUtil.addRingLook(mplew, chr, true) // crush
            PacketUtil.addRingLook(mplew, chr, false) // friendship
            PacketUtil.addMarriageRingLook(target, mplew, chr)
            /* above
           if ( CInPacket::Decode1(a2) ) {
		    v39 = CInPacket::Decode4(a2);
		    if ( v39 > 0 )
		    {
		      v40 = v39;
		      do
		      {
		        v41 = CInPacket::Decode4(a2);
		        CUserPool::OnNewYearCardRecordAdd(0, v5, v41);
		        --v40;
		      }
		      while ( v40 );
		    }
		  }*/
            PacketUtil.encodeNewYearCardInfo(mplew, chr) // new year seems to crash sometimes...
            mplew.skip(2)
            mplew.write(chr.team) //only needed in specific fields

            return mplew.packet
        }

        fun onUserLeaveField(cid: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.REMOVE_PLAYER_FROM_MAP.value)
            mplew.writeInt(cid)

            return mplew.packet
        }
    }
}