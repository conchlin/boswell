package network.packet

import client.MapleCharacter
import client.inventory.Item
import enums.CashItemResultType
import network.opcode.SendOpcode
import server.cashshop.CashItem
import tools.MaplePacketCreator
import tools.Pair
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil

class CCashShop {

    companion object Packet {

        /**
         * packet responsible for handling cash transactions within the cash shop
         *
         * @param user the user being affected
         */
        fun onQueryCashResult(user: MapleCharacter): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.QueryCashResult.value)
            mplew.writeInt(user.cashShop.getCash(1))
            mplew.writeInt(user.cashShop.getCash(2))
            mplew.writeInt(user.cashShop.getCash(4))
            return mplew.packet
        }

        /**
         * Packet responsible for handling the various CS actions
         *
         * @param result see CashItemResultType
         * @param user MapleCharacter instance
         * @param args see method comments for value description
         */
        fun onCashItemResult(result: Int, user: MapleCharacter, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CashItemResult.value)
            mplew.write(result)
            when (result) {
                CashItemResultType.LoadInventory.result -> {
                    mplew.writeShort(user.cashShop.inventory.size)
                    for (item in user.cashShop.inventory) {
                        MaplePacketCreator.addCashItemInformation(mplew, item, user.client.accID)
                    }
                    mplew.writeShort(user.storage.slots.toInt())
                    mplew.writeShort(user.client.characterSlots.toInt())
                }
                CashItemResultType.LoadWishList.result,
                CashItemResultType.UpdateWishlist.result -> {
                    for (sn in user.cashShop.wishList) {
                        mplew.writeInt(sn) //serial number
                    }
                    for (i in user.cashShop.wishList.size..9) {
                        mplew.writeInt(0)
                    }
                }
                CashItemResultType.SlotInventoryPurchase.result -> {
                    mplew.write(args[0]) //inventory type
                    mplew.writeShort(args[1]) //slot amount
                }
                CashItemResultType.SlotStoragePurchase.result,
                CashItemResultType.SlotCharacterPurchase.result -> {
                    mplew.writeShort(args[0]) //slot amount
                }
                CashItemResultType.ItemQuest.result -> {
                    mplew.writeInt(1)
                    mplew.writeShort(1)
                    mplew.write(11)
                    mplew.write(0)
                    mplew.writeInt(args[0]) // item id
                }
            }
            return mplew.packet
        }

        /**
         * Packet responsible for handling the various CS actions
         * use this specific method when you need an Item instance
         *
         * @param result see CashItemResultType
         * @param item Item instance of item being created
         * @param args see comments
         */
        fun onCashItemResult(result: Int, item: Item, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CashItemResult.value)
            mplew.write(result)
            when (result) {
                CashItemResultType.ItemPurchase.result,
                CashItemResultType.ItemDeposit.result -> {
                    MaplePacketCreator.addCashItemInformation(mplew, item, args[0]) //account id
                }
                CashItemResultType.ItemRetrieve.result -> {
                    mplew.writeShort(item.position.toInt())
                    PacketUtil.addItemInfoZeroPos(mplew, item)
                }
            }
            return mplew.packet
        }

        /**
         * Packet responsible for handling the various CS actions
         * This method specifically handles cash packages/bundles
         *
         * @param result see CashItemResultType
         * @param cashPackage list of items included in the package
         * @param accountId
         */
        fun onCashItemResult(result: Int, cashPackage: List<Item?>, accountId: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CashItemResult.value)
            mplew.write(result)
            when (result) {
                CashItemResultType.ItemPackage.result -> {
                    mplew.write(cashPackage.size)
                    for (item in cashPackage) {
                        MaplePacketCreator.addCashItemInformation(mplew, item, accountId)
                    }
                    mplew.writeShort(0)
                }
            }
            return mplew.packet
        }

        /**
         * Packet responsible for handling the various CS actions
         * This method specifically handles loading of gifts
         *
         * @param result see CashItemResultType
         * @param gifts paired list to load
         */
        fun onCashItemResult(result: Int, gifts: List<Pair<Item, String>>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CashItemResult.value)
            mplew.write(result)
            when (result) {
                CashItemResultType.LoadGifts.result -> {
                    mplew.writeShort(gifts.size)

                    for (gift in gifts) {
                        MaplePacketCreator.addCashItemInformation(mplew, gift.getLeft(), 0, gift.getRight())
                    }
                }
            }
            return mplew.packet
        }

        /**
         * Packet responsible for handling the various CS actions
         * This method specifically handles loading of gifts
         *
         * @param result see CashItemResultType
         * @param item CashItem instance of item being sent as gift
         * @param msg gift message
         */
        fun onCashItemResult(result: Int, item: CashItem, msg: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CashItemResult.value)
            mplew.write(result)
            when (result) {
                CashItemResultType.ItemGift.result -> {
                    mplew.writeMapleAsciiString(msg)
                    mplew.writeInt(item.itemId)
                    mplew.writeShort(item.count.toInt())
                    mplew.writeInt(item.price)
                }
            }
            return mplew.packet
        }

        /**
         * Handles messaging associated with the cash shop (error, success, etc)
         *
         *          * 00 = Due to an unknown error, failed
         *          * A4 = Due to an unknown error, failed + warpout
         *          * A5 = You don't have enough cash.
         *          * A6 = long as shet msg
         *          * A7 = You have exceeded the allotted limit of price for gifts.
         *          * A8 = You cannot send a gift to your own account. Log in on the char and purchase
         *          * A9 = Please confirm whether the character's name is correct.
         *          * AA = Gender restriction!
         *          * AB = gift cannot be sent because recipient inv is full
         *          * AC = exceeded the number of cash items you can have
         *          * AD = check and see if the character name is wrong or there is gender restrictions
         *          * //Skipped a few
         *          * B0 = Wrong Coupon Code
         *          * B1 = Disconnect from CS because of 3 wrong coupon codes < lol
         *          * B2 = Expired Coupon
         *          * B3 = Coupon has been used already
         *          * B4 = Nexon internet café
         *          * BB = inv full
         *          * BC = long as shet "(not?) available to purchase by a use at the premium" msg
         *          * BD = invalid gift recipient
         *          * BE = invalid receiver name
         *          * BF = item unavailable to purchase at this hour
         *          * C0 = not enough items in stock, therefore not available
         *          * C1 = you have exceeded spending limit of NX
         *          * C2 = not enough mesos? Lol not even 1 mesos xD
         *          * C3 = cash shop unavailable during beta phase
         *          * C4 = check birthday code
         *          * C7 = only available to users buying cash item, whatever msg too long
         *          * C8 = already applied for this
         *          * D2 = coupon system currently unavailable
         *          * D3 = item can only be used 15 days after registration
         *          * D4 = not enough gift tokens
         *          * D6 = fresh people cannot gift items lul
         *          * D7 = bad people cannot gift items >:(
         *          * D8 = cannot gift due to limitations
         *          * D9 = cannot gift due to amount of gifted times
         *          * DA = cannot be gifted due to technical difficulties
         *          * DB = cannot transfer to char below level 20
         *          * DC = cannot transfer char to same world
         *          * DD = cannot transfer char to new server world
         *          * DE = cannot transfer char out of this world
         *          * DF = cannot transfer char due to no empty char slots
         *          * E0 = event or free test time ended
         *          * E6 = item cannot be purchased with MaplePoints
         *          * E7 = lol sorry for the inconvenience, eh?
         *          * E8 = cannot be purchased by anyone under 7
         */
        fun onCashItemResultMessage(msg: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CashItemResult.value)
            mplew.write(92)
            mplew.write(msg)

            return mplew.packet
        }

        /**
         * packet responsible for checking ID eligibility when performing a character name change
         *
         * @param canUseName is name already in use or not
         */
        fun onCheckDuplicatedIDResult(canUseName: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CashShopCheckDuplicatedIDResult.value)
            mplew.writeShort(0)
            mplew.writeBool(!canUseName)
            return mplew.packet
        }

        /**
         * packet responsible for checking username availability when performing a character name change
         *
         *     /*  1: name change already submitted
         *             2: name change within a month
         *             3: recently banned
         *             4: unknown error
         *      */
         */
        fun onCheckNameChangePossibleResult(error: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CheckNameChangePossibleResult.value)
            mplew.writeInt(0)
            mplew.write(error)
            mplew.writeInt(0)
            return mplew.packet
        }

        /**
         * packet responsible for error handling when trying to character transfer into as different world
         *
         *     /*  1: cannot find char info,
         *             2: cannot transfer under 20,
         *             3: cannot send banned,
         *             4: cannot send married,
         *             5: cannot send guild leader,
         *             6: cannot send if account already requested transfer,
         *             7: cannot transfer within 30days,
         *             8: must quit family,
         *             9: unknown error
         *      */
         */
        fun onCheckTransferWorldPossibleResult(error: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CheckTransferWorldPossibleResult.value)
            mplew.writeInt(0)
            mplew.write(0)
            mplew.write(error)
            mplew.writeInt(0)
            return mplew.packet
        }


        /*fun onCashItemGachaponOpenFailed(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CashItemGachaponResult.value)
            mplew.write(189)
            return mplew.packet
        }

        fun onCashGachaponOpenSuccess(
            accountid: Int,
            sn: Long,
            remainingBoxes: Int,
            item: Item?,
            itemid: Int,
            nSelectedItemCount: Int,
            bJackpot: Boolean
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CashItemGachaponResult.value)
            mplew.write(190)
            mplew.writeLong(sn) // sn of the box used
            mplew.writeInt(remainingBoxes)
            MaplePacketCreator.addCashItemInformation(mplew, item, accountid)
            mplew.writeInt(itemid) // the itemid of the liSN?
            mplew.write(nSelectedItemCount) // the total count now? o.O
            mplew.writeBool(bJackpot) // "CashGachaponJackpot"
            return mplew.packet
        }*/
    }
}