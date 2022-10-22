package network.packet

import client.MapleCharacter
import client.MapleClient
import constants.ServerConstants
import net.server.Server
import net.server.channel.Channel
import network.opcode.SendOpcode
import tools.Pair
import tools.Randomizer
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil
import java.net.InetAddress

class CLogin {

    companion object Packet {

        // use LoginResultType for reason
        fun getLoginFailed(reason: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(8)
            mplew.writeShort(SendOpcode.LOGIN_STATUS.value)
            mplew.write(reason)
            mplew.write(0)
            mplew.writeInt(0)

            return mplew.packet
        }

        fun getPermBan(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.LOGIN_STATUS.value)
            mplew.write(2) // Account is banned
            mplew.write(0)
            mplew.writeInt(0)
            mplew.write(0)
            mplew.writeLong(PacketUtil.getTime(-1))

            return mplew.packet
        }

        fun getTempBan(timestampTill: Long, reason: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(17)
            mplew.writeShort(SendOpcode.LOGIN_STATUS.value)
            mplew.write(2)
            mplew.write(0)
            mplew.writeInt(0)
            mplew.write(reason)
            // Tempban date is handled as a 64-bit long, number of 100NS intervals since 1/1/1601. Lulz.
            mplew.writeLong(PacketUtil.getTime(timestampTill))

            return mplew.packet
        }

        /**
         * Gets a successful authentication packet.
         *
         * @param c
         * @return the successful authentication packet
         */
        fun getAuthSuccess(c: MapleClient): ByteArray? {
            // locks the login session until data is recovered from the cache or the DB.
            Server.getInstance().loadAccountCharacters(c)

            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.LOGIN_STATUS.value)
            mplew.writeInt(0)
            mplew.writeShort(0)
            mplew.writeInt(c.accID)
            mplew.write(c.gender)
            val canFly = Server.getInstance().canFly(c.accID)
            mplew.writeBool(if (ServerConstants.USE_ENFORCE_ADMIN_ACCOUNT || canFly) c.gmLevel > 1 else false)
            mplew.write(if ((ServerConstants.USE_ENFORCE_ADMIN_ACCOUNT || canFly) && c.gmLevel > 1) 0x80 else 0)
            mplew.write(0) // Country Code.
            mplew.writeMapleAsciiString(c.accountName)
            mplew.write(0)
            mplew.write(0) // IsQuietBan
            mplew.writeLong(0) //IsQuietBanTimeStamp
            mplew.writeLong(0) //CreationTimeStamp
            mplew.writeInt(1) // 1: Remove the "Select the world you want to play in"
            mplew.write(1) // 0 = Pin-System Enabled, 1 = Disabled
            mplew.write(if (c.pic == null || c.pic == "") 0 else 1) // 0 = Register PIC, 1 = Ask for PIC, 2 = Disabled

            return mplew.packet
        }

        fun sendGuestTOS(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GUEST_ID_LOGIN.value)
            mplew.writeShort(0x100)
            mplew.writeInt(Randomizer.nextInt(999999))
            mplew.writeLong(0)
            mplew.writeLong(PacketUtil.getTime(-2))
            mplew.writeLong(PacketUtil.getTime(System.currentTimeMillis()))
            mplew.writeInt(0)
            mplew.writeMapleAsciiString("http://playboswell.com")

            return mplew.packet
        }

        /**
         * Gets a packet detailing a server status message.
         *
         * Possible values for `status`:<br></br> 0 - Normal<br></br> 1 - Highly
         * populated<br></br> 2 - Full
         *
         * @param status The server status.
         * @return The server status packet.
         */
        fun getServerStatus(status: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(4)
            mplew.writeShort(SendOpcode.SERVERSTATUS.value)
            mplew.writeShort(status)

            return mplew.packet
        }

        /**
         * @param mode use PinCodeResultType.kt enum
         * @return
         */
        fun onCheckPinCodeResult(mode: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(3)
            mplew.writeShort(SendOpcode.CHECK_PINCODE.value)
            mplew.write(mode)

            return mplew.packet
        }

        fun onUpdatePinCodeResult(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(3)
            mplew.writeShort(SendOpcode.UPDATE_PINCODE.value)
            mplew.write(0)

            return mplew.packet
        }

        fun showAllCharacter(chars: Int, unk: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(11)
            mplew.writeShort(SendOpcode.VIEW_ALL_CHAR.value)
            // 2: already connected to server, 3 : unk error (view-all-characters), 5 : cannot find any
            mplew.write(if (chars > 0) 1 else 5)
            mplew.writeInt(chars)
            mplew.writeInt(unk)

            return mplew.packet
        }

        fun showAllCharacterInfo(worldid: Int, chars: List<MapleCharacter?>, usePic: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.VIEW_ALL_CHAR.value)
            mplew.write(0)
            mplew.write(worldid)
            mplew.write(chars.size)
            for (chr in chars) {
                PacketUtil.addCharEntry(mplew, chr, true)
            }
            mplew.write(if (usePic) 1 else 2)

            return mplew.packet
        }

        // use LoginResultType for reason value
        fun getAfterLoginError(reason: Int): ByteArray? { //same as above o.o
            val mplew = MaplePacketLittleEndianWriter(8)
            mplew.writeShort(SendOpcode.SELECT_CHARACTER_BY_VAC.value)
            mplew.writeShort(reason) //using other types than stated above = CRASH

            return mplew.packet
        }

        /**
         * Gets a packet detailing a server and its channels.
         *
         * @param serverId
         * @param serverName The name of the server.
         * @param flag
         * @param eventmsg
         * @param channelLoad Load of the channel - 1200 seems to be max.
         * @return The server info packet.
         */
        fun getServerList(
            serverId: Int,
            serverName: String,
            flag: Int,
            eventmsg: String?,
            channelLoad: List<Channel>
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SERVERLIST.value)
            mplew.write(serverId)
            mplew.writeMapleAsciiString(serverName)
            mplew.write(flag)
            mplew.writeMapleAsciiString(eventmsg)
            mplew.write(100) // rate modifier, don't ask O.O!
            mplew.write(0) // event xp * 2.6 O.O!
            mplew.write(100) // rate modifier, don't ask O.O!
            mplew.write(0) // drop rate * 2.6
            mplew.write(0)
            mplew.write(channelLoad.size)
            for (ch in channelLoad) {
                mplew.writeMapleAsciiString(serverName + "-" + ch.id)
                mplew.writeInt(ch.channelCapacity)

                // thanks GabrielSin for this channel packet structure part
                mplew.write(1) // nWorldID
                mplew.write(ch.id - 1) // nChannelID
                mplew.writeBool(false) // bAdultChannel
            }
            mplew.writeShort(0)

            return mplew.packet
        }

        /**
         * Gets a packet saying that the server list is over.
         *
         * @return The end of server list packet.
         */
        fun getEndOfServerList(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(3)
            mplew.writeShort(SendOpcode.SERVERLIST.value)
            mplew.write(0xFF)

            return mplew.packet
        }

        /**
         * Gets a packet with a list of characters.
         *
         * @param c The MapleClient to load characters of.
         * @param serverId The ID of the server requested.
         * @return The character list packet.
         */
        fun getCharList(c: MapleClient, serverId: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CHARLIST.value)
            mplew.write(0) // status
            val chars = c.loadCharacters(serverId)
            mplew.write(chars.size.toByte())
            for (chr in chars) {
                PacketUtil.addCharEntry(mplew, chr, false)
            }
            mplew.write(if (c.pic == null || c.pic == "") 0 else 1) // 0 = Register PIC, 1 = Ask for PIC, 2 = Disabled
            mplew.writeInt(c.characterSlots.toInt())

            return mplew.packet
        }

        /**
         * Gets a packet telling the client the IP of the channel server.
         *
         * @param inetAddr The InetAddress of the requested channel server.
         * @param port The port the channel is on.
         * @param clientId The ID of the client.
         * @return The server IP packet.
         */
        fun getServerIP(inetAddr: InetAddress, port: Int, clientId: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SERVER_IP.value)
            mplew.writeShort(0)
            val addr = inetAddr.address
            mplew.write(addr)
            mplew.writeShort(port)
            mplew.writeInt(clientId)
            mplew.write(byteArrayOf(0, 0, 0, 0, 0))
            return mplew.packet
        }

        fun onCheckDuplicatedIDResult(charname: String?, nameUsed: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CHAR_NAME_RESPONSE.value)
            mplew.writeMapleAsciiString(charname)
            mplew.write(if (nameUsed) 1 else 0)

            return mplew.packet
        }

        fun addNewCharEntry(chr: MapleCharacter?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ADD_NEW_CHAR_ENTRY.value)
            mplew.write(0)
            PacketUtil.addCharEntry(mplew, chr, false)

            return mplew.packet
        }

        /**
         * @param cid character id
         * @param state use CharDeleteResultType
         * @return
         */
        fun deleteCharResponse(cid: Int, state: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.DELETE_CHAR_RESPONSE.value)
            mplew.writeInt(cid)
            mplew.write(state)

            return mplew.packet
        }

        /**
         * Gets a packet telling the client the IP of the new channel.
         *
         * @param inetAddr The InetAddress of the requested channel server.
         * @param port The port the channel is on.
         * @return The server IP packet.
         */
        fun getChannelChange(inetAddr: InetAddress, port: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CHANGE_CHANNEL.value)
            mplew.write(1)
            val addr = inetAddr.address
            mplew.write(addr)
            mplew.writeShort(port)

            return mplew.packet
        }

        fun getPing(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(2)
            mplew.writeShort(SendOpcode.PING.value)

            return mplew.packet
        }

        fun getRelogResponse(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(3)
            mplew.writeShort(SendOpcode.RELOG_RESPONSE.value)
            mplew.write(1) //1 O.O Must be more types ):

            return mplew.packet
        }

        fun selectWorld(world: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.LAST_CONNECTED_WORLD.value)
            //According to GMS, it should be the world that contains the most characters (most active)
            mplew.writeInt(world)

            return mplew.packet
        }

        fun sendRecommended(worlds: List<Pair<Int?, String?>>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.RECOMMENDED_WORLD_MESSAGE.value)
            mplew.write(worlds.size) //size
            for (world in worlds) {
                mplew.writeInt(world.getLeft()!!)
                mplew.writeMapleAsciiString(world.getRight())
            }

            return mplew.packet
        }

        fun wrongPic(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(3)
            mplew.writeShort(SendOpcode.CHECK_SPW_RESULT.value)
            mplew.write(0)

            return mplew.packet
        }
    }
}