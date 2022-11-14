package network.packet.wvscontext

import client.MapleCharacter
import enums.PartyResultType
import net.server.world.MapleParty
import net.server.world.MaplePartyCharacter
import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil

class PartyPacket {

    companion object Packet {

        /**
         * sends party error message to player
         *
         * @param result use PartyResultType
         * @param args if you need to specify ign
         */
        fun onPartyMessage(result: Int, vararg args: String): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PARTY_OPERATION.value)
            mplew.write(result)
            when (result) {
                PartyResultType.UserHasOtherInvite.result,
                PartyResultType.UserDenyInvite.result -> {
                    mplew.writeMapleAsciiString(args[0])
                }
            }
            return mplew.packet
        }

        /**
         * only used for inviting
         * @param chr invitation send from
         * @param result type of invite send (reg or search)
         */
        fun onPartyResult(chr: MapleCharacter, result: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PARTY_OPERATION.value)
            mplew.write(result)
            when (result) {
                PartyResultType.Invite.result -> {
                    mplew.writeInt(chr.party.id) // leader's pt id
                    mplew.writeMapleAsciiString(chr.name) // leader name
                    mplew.write(0)
                }
                PartyResultType.SearchInvite.result -> {
                    mplew.writeInt(chr.party.id)
                    mplew.writeMapleAsciiString("PS: " + chr.name)
                    mplew.write(0)
                }
            }
            return mplew.packet
        }

        /**
         * actions that directly affect the party
         *
         * @param pt party info
         * @param mpc characters within party scope
         * @param result see PartyResultType
         */
        fun onPartyResult(pt: MapleParty, mpc: MaplePartyCharacter, result: Int, channel: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PARTY_OPERATION.value)
            mplew.write(result)
            when (result) {
                PartyResultType.Disband.result,
                PartyResultType.Expel.result,
                PartyResultType.Leave.result -> {
                    mplew.writeInt(pt.id)
                    mplew.writeInt(mpc.id)
                    if (result == PartyResultType.Disband.result) {
                        mplew.write(0);
                        mplew.writeInt(pt.id);
                    } else {
                        mplew.write(1);
                        if (result == PartyResultType.Expel.result) {
                            mplew.write(1);
                        } else {
                            mplew.write(0);
                        }
                        mplew.writeMapleAsciiString(mpc.name);
                        PacketUtil.addPartyStatus(channel, pt, mplew, false);
                    }
                }
                PartyResultType.Join.result -> {
                    mplew.writeInt(pt.id)
                    mplew.writeMapleAsciiString(mpc.name) // user invited
                    PacketUtil.addPartyStatus(channel, pt, mplew, false)
                }
                PartyResultType.SilentUpdate.result -> {
                    mplew.writeInt(pt.id)
                    PacketUtil.addPartyStatus(channel, pt, mplew, false)
                }
                PartyResultType.ChangeLeader.result -> {
                    mplew.writeInt(mpc.id) // new leader id
                    mplew.write(0)
                }
                PartyResultType.TownPortal.result -> {
                    if (mpc.door != null) {
                        mplew.writeInt(mpc.door.town.id)
                        mplew.writeInt(mpc.door.target.id)
                        mplew.writePos(mpc.door.targetPosition)
                    } else {
                        mplew.writeInt(999999999)
                        mplew.writeInt(999999999)
                        mplew.writeInt(0)
                    }
                }
            }
            return mplew.packet
        }

        /**
         * used for when you need to call PartyResultType.SilentUpdate without a reference to MaplePartyCharacter
         * avoids passing a null value
         *
         *
         * @param pt party that is being targeted
         * @param channel id of channel being targeted
         */
        fun onPartySilentUpdate(pt: MapleParty, channel: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PARTY_OPERATION.value)
            mplew.write(PartyResultType.SilentUpdate.result)
            mplew.writeInt(pt.id)
            PacketUtil.addPartyStatus(channel, pt, mplew, false)

            return mplew.packet
        }

        /**
         * MaplePartyCharacter needs to be populated before calling this method
         */
        fun onPartyCreation(party: MaplePartyCharacter): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.PARTY_OPERATION.value)
            mplew.write(PartyResultType.Create.result)
            mplew.writeInt(party.id)
            if (party.door != null) {
                mplew.writeInt(party.door.town.id)
                mplew.writeInt(party.door.target.id)
                mplew.writePos(party.door.targetPosition)
            } else {
                mplew.writeInt(999999999)
                mplew.writeInt(999999999)
                mplew.writeShort(0)
                mplew.writeShort(0)
            }
            return mplew.packet
        }
    }
}