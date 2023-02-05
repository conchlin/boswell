package network.packet.context

import client.BuddylistEntry
import enums.FriendResultType
import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil

class FriendPacket {
    companion object Packet {

        /**
         * packet responsible for actions pertaining to the friend/buddy system
         *
         * @param type see FriendResultType.kt
         * @param userId
         * @param args see method comments
         */
        fun onFriendResult(type: Int, userId: Int, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FriendResult.value)
            mplew.write(type)
            when (type) {
                FriendResultType.ChannelChange.type -> {
                    mplew.writeInt(userId)
                    mplew.write(0)
                    mplew.writeInt(args[0]) // channel id
                }
                FriendResultType.CapacityChange.type -> {
                    mplew.write(args[0]) // new capacity amount
                }
            }

            return mplew.packet
        }

        /**
         * packet responsible for actions pertaining to the friend/buddy system
         * This method is used to specifically handle any friend list modifications
         *
         * @param type see FriendResultType.kt
         * @param friendList the newly modified collection of friend entries
         */
        fun onFriendResult(type: Int, friendList: Collection<BuddylistEntry>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FriendResult.value)
            mplew.write(type)
            when (type) {
                FriendResultType.UpdateList.type -> {
                    mplew.write(friendList.size)
                    for (friend in friendList) {
                        if (friend.isVisible) {
                            mplew.writeInt(friend.characterId)
                            mplew.writeAsciiString(PacketUtil.getRightPaddedStr(friend.name, '\u0000', 13))
                            mplew.write(0)
                            mplew.writeInt(friend.channel - 1)
                            mplew.writeAsciiString(PacketUtil.getRightPaddedStr(friend.group, '\u0000', 13))
                            mplew.writeInt(0)
                        }
                    }
                    for (x in friendList.indices) {
                        mplew.writeInt(0)
                    }
                }
            }

            return mplew.packet
        }

        /**
         * packet responsible for actions pertaining to the friend/buddy system
         * this method is only used for sending add requests to users
         *
         * @param type see FriendResultType.kt
         * @param senderId user sending the add request
         * @param receiverId user receiving add request
         * @param senderName user sending the add request (name)
         */
        fun onFriendResult(type: Int, senderId: Int, receiverId: Int, senderName: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FriendResult.value)
            mplew.write(type)
            when (type) {
                FriendResultType.AddFriend.type -> {
                    mplew.writeInt(senderId)
                    mplew.writeMapleAsciiString(senderName)
                    mplew.writeInt(senderId)
                    mplew.writeAsciiString(PacketUtil.getRightPaddedStr(senderName, '\u0000', 11))
                    mplew.write(9)
                    mplew.write(240)
                    mplew.write(1)
                    mplew.writeInt(15)
                    mplew.writeNullTerminatedAsciiString("Default Group")
                    mplew.writeInt(receiverId)
                }
            }
            return mplew.packet
        }
    }
}