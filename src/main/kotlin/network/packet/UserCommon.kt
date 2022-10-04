package network.packet

import client.MapleCharacter
import client.inventory.Equip.ScrollResult
import network.opcode.SendOpcode
import server.maps.MapleMiniGame
import server.maps.MaplePlayerShop
import tools.data.output.MaplePacketLittleEndianWriter

class UserCommon {

    companion object Packet {

        fun onUserChat(cidfrom: Int, text: String?, gm: Boolean, show: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CHATTEXT.value)
            mplew.writeInt(cidfrom)
            mplew.writeBool(gm)
            mplew.writeMapleAsciiString(text)
            mplew.write(show)

            return mplew.packet
        }

        fun onADBoard(chr: MapleCharacter, close: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CHALKBOARD.value)
            mplew.writeInt(chr.id)
            if (close) {
                mplew.write(0)
            } else {
                mplew.write(1)
                mplew.writeMapleAsciiString(chr.chalkboard)
            }

            return mplew.packet
        }

        /** begin of onMiniRoomBalloon() **/
        fun updatePlayerShopBox(shop: MaplePlayerShop): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.value)
            mplew.writeInt(shop.owner.id)
            updatePlayerShopBoxInfo(mplew, shop)

            return mplew.packet
        }

        fun removePlayerShopBox(shop: MaplePlayerShop): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(7)
            mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.value)
            mplew.writeInt(shop.owner.id)
            mplew.write(0)

            return mplew.packet
        }

        private fun updatePlayerShopBoxInfo(mplew: MaplePacketLittleEndianWriter, shop: MaplePlayerShop) {
            val roomInfo = shop.shopRoomInfo
            mplew.write(4)
            mplew.writeInt(shop.objectId)
            mplew.writeMapleAsciiString(shop.description)
            mplew.write(0) // pw
            mplew.write(shop.itemId % 100)
            mplew.write(roomInfo[0]) // curPlayers
            mplew.write(roomInfo[1]) // maxPlayers
            mplew.write(0)
        }

        fun addOmokBox(c: MapleCharacter, ammount: Int, type: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.value)
            mplew.writeInt(c.id)
            addAnnounceBox(mplew, c.miniGame, ammount, type)

            return mplew.packet
        }

        fun addMatchCardBox(c: MapleCharacter, ammount: Int, type: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.value)
            mplew.writeInt(c.id)
            addAnnounceBox(mplew, c.miniGame, ammount, type)

            return mplew.packet
        }

        private fun addAnnounceBox(
            mplew: MaplePacketLittleEndianWriter,
            game: MapleMiniGame,
            ammount: Int,
            joinable: Int
        ) {
            mplew.write(game.gameType.value)
            mplew.writeInt(game.objectId) // gameid/shopid
            mplew.writeMapleAsciiString(game.description) // desc
            mplew.writeBool(game.password.isNotEmpty()) // password here, thanks GabrielSin!
            mplew.write(game.pieceType)
            mplew.write(ammount)
            mplew.write(2) //player capacity
            mplew.write(joinable)
        }

        fun removeMinigameBox(chr: MapleCharacter): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(7)
            mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.value)
            mplew.writeInt(chr.id)
            mplew.write(0)

            return mplew.packet
        }
        /** end of onMiniRoomBalloon() **/

        fun onShowItemUpgradeEffect(
            chr: Int,
            scrollSuccess: ScrollResult,
            legendarySpirit: Boolean,
            whiteScroll: Boolean
        ): ByteArray? {   // thanks to Rien dev team
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SHOW_SCROLL_EFFECT.value)
            mplew.writeInt(chr)
            mplew.writeBool(scrollSuccess == ScrollResult.SUCCESS)
            mplew.writeBool(scrollSuccess == ScrollResult.CURSE)
            mplew.writeBool(legendarySpirit)
            mplew.writeBool(whiteScroll)
            return mplew.packet
        }
    }
}