/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.manipulator.MapleInventoryManipulator;
import client.inventory.manipulator.MapleKarmaManipulator;
import constants.ItemConstants;
import constants.ServerConstants;

import enums.BroadcastMessageType;
import net.AbstractMaplePacketHandler;
import network.packet.EmployeePool;
import network.packet.UserCommon;
import network.packet.context.BroadcastMsgPacket;
import network.packet.context.WvsContext;
import server.MapleItemInformationProvider;
import server.MapleTrade;
import constants.GameConstants;
import server.maps.FieldLimit;
import server.maps.MapleHiredMerchant;
import server.maps.MapleMapObject;
import server.maps.MapleMiniGame;
import server.maps.MapleMiniGame.MiniGameType;
import server.maps.MaplePlayerShop;
import server.maps.MaplePlayerShopItem;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.Point;
import java.sql.SQLException;

/**
 *
 * @author Matze
 * @author Ronan - concurrency safety & reviewed minigames
 */
public final class PlayerInteractionHandler extends AbstractMaplePacketHandler {
    public enum Action {
        CREATE(0),
        INVITE(2),
        DECLINE(3),
        VISIT(4),
        ROOM(5),
        CHAT(6),
        CHAT_THING(8),
        EXIT(0xA),
        OPEN_STORE(0xB),
        OPEN_CASH(0xE),
        SET_ITEMS(0xF),
        SET_MESO(0x10),
        CONFIRM(0x11),
        TRANSACTION(0x14),
        ADD_ITEM(0x16),
        BUY(0x17),
        UPDATE_MERCHANT(0x19),
        UPDATE_PLAYERSHOP(0x1A),
        REMOVE_ITEM(0x1B),
        BAN_PLAYER(0x1C),
        MERCHANT_THING(0x1D),
        OPEN_THING(0x1E),
        PUT_ITEM(0x21),
        MERCHANT_BUY(0x22),
        TAKE_ITEM_BACK(0x26),
        MAINTENANCE_OFF(0x27),
        MERCHANT_ORGANIZE(0x28),
        CLOSE_MERCHANT(0x29),
        REAL_CLOSE_MERCHANT(0x2A),
        MERCHANT_MESO(0x2B),
        SOMETHING(0x2D),
        VIEW_VISITORS(0x2E),
        BLACKLIST(0x2F),
        REQUEST_TIE(0x32),
        ANSWER_TIE(0x33),
        GIVE_UP(0x34),
        EXIT_AFTER_GAME(0x38),
        CANCEL_EXIT_AFTER_GAME(0x39),
        READY(0x3A),
        UN_READY(0x3B),
        EXPEL(0x3C),
        START(0x3D),
        GET_RESULT(0x3E),
        SKIP(0x3F),
        MOVE_OMOK(0x40),
        SELECT_CARD(0x44);
        final byte code;

        private Action(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
        }
    }

    private static int establishMiniroomStatus(MapleCharacter chr, boolean isMinigame) {
        if (isMinigame && FieldLimit.CANNOTMINIGAME.check(chr.getMap().getFieldLimit())) {
            return 11;
        }

        if (chr.getChalkboard() != null) {
            return 13;
        }

        if(chr.getEventInstance() != null) {
            return 5;
        }
        
        return 0;
    }
    
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.tryacquireClient()) {    // thanks GabrielSin for pointing dupes within player interactions
            c.announce(WvsContext.Packet.enableActions());
            return;
        }
        
        try {
            byte mode = slea.readByte();
            final MapleCharacter chr = c.getPlayer();
            
            if (mode == Action.CREATE.getCode()) {
                if(!chr.isAlive()) {    // thanks GabrielSin for pointing this
                    chr.getClient().announce(MaplePacketCreator.getMiniRoomError(4));
                    return;
                }

                byte createType = slea.readByte();
                if (createType == 3) {  // trade
                    MapleTrade.startTrade(chr);
                } else if (createType == 1) { // omok mini game
                    int status = establishMiniroomStatus(chr, true);
                    if (status > 0) {
                        chr.getClient().announce(MaplePacketCreator.getMiniRoomError(status));
                        return;
                    }

                    String desc = slea.readMapleAsciiString();
                    String pw;

                    if (slea.readByte() != 0) {
                        pw = slea.readMapleAsciiString();
                    } else {
                        pw = "";
                    }

                    int type = slea.readByte();
                    if (type > 11) {
                        type = 11;
                    } else if (type < 0) {
                        type = 0;
                    }
                    if (!chr.haveItem(4080000 + type)) {
                        chr.getClient().announce(MaplePacketCreator.getMiniRoomError(6));
                        return;
                    }

                    MapleMiniGame game = new MapleMiniGame(chr, desc, pw);
                    chr.setMiniGame(game);
                    game.setPieceType(type);
                    game.setGameType(MiniGameType.OMOK);
                    chr.getMap().addMapObject(game);
                    chr.getMap().broadcastMessage(UserCommon.Packet.addOmokBox(chr, 1, 0));
                    game.sendOmok(c, type);
                } else if (createType == 2) { // matchcard
                    int status = establishMiniroomStatus(chr, true);
                    if (status > 0) {
                        chr.getClient().announce(MaplePacketCreator.getMiniRoomError(status));
                        return;
                    }

                    String desc = slea.readMapleAsciiString();
                    String pw;

                    if (slea.readByte() != 0) {
                        pw = slea.readMapleAsciiString();
                    } else {
                        pw = "";
                    }

                    int type = slea.readByte();
                    if (type > 2) {
                        type = 2;
                    } else if (type < 0) {
                        type = 0;
                    }
                    if (!chr.haveItem(4080100)) {
                        chr.getClient().announce(MaplePacketCreator.getMiniRoomError(6));
                        return;
                    }

                    MapleMiniGame game = new MapleMiniGame(chr, desc, pw);
                    game.setPieceType(type);
                    if (type == 0) {
                        game.setMatchesToWin(6);
                    } else if (type == 1) {
                        game.setMatchesToWin(10);
                    } else if (type == 2) {
                        game.setMatchesToWin(15);
                    }
                    game.setGameType(MiniGameType.MATCH_CARD);
                    chr.setMiniGame(game);
                    chr.getMap().addMapObject(game);
                    chr.getMap().broadcastMessage(UserCommon.Packet.addMatchCardBox(chr, 1, 0));
                    game.sendMatchCard(c, type);
                } else if (createType == 4 || createType == 5) { // shop
                    if(!GameConstants.isFreeMarketRoom(chr.getMapId())) {
                        chr.getClient().announce(MaplePacketCreator.getMiniRoomError(15));
                        return;
                    }

                    int status = establishMiniroomStatus(chr, false);
                    if (status > 0) {
                        chr.getClient().announce(MaplePacketCreator.getMiniRoomError(status));
                        return;
                    }
                    
                    try {
                        Point cpos = chr.getPosition();
                        if (chr.getMap().findClosestWarpPortal(cpos).getPosition().distance(cpos) < 120.0) {
                            chr.getClient().announce(MaplePacketCreator.getMiniRoomError(10));
                            return;
                        }
                    } catch (NullPointerException npe) {}
                    
                    String desc = slea.readMapleAsciiString();
                    slea.skip(3);
                    int itemId = slea.readInt();
                    if (chr.getInventory(MapleInventoryType.CASH).countById(itemId) < 1) {
                        chr.getClient().announce(MaplePacketCreator.getMiniRoomError(6));
                        return;
                    }

                    if (ItemConstants.isPlayerShop(itemId)) {
                        MaplePlayerShop shop = new MaplePlayerShop(chr, desc, itemId);
                        chr.setPlayerShop(shop);
                        chr.getMap().addMapObject(shop);
                        shop.sendShop(c);
                        c.getWorldServer().registerPlayerShop(shop);
                        //c.announce(MaplePacketCreator.getPlayerShopRemoveVisitor(1));
                    } else if (ItemConstants.isHiredMerchant(itemId)) {
                        if (chr.isCheater()) {
                            System.out.println("[CHEATER_ACTION] " + chr.getName() + " tried to make a hired merchant");
                        } else {
                            MapleHiredMerchant merchant = new MapleHiredMerchant(chr, desc, itemId);
                            chr.setHiredMerchant(merchant);
                            c.getWorldServer().registerHiredMerchant(merchant);
                            chr.getClient().getChannelServer().addHiredMerchant(chr.getId(), merchant);
                            chr.announce(MaplePacketCreator.getHiredMerchant(chr, merchant, true));
                        }
                    }
                }
            } else if (mode == Action.INVITE.getCode()) {
                int otherCid = slea.readInt();
                MapleCharacter other = chr.getMap().getCharacterById(otherCid);
                if (other == null || chr.getId() == other.getId()) {
                    return;
                }
                if (!chr.isCheater() && !other.isCheater()) {
                    MapleTrade.inviteTrade(chr, other);
                } else {
                    c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(),
                            "You are not allowed to invite this player to a trade. Please contact a moderator for assistance."));
                }
            } else if (mode == Action.DECLINE.getCode()) {
                MapleTrade.declineTrade(chr);
            } else if (mode == Action.VISIT.getCode()) {
                if (chr.getTrade() != null && chr.getTrade().getPartner() != null) {
                    if (!chr.getTrade().isFullTrade() && !chr.getTrade().getPartner().isFullTrade()) {
                        MapleTrade.visitTrade(chr, chr.getTrade().getPartner().getChr());
                    } else {
                        chr.getClient().announce(MaplePacketCreator.getMiniRoomError(2));
                        return;
                    }
                } else {
                    int oid = slea.readInt();
                    MapleMapObject ob = chr.getMap().getMapObject(oid);
                    if (ob instanceof MaplePlayerShop shop) {
                        shop.visitShop(chr);
                    } else if (ob instanceof MapleMiniGame game) {
                        slea.skip(1);
                        String pw = slea.available() > 1 ? slea.readMapleAsciiString() : "";

                        if(game.checkPassword(pw)) {
                            if (game.hasFreeSlot() && !game.isVisitor(chr)) {
                                game.addVisitor(chr);
                                chr.setMiniGame(game);
                                switch (game.getGameType()) {
                                    case OMOK -> game.sendOmok(c, game.getPieceType());
                                    case MATCH_CARD -> game.sendMatchCard(c, game.getPieceType());
                                }
                            } else {
                                chr.getClient().announce(MaplePacketCreator.getMiniRoomError(2));
                            }
                        } else {
                            chr.getClient().announce(MaplePacketCreator.getMiniRoomError(22));
                        }
                    } else if (ob instanceof MapleHiredMerchant merchant && chr.getHiredMerchant() == null) {
                        if (chr.getTrade() != null) {
                            //Apparently there is a dupe exploit that causes racing conditions when saving/retrieving from the db with stuff like trade open.
                            c.announce(WvsContext.Packet.enableActions());
                            return;
                        }
                        merchant.visitShop(chr);
                    }
                }
            } else if (mode == Action.CHAT.getCode()) { // chat lol
                MapleHiredMerchant merchant = chr.getHiredMerchant();
                if (chr.getTrade() != null) {
                    chr.getTrade().chat(slea.readMapleAsciiString());
                } else if (chr.getPlayerShop() != null) { //mini game
                    MaplePlayerShop shop = chr.getPlayerShop();
                    if (shop != null) {
                        shop.chat(c, slea.readMapleAsciiString());
                    }
                } else if (chr.getMiniGame() != null) {
                    MapleMiniGame game = chr.getMiniGame();
                    if (game != null) {
                        game.chat(c, slea.readMapleAsciiString());
                    }
                } else if (merchant != null) {
                    merchant.sendMessage(chr, slea.readMapleAsciiString());
                }
            } else if (mode == Action.EXIT.getCode()) {
                if (chr.getTrade() != null) {
                    MapleTrade.cancelTrade(chr, MapleTrade.TradeResult.PARTNER_CANCEL);
                } else {
                    chr.closePlayerShop();
                    chr.closeMiniGame(false);
                    chr.closeHiredMerchant(true);
                }
            } else if (mode == Action.OPEN_STORE.getCode() || mode == Action.OPEN_CASH.getCode()) {
                if (chr.getTrade() != null) {
                    //Apparently there is a dupe exploit that causes racing conditions when saving/retrieving from the db with stuff like trade open.
                    c.announce(WvsContext.Packet.enableActions());
                    return;
                }
                
                if (mode == Action.OPEN_STORE.getCode()) {
                    slea.readByte();    //01
                } else {
                    slea.readShort();
                    int birthday = slea.readInt();
                    if (!CashOperationHandler.checkBirthday(c, birthday)) { // birthday check here found thanks to lucasziron
                        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "Please check again the birthday date."));
                        return;
                    }
                    
                    c.announce(MaplePacketCreator.hiredMerchantOwnerMaintenanceLeave());
                }

                MaplePlayerShop shop = chr.getPlayerShop();
                MapleHiredMerchant merchant = chr.getHiredMerchant();
                if (shop != null && shop.isOwner(chr)) {
                    chr.getMap().broadcastMessage(UserCommon.Packet.updatePlayerShopBox(shop));
                    shop.setOpen(true);
                } else if (merchant != null && merchant.isOwner(chr)) {
                    chr.setHasMerchant(true);
                    merchant.setOpen(true);
                    chr.getMap().addMapObject(merchant);
                    chr.setHiredMerchant(null);
                    chr.getMap().broadcastMessage(EmployeePool.Packet.onEnterField(merchant));
                }
            } else if (mode == Action.READY.getCode()) {
                MapleMiniGame game = chr.getMiniGame();
                game.broadcast(MaplePacketCreator.getMiniGameReady(game));
            } else if (mode == Action.UN_READY.getCode()) {
                MapleMiniGame game = chr.getMiniGame();
                game.broadcast(MaplePacketCreator.getMiniGameUnReady(game));
            } else if (mode == Action.START.getCode()) {
                MapleMiniGame game = chr.getMiniGame();
                if (game.getGameType().equals(MiniGameType.OMOK)) {
                    game.minigameMatchStarted();
                    game.broadcast(MaplePacketCreator.getMiniGameStart(game, game.getLoser()));
                    chr.getMap().broadcastMessage(UserCommon.Packet.addOmokBox(game.getOwner(), 2, 1));
                } else if (game.getGameType().equals(MiniGameType.MATCH_CARD)) {
                    game.minigameMatchStarted();
                    game.shuffleList();
                    game.broadcast(MaplePacketCreator.getMatchCardStart(game, game.getLoser()));
                    chr.getMap().broadcastMessage(UserCommon.Packet.addMatchCardBox(game.getOwner(), 2, 1));
                }
            } else if (mode == Action.GIVE_UP.getCode()) {
                MapleMiniGame game = chr.getMiniGame();
                if (game.getGameType().equals(MiniGameType.OMOK)) {
                    if (game.isOwner(chr)) {
                        game.minigameMatchVisitorWins(true);
                    } else {
                        game.minigameMatchOwnerWins(true);
                    }
                } else if (game.getGameType().equals(MiniGameType.MATCH_CARD)) {
                    if (game.isOwner(chr)) {
                        game.minigameMatchVisitorWins(true);
                    } else {
                        game.minigameMatchOwnerWins(true);
                    }
                }
            } else if (mode == Action.REQUEST_TIE.getCode()) {
                MapleMiniGame game = chr.getMiniGame();
                if (!game.isTieDenied(chr)) {
                    if (game.isOwner(chr)) {
                        game.broadcastToVisitor(MaplePacketCreator.getMiniGameRequestTie(game));
                    } else {
                        game.broadcastToOwner(MaplePacketCreator.getMiniGameRequestTie(game));
                    }
                }
            } else if (mode == Action.ANSWER_TIE.getCode()) {
                MapleMiniGame game = chr.getMiniGame();
                if (slea.readByte() != 0) {
                    game.minigameMatchDraw();
                } else {
                    game.denyTie(chr);

                    if (game.isOwner(chr)) {
                        game.broadcastToVisitor(MaplePacketCreator.getMiniGameDenyTie(game));
                    } else {
                        game.broadcastToOwner(MaplePacketCreator.getMiniGameDenyTie(game));
                    }
                }
            } else if (mode == Action.SKIP.getCode()) {
                MapleMiniGame game = chr.getMiniGame();
                if (game.isOwner(chr)) {
                    game.broadcast(MaplePacketCreator.getMiniGameSkipOwner(game));
                } else {
                    game.broadcast(MaplePacketCreator.getMiniGameSkipVisitor(game));
                }
            } else if (mode == Action.MOVE_OMOK.getCode()) {
                int x = slea.readInt(); // x point
                int y = slea.readInt(); // y point
                int type = slea.readByte(); // piece ( 1 or 2; Owner has one piece, visitor has another, it switches every game.)
                chr.getMiniGame().setPiece(x, y, type, chr);
            } else if (mode == Action.SELECT_CARD.getCode()) {
                int turn = slea.readByte(); // 1st turn = 1; 2nd turn = 0
                int slot = slea.readByte(); // slot
                MapleMiniGame game = chr.getMiniGame();
                int firstslot = game.getFirstSlot();
                if (turn == 1) {
                    game.setFirstSlot(slot);
                    if (game.isOwner(chr)) {
                        game.broadcastToVisitor(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, turn));
                    } else {
                        game.getOwner().getClient().announce(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, turn));
                    }
                } else if ((game.getCardId(firstslot)) == (game.getCardId(slot))) {
                    if (game.isOwner(chr)) {
                        game.broadcast(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 2));
                        game.setOwnerPoints();
                    } else {
                        game.broadcast(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 3));
                        game.setVisitorPoints();
                    }
                } else if (game.isOwner(chr)) {
                    game.broadcast(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 0));
                } else {
                    game.broadcast(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 1));
                }
            } else if (mode == Action.SET_MESO.getCode()) {
                chr.getTrade().setMeso(slea.readInt());
            } else if (mode == Action.SET_ITEMS.getCode()) {
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                MapleInventoryType ivType = MapleInventoryType.getByType(slea.readByte());
                short pos = slea.readShort();
                Item item = chr.getInventory(ivType).getItem(pos);
                short quantity = slea.readShort();
                byte targetSlot = slea.readByte();

                if (targetSlot < 1 || targetSlot > 9) {
                    System.out.println("[Hack] " + chr.getName() + " Trying to dupe on trade slot.");
                    c.announce(WvsContext.Packet.enableActions());
                    return;
                }
                
                if (item == null) {
                    c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "Invalid item description."));
                    c.announce(WvsContext.Packet.enableActions());
                    return;
                }
                
                if(ii.isCash(item.getItemId())) {
                    c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "Cash items are not allowed to be traded."));
                    return;
                }
                
                if (ItemConstants.isPet(item.getItemId())) {
                    c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "Pets are not allowed to be traded."));
                    return;
                }
                
                if (quantity < 1 || quantity > item.getQuantity()) {
                    c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "You don't have enough quantity of the item."));
                    c.announce(WvsContext.Packet.enableActions());
                    return;
                }
                
                MapleTrade trade = chr.getTrade();
                if (trade != null) {
                    if ((quantity <= item.getQuantity() && quantity >= 0) || ItemConstants.isRechargeable(item.getItemId())) {
                        if (ii.isDropRestricted(item.getItemId())) { // ensure that undroppable items do not make it to the trade window
                            if (!MapleKarmaManipulator.hasKarmaFlag(item)) {
                                c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "That item is untradeable."));
                                c.announce(WvsContext.Packet.enableActions());
                                return;
                            }
                        }
                        
                        MapleInventory inv = chr.getInventory(ivType);
                        inv.lockInventory();
                        try {
                            Item checkItem = chr.getInventory(ivType).getItem(pos);
                            if (checkItem != item || checkItem.getPosition() != item.getPosition()) {
                                c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "Invalid item description."));
                                c.announce(WvsContext.Packet.enableActions());
                                return;
                            }
                            
                            Item tradeItem = item.copy();
                            if (ItemConstants.isRechargeable(item.getItemId())) {
                                quantity = item.getQuantity();
                            }
                            
                            tradeItem.setQuantity(quantity);
                            tradeItem.setPosition(targetSlot);
                            
                            if (trade.addItem(tradeItem)) {
                                MapleInventoryManipulator.removeFromSlot(c, ivType, item.getPosition(), quantity, true);
                                
                                trade.getChr().announce(MaplePacketCreator.getTradeItemAdd((byte) 0, tradeItem));
                                if (trade.getPartner() != null) {
                                    trade.getPartner().getChr().announce(MaplePacketCreator.getTradeItemAdd((byte) 1, tradeItem));
                                }
                            }
                        } catch (Exception e) {
                            FilePrinter.printError(FilePrinter.TRADE_EXCEPTION, e, "Player '" + chr + "' tried to add " + ii.getName(item.getItemId()) + " qty. " + item.getQuantity() + " in trade (slot " + targetSlot + ") then exception occurred.");
                        } finally {
                            inv.unlockInventory();
                        }
                    }
                }
            } else if (mode == Action.CONFIRM.getCode()) {
                MapleTrade.completeTrade(chr);
            } else if (mode == Action.ADD_ITEM.getCode() || mode == Action.PUT_ITEM.getCode()) {
                if (chr.getTrade() != null) {
                    //Apparently there is a dupe exploit that causes racing conditions when saving/retrieving from the db with stuff like trade open.
                    c.announce(WvsContext.Packet.enableActions());
                    return;
                }

                MapleInventoryType ivType = MapleInventoryType.getByType(slea.readByte());
                short slot = slea.readShort();
                short bundles = slea.readShort();
                Item ivItem = chr.getInventory(ivType).getItem(slot);

                if (ivItem == null || ivItem.isUntradeable()) {
                    c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "Could not perform shop operation with that item."));
                    c.announce(WvsContext.Packet.enableActions());
                    return;
                }

                short perBundle = slea.readShort();
                
                if (ItemConstants.isRechargeable(ivItem.getItemId())) {
                    perBundle = 1;
                    bundles = 1;
                } else if (ivItem.getQuantity() < (bundles * perBundle)) {     // thanks GabrielSin for finding a dupe here
                    c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "Could not perform shop operation with that item."));
                    c.announce(WvsContext.Packet.enableActions());
                    return;
                }

                int price = slea.readInt();
                if (perBundle <= 0 || perBundle * bundles > 2000 || bundles <= 0 || price <= 0 || price > Integer.MAX_VALUE) {
                    AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + " tried to packet edit with hired merchants.");
                    FilePrinter.printError(FilePrinter.EXPLOITS + chr.getName() + ".txt", chr.getName() + " might of possibly packet edited Hired Merchants\nperBundle: " + perBundle + "\nperBundle * bundles (This multiplied cannot be greater than 2000): " + perBundle * bundles + "\nbundles: " + bundles + "\nprice: " + price);
                    return;
                }

                if(MapleItemInformationProvider.getInstance().isCash(ivItem.getItemId())) {
                    c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "Cash items are not allowed to be sold on the Player Store."));
                    return;
                }
                
                if (ItemConstants.isPet(ivItem.getItemId())) {
                    c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "Pets are not allowed to be sold on the Player Store."));
                    return;
                }

                Item sellItem = ivItem.copy();
                if(!ItemConstants.isRechargeable(ivItem.getItemId())) {
                    sellItem.setQuantity(perBundle);
                }

                MaplePlayerShopItem shopItem = new MaplePlayerShopItem(sellItem, bundles, price);
                MaplePlayerShop shop = chr.getPlayerShop();
                MapleHiredMerchant merchant = chr.getHiredMerchant();
                if (shop != null && shop.isOwner(chr)) {
                    if (shop.isOpen() || !shop.addItem(shopItem)) { // thanks Vcoc for pointing an exploit with unlimited shop slots
                        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "You can't sell it anymore."));
                        return;
                    }
                    
                    if (ItemConstants.isRechargeable(ivItem.getItemId())) {
                        MapleInventoryManipulator.removeFromSlot(c, ivType, slot, ivItem.getQuantity(), true);
                    } else {
                        MapleInventoryManipulator.removeFromSlot(c, ivType, slot, (short) (bundles * perBundle), true);
                    }
                    
                    c.announce(MaplePacketCreator.getPlayerShopItemUpdate(shop));
                } else if (merchant != null && merchant.isOwner(chr)) {
                    if (ivType.equals(MapleInventoryType.CASH) && merchant.isPublished()) {
                        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "Cash items are only allowed to be sold when first opening the store."));
                        return;
                    }
                    
                    if (merchant.isOpen() || !merchant.addItem(shopItem)) { // thanks Vcoc for pointing an exploit with unlimited shop slots
                        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "You can't sell it anymore."));
                        return;
                    }
                    
                    if (ItemConstants.isRechargeable(ivItem.getItemId())) {
                        MapleInventoryManipulator.removeFromSlot(c, ivType, slot, ivItem.getQuantity(), true);
                    } else {
                        MapleInventoryManipulator.removeFromSlot(c, ivType, slot, (short) (bundles * perBundle), true);
                    }
                    
                    c.announce(MaplePacketCreator.updateHiredMerchant(merchant, chr));
                    
                    if (ServerConstants.USE_ENFORCE_MERCHANT_SAVE) {
                        chr.saveCharToDB(false);
                    }
                    
                    try {
                        merchant.saveItems(false);   // thanks Masterrulax for realizing yet another dupe with merchants/Fredrick
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "You can't sell without owning a shop."));
                }
            } else if (mode == Action.REMOVE_ITEM.getCode()) {
                if (chr.getTrade() != null) {
                    //Apparently there is a dupe exploit that causes racing conditions when saving/retrieving from the db with stuff like trade open.
                    c.announce(WvsContext.Packet.enableActions());
                    return;
                }

                MaplePlayerShop shop = chr.getPlayerShop();
                if (shop != null && shop.isOwner(chr)) {
                    if (shop.isOpen()) {
                        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "You can't take it with the store open."));
                        return;
                    }

                    int slot = slea.readShort();
                    if (slot >= shop.getItems().size() || slot < 0) {
                        AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + " tried to packet edit with a player shop.");
                        FilePrinter.printError(FilePrinter.EXPLOITS + chr.getName() + ".txt", chr.getName() + " tried to remove item at slot " + slot);
                        c.disconnect(true, false);
                        return;
                    }

                    shop.takeItemBack(slot, chr);
                }
            } else if (mode == Action.MERCHANT_MESO.getCode()) {
                MapleHiredMerchant merchant = chr.getHiredMerchant();
                if (merchant == null) return;

                merchant.withdrawMesos(chr);
            } else if (mode == Action.MERCHANT_ORGANIZE.getCode()) {
                MapleHiredMerchant merchant = chr.getHiredMerchant();
                if (merchant == null || !merchant.isOwner(chr)) return;

                merchant.withdrawMesos(chr);
                merchant.clearInexistentItems();

                if (merchant.getItems().isEmpty()) {
                    merchant.closeOwnerMerchant(chr);
                    return;
                }
                c.announce(MaplePacketCreator.updateHiredMerchant(merchant, chr));

            } else if (mode == Action.BUY.getCode() || mode == Action.MERCHANT_BUY.getCode()) {
                if (chr.getTrade() != null) {
                    //Apparently there is a dupe exploit that causes racing conditions when saving/retrieving from the db with stuff like trade open.
                    c.announce(WvsContext.Packet.enableActions());
                    return;
                }

                int itemid = slea.readByte();
                short quantity = slea.readShort();
                if (quantity < 1) {
                    AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + " tried to packet edit with a hired merchant and or player shop.");
                    FilePrinter.printError(FilePrinter.EXPLOITS + chr.getName() + ".txt", chr.getName() + " tried to buy item " + itemid + " with quantity " + quantity);
                    c.disconnect(true, false);
                    return;
                }
                MaplePlayerShop shop = chr.getPlayerShop();
                MapleHiredMerchant merchant = chr.getHiredMerchant();
                if (shop != null && shop.isVisitor(chr) && !chr.isCheater() && !shop.getOwner().isCheater()) {
                    if (shop.buy(c, itemid, quantity)) {
                        shop.broadcast(MaplePacketCreator.getPlayerShopItemUpdate(shop));
                    }
                } else if (merchant != null && !merchant.isOwner(chr) && !chr.isCheater()) {
                    merchant.buy(c, itemid, quantity);
                    merchant.broadcastToVisitorsThreadsafe(MaplePacketCreator.updateHiredMerchant(merchant, chr));
                }
            } else if (mode == Action.TAKE_ITEM_BACK.getCode()) {
                if (chr.getTrade() != null) {
                    //Apparently there is a dupe exploit that causes racing conditions when saving/retrieving from the db with stuff like trade open.
                    c.announce(WvsContext.Packet.enableActions());
                    return;
                }

                MapleHiredMerchant merchant = chr.getHiredMerchant();
                if (merchant != null && merchant.isOwner(chr)) {
                    if (merchant.isOpen()) {
                        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(), "You can't take it with the store open."));
                        return;
                    }

                    int slot = slea.readShort();
                    if (slot >= merchant.getItems().size() || slot < 0) {
                        AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + " tried to packet edit with a hired merchant.");
                        FilePrinter.printError(FilePrinter.EXPLOITS + chr.getName() + ".txt", chr.getName() + " tried to remove item at slot " + slot);
                        c.disconnect(true, false);
                        return;
                    }

                    merchant.takeItemBack(slot, chr);
                }
            } else if (mode == Action.CLOSE_MERCHANT.getCode()) {
                if (chr.getTrade() != null) {
                    //Apparently there is a dupe exploit that causes racing conditions when saving/retrieving from the db with stuff like trade open.
                    c.announce(WvsContext.Packet.enableActions());
                    return;
                }

                MapleHiredMerchant merchant = chr.getHiredMerchant();
                if (merchant != null) {
                    merchant.closeOwnerMerchant(chr);
                }
            } else if (mode == Action.MAINTENANCE_OFF.getCode()) {
                if (chr.getTrade() != null) {
                    //Apparently there is a dupe exploit that causes racing conditions when saving/retrieving from the db with stuff like trade open.
                    c.announce(WvsContext.Packet.enableActions());
                    return;
                }

                MapleHiredMerchant merchant = chr.getHiredMerchant();
                if(merchant != null) {
                    if (merchant.isOwner(chr)) {
                        if (merchant.getItems().isEmpty()) {
                            merchant.closeOwnerMerchant(chr);
                        } else {
                            merchant.clearMessages();
                            merchant.setOpen(true);
                            merchant.getMap().broadcastMessage(EmployeePool.Packet.onMiniRoomBalloon(merchant));
                        }
                    }
                }

                chr.setHiredMerchant(null);
                c.announce(WvsContext.Packet.enableActions());
            } else if (mode == Action.BAN_PLAYER.getCode()) {
                slea.skip(1);

                MaplePlayerShop shop = chr.getPlayerShop();
                if (shop != null && shop.isOwner(chr)) {
                    shop.banPlayer(slea.readMapleAsciiString());
                }
            } else if (mode == Action.EXPEL.getCode()) {
                MapleMiniGame miniGame = chr.getMiniGame();
                if(miniGame != null && miniGame.isOwner(chr)) {
                    MapleCharacter visitor = miniGame.getVisitor();

                    if(visitor != null) {
                        visitor.closeMiniGame(false);
                        visitor.announce(MaplePacketCreator.getMiniGameClose(true, 5));
                    }
                }
                 } else if (mode == Action.EXIT_AFTER_GAME.getCode()) {
                MapleMiniGame miniGame = chr.getMiniGame();
                if(miniGame != null) {
                    miniGame.setQuitAfterGame(chr, true);
                }
            } else if (mode == Action.CANCEL_EXIT_AFTER_GAME.getCode()) {
                MapleMiniGame miniGame = chr.getMiniGame();
                if(miniGame != null) {
                    miniGame.setQuitAfterGame(chr, false);
                }
            }
        } finally {
            c.releaseClient();
        }
    }
}
