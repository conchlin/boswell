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
package tools;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import client.*;
import network.opcode.SendOpcode;
import net.server.Server;
import net.server.channel.handlers.PlayerInteractionHandler;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import server.cashshop.CashItemFactory;
import server.cashshop.SpecialCashItem;
import server.cashshop.CategoryDiscount;
import server.cashshop.CommodityFlags;
import server.cashshop.ItemStock;
import server.cashshop.LimitedGoods;
import server.DueyPackage;
import server.MTSItemInfo;
import server.MapleItemInformationProvider;
import server.MapleShopItem;
import server.MapleTrade;
import server.life.MapleMonster;
import server.life.MobSpawnType;
import server.maps.MapleHiredMerchant;
import server.maps.MapleMap;
import server.maps.MapleMiniGame;
import server.maps.MapleMiniGame.MiniGameResult;
import server.maps.MaplePlayerShop;
import server.maps.MaplePlayerShopItem;
import server.life.MaplePlayerNPC;
import server.skills.SkillMacro;
import tools.data.output.MaplePacketLittleEndianWriter;
import server.skills.Skill;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.InventoryOperation;
import client.newyear.NewYearCardRecord;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.ItemConstants;
import server.maps.AbstractMapleMapObject;
import tools.packets.PacketUtil;

/**
 *
 * @author Frz
 */
public class MaplePacketCreator {

    public static final List<Pair<MapleStat, Integer>> EMPTY_STATUPDATE = Collections.emptyList();

    public static byte[] setExtraPendantSlot(boolean toggleExtraSlot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_EXTRA_PENDANT_SLOT.getValue());
        mplew.writeBool(toggleExtraSlot);
        return mplew.getPacket();
    }

    /**
     * Sends a hello packet.
     *
     * @param mapleVersion The maple client version.
     * @param sendIv the IV used by the server for sending
     * @param recvIv the IV used by the server for receiving
     * @return
     */
    public static byte[] getHello(short mapleVersion, byte[] sendIv, byte[] recvIv) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(0x0E);
        mplew.writeShort(mapleVersion);
        mplew.writeShort(1);
        mplew.write(49);
        mplew.write(recvIv);
        mplew.write(sendIv);
        mplew.write(8);
        return mplew.getPacket();
    }

    public static byte[] sendPolice() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAKE_GM_NOTICE.getValue());
        mplew.write(0);//doesn't even matter what value
        return mplew.getPacket();
    }

    public static byte[] sendPolice(String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DATA_CRC_CHECK_FAILED.getValue());
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    /**
     * Gets character info for a character.
     *
     * @param chr The character to get info about.
     * @return The character info packet.
     */
    public static byte[] getCharInfo(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_FIELD.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.write(1);
        mplew.write(1);
        mplew.writeShort(0);
        for (int i = 0; i < 3; i++) {
            mplew.writeInt(Randomizer.nextInt());
        }
        PacketUtil.addCharacterInfo(mplew, chr);
        mplew.writeLong(PacketUtil.getTime(System.currentTimeMillis()));
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to change maps.
     *
     * @param to The <code>MapleMap</code> to warp to.
     * @param spawnPoint The spawn portal number to spawn at.
     * @param chr The character warping to <code>to</code>
     * @return The map change packet.
     */
    public static byte[] getWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_FIELD.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.writeInt(0);//updated
        mplew.write(0);//updated
        mplew.writeInt(to.getId());
        mplew.write(spawnPoint);
        mplew.writeShort(chr.getHp());
        mplew.writeBool(false);
        mplew.writeLong(PacketUtil.getTime(Server.getInstance().getCurrentTime()));
        mplew.skip(18);
        return mplew.getPacket();
    }

    public static byte[] getWarpToMap(MapleMap to, int spawnPoint, Point spawnPosition, MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_FIELD.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.writeInt(0);//updated
        mplew.write(0);//updated
        mplew.writeInt(to.getId());
        mplew.write(spawnPoint);
        mplew.writeShort(chr.getHp());
        mplew.writeBool(true);
        mplew.writeInt(spawnPosition.x);    // spawn position placement thanks to Arnah (Vertisy)
        mplew.writeInt(spawnPosition.y);
        mplew.writeLong(PacketUtil.getTime(Server.getInstance().getCurrentTime()));
        mplew.skip(18);
        return mplew.getPacket();
    }

    /**
     * Gets a packet to spawn a portal.
     *
     * @param townId The ID of the town the portal goes to.
     * @param targetId The ID of the target.
     * @param pos Where to put the portal.
     * @return The portal spawn packet.
     */
    public static byte[] spawnPortal(int townId, int targetId, Point pos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(14);
        mplew.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        mplew.writePos(pos);
        return mplew.getPacket();
    }

    public static byte[] removePortal() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
        mplew.writeInt(999999999);
        mplew.writeInt(999999999);

        return mplew.getPacket();
    }

    /**
     * Gets a server message packet.
     *
     * @param message The message to convey.
     * @return The server message packet.
     */
    public static byte[] serverMessage(String message) {
        return serverMessage(4, (byte) 0, message, true, false, 0);
    }

    /**
     * Gets a server notice packet.
     *
     * Possible values for <code>type</code>:<br> 0: [Notice]<br> 1: Popup<br>
     * 2: Megaphone<br> 3: Super Megaphone<br> 4: Scrolling message at top<br>
     * 5: Pink Text<br> 6: Lightblue Text
     *
     * @param type The type of the notice.
     * @param message The message to convey.
     * @return The server notice packet.
     */
    public static byte[] serverNotice(int type, String message) {
        return serverMessage(type, (byte) 0, message, false, false, 0);
    }

    /**
     * Gets a server notice packet.
     *
     * Possible values for <code>type</code>:<br> 0: [Notice]<br> 1: Popup<br>
     * 2: Megaphone<br> 3: Super Megaphone<br> 4: Scrolling message at top<br>
     * 5: Pink Text<br> 6: Lightblue Text
     *
     * @param type The type of the notice.
     * @param message The message to convey.
     * @return The server notice packet.
     */
    public static byte[] serverNotice(int type, String message, int npc) {
        return serverMessage(type, 0, message, false, false, npc);
    }

    public static byte[] serverNotice(int type, int channel, String message) {
        return serverMessage(type, channel, message, false, false, 0);
    }

    public static byte[] serverNotice(int type, int channel, String message, boolean smegaEar) {
        return serverMessage(type, channel, message, false, smegaEar, 0);
    }

    /**
     * Gets a server message packet.
     *
     * Possible values for <code>type</code>:<br> 0: [Notice]<br> 1: Popup<br>
     * 2: Megaphone<br> 3: Super Megaphone<br> 4: Scrolling message at top<br>
     * 5: Pink Text<br> 6: Lightblue Text<br> 7: BroadCasting NPC
     *
     * @param type The type of the notice.
     * @param channel The channel this notice was sent on.
     * @param message The message to convey.
     * @param servermessage Is this a scrolling ticker?
     * @return The server notice packet.
     */
    private static byte[] serverMessage(int type, int channel, String message, boolean servermessage, boolean megaEar, int npc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
        mplew.write(type);
        if (servermessage) {
            mplew.write(1);
        }
        mplew.writeMapleAsciiString(message);
        if (type == 3) {
            mplew.write(channel - 1); // channel
            mplew.writeBool(megaEar);
        } else if (type == 6) {
            mplew.writeInt(0);
        } else if (type == 7) { // npc 
            mplew.writeInt(npc);
        }
        return mplew.getPacket();
    }

    /**
     * Sends a Avatar Super Megaphone packet.
     *
     * @param chr The character name.
     * @param medal The medal text.
     * @param channel Which channel.
     * @param itemId Which item used.
     * @param message The message sent.
     * @param ear Whether or not the ear is shown for whisper.
     * @return
     */
    public static byte[] getAvatarMega(MapleCharacter chr, String medal, int channel, int itemId, List<String> message, boolean ear) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_AVATAR_MEGAPHONE.getValue());
        mplew.writeInt(itemId);
        mplew.writeMapleAsciiString(medal + chr.getName());
        for (String s : message) {
            mplew.writeMapleAsciiString(s);
        }
        mplew.writeInt(channel - 1); // channel
        mplew.writeBool(ear);
        PacketUtil.addCharLook(mplew, chr, true);
        return mplew.getPacket();
    }

    /*
         * Sends a packet to remove the tiger megaphone
         * @return
     */
    public static byte[] byeAvatarMega() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CLEAR_AVATAR_MEGAPHONE.getValue());
        mplew.write(1);
        return mplew.getPacket();
    }

    /**
     * Sends the Gachapon green message when a user uses a gachapon ticket.
     *
     * @param item
     * @param town
     * @param player
     * @return
     */
    public static byte[] gachaponMessage(Item item, String town, MapleCharacter player) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
        mplew.write(0x0B);
        mplew.writeMapleAsciiString(player.getName() + " : got a(n)");
        mplew.writeInt(0); //random?
        mplew.writeMapleAsciiString(town);
        PacketUtil.addItemInfoZeroPos(mplew, item);
        return mplew.getPacket();
    }

    /**
     * Gets a spawn monster packet.
     *
     * @param life The monster to spawn.
     *
     * @return The spawn monster packet.
     */
    public static byte[] spawnMonster(MapleMonster life, int effect) {
        return spawnMonsterInternal(life, false, false, effect, false, null);
    }

    /**
     * Gets a control monster packet.
     *
     * @param life The monster to give control to.
     * @param aggro Aggressive monster?
     *
     * @return The monster control packet.
     */
    public static byte[] controlMonster(MapleMonster life, int effect, boolean aggro) {
        return spawnMonsterInternal(life, true, aggro, effect, false, null);
    }

    /**
     * Removes a monster invisibility.
     *
     * @param life
     *
     * @return byte[]
     */
    public static byte[] removeMonsterInvisibility(MapleMonster life) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
        mplew.write(1);
        mplew.writeInt(life.getObjectId());
        return mplew.getPacket();
    }

    /**
     * Makes a monster invisible for Ariant PQ.
     *
     * @param life
     *
     * @return
     */
    public static byte[] makeMonsterInvisible(MapleMonster life) {
        return spawnMonsterInternal(life, true, false, MobSpawnType.SUSPENDED.getType(), true, null);
    }

    /**
     * Handles monsters not being targettable, such as Zakum's first body.
     *
     * @param life The mob to spawn as non-targettable.
     * @param effect The effect to show when spawning.
     *
     * @return The packet to spawn the mob as non-targettable.
     */
    public static byte[] spawnFakeMonster(MapleMonster life, int effect) {
        return spawnMonsterInternal(life, true, false, effect, false, null);
    }

    /**
     * Makes a monster previously spawned as non-targettable, targettable.
     *
     * @param life The mob to make targettable.
     *
     * @return The packet to make the mob targettable.
     */
    public static byte[] makeMonsterReal(MapleMonster life, int effect) {
        return spawnMonsterInternal(life, false, false, effect, false, null);
    }

    /**
     * Internal function to handler monster spawning and controlling.
     *
     * @param life The mob to perform operations with.
     * @param requestController Requesting control of mob?
     * @param aggro Aggressive mob?
     * @param effect The spawn effect to use.
     *
     * @return The spawn/control packet.
     */
    private static byte[] spawnMonsterInternal(MapleMonster life, boolean requestController, boolean aggro,
                                               int effect, boolean makeInvis, MonsterStatusEffect mse) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (makeInvis) {
            mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
            mplew.write(0);
            mplew.writeInt(life.getObjectId());
            return mplew.getPacket();
        }
        if (requestController) {
            mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
            mplew.write(aggro ? 2 : 1);
        } else {
            mplew.writeShort(SendOpcode.SPAWN_MONSTER.getValue());
        }
        mplew.writeInt(life.getObjectId());
        mplew.write(life.getController() == null ? 5 : 1);
        mplew.writeInt(life.getId());
        mobStat(mplew, mse);//MobStat::EncodeTemporary
        mplew.writePos(life.getPosition());
        mplew.write(life.getStance());
        mplew.writeShort(life.getStartFh()); //Origin FH //life.getStartFh()
        mplew.writeShort(life.getFh());
        mplew.write(effect);
        if (effect == -3 || effect > -1) {
            mplew.writeInt(life.getParentMob());
        }
        mplew.write(life.getTeam());
        mplew.writeInt(0); //nEffectItemId
        return mplew.getPacket();
    }

    /**
     * Gets a stop control monster packet.
     *
     * @param oid The ObjectID of the monster to stop controlling.
     * @return The stop control monster packet.
     */
    public static byte[] stopControllingMonster(int oid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
        mplew.write(0);
        mplew.writeInt(oid);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show an EXP increase.
     *
     * @param gain The amount of EXP gained.
     * @param inChat In the chat box?
     * @param white White text or yellow?
     * @return The exp gained packet.
     */
    public static byte[] getShowExpGain(int gain, int equip, int party, boolean inChat, boolean white) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
        mplew.writeBool(white);
        mplew.writeInt(gain);
        mplew.writeBool(inChat);
        mplew.writeInt(0); // bonus event exp
        mplew.write(0); // third monster kill event
        mplew.write(0); // RIP byte, this is always a 0
        mplew.writeInt(0); //wedding bonus
        if (inChat) { // quest bonus rate stuff
            mplew.write(0);
        }

        mplew.write(0); //0 = party bonus, 100 = 1x Bonus EXP, 200 = 2x Bonus EXP
        mplew.writeInt(party); // party bonus 
        mplew.writeInt(equip); //equip bonus
        mplew.writeInt(0); //Internet Cafe Bonus
        mplew.writeInt(0); //Rainbow Week Bonus
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show a fame gain.
     *
     * @param gain How many fame gained.
     * @return The meso gain packet.
     */
    public static byte[] getShowFameGain(int gain) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(4);
        mplew.writeInt(gain);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show a meso gain.
     *
     * @param gain How many mesos gained.
     * @return The meso gain packet.
     */
    public static byte[] getShowMesoGain(int gain) {
        return getShowMesoGain(gain, false);
    }

    /**
     * Gets a packet telling the client to show a meso gain.
     *
     * @param gain How many mesos gained.
     * @param inChat Show in the chat window?
     * @return The meso gain packet.
     */
    public static byte[] getShowMesoGain(int gain, boolean inChat) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        if (!inChat) {
            mplew.write(0);
            mplew.writeShort(1); //v83
        } else {
            mplew.write(5);
        }
        mplew.writeInt(gain);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show a item gain.
     *
     * @param itemId The ID of the item gained.
     * @param quantity How many items gained.
     * @return The item gain packet.
     */
    public static byte[] getShowItemGain(int itemId, short quantity) {
        return getShowItemGain(itemId, quantity, false);
    }

    /**
     * Gets a packet telling the client to show an item gain.
     *
     * @param itemId The ID of the item gained.
     * @param quantity The number of items gained.
     * @param inChat Show in the chat window?
     * @return The item gain packet.
     */
    public static byte[] getShowItemGain(int itemId, short quantity, boolean inChat) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (inChat) {
            mplew.writeShort(SendOpcode.LocalEffect.getValue());
            mplew.write(3);
            mplew.write(1);
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
        } else {
            mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            mplew.writeShort(0);
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static byte[] killMonster(int oid, boolean animation) {
        return killMonster(oid, animation ? 1 : 0);
    }

    /**
     * Gets a packet telling the client that a monster was killed.
     *
     * @param oid The objectID of the killed monster.
     * @param animation 0 = dissapear, 1 = fade out, 2+ = special
     * @return The kill monster packet.
     */
    public static byte[] killMonster(int oid, int animation) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.KILL_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(animation);
        mplew.write(animation);
        return mplew.getPacket();
    }

    public static byte[] onNewYearCardRes(MapleCharacter user, int cardId, int mode, int msg) {
        NewYearCardRecord newyear = user.getNewYearRecord(cardId);
        return onNewYearCardRes(user, newyear, mode, msg);
    }

    public static byte[] onNewYearCardRes(MapleCharacter user, NewYearCardRecord newyear, int mode, int msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NEW_YEAR_CARD_RES.getValue());
        mplew.write(mode);
        switch (mode) {
            case 4, 6 ->
                    // Successfully sent a New Year Card\r\n to %s.
                    // Successfully received a New Year Card.
                    PacketUtil.encodeNewYearCard(newyear, mplew);
            case 8 -> // Successfully deleted a New Year Card.
                    mplew.writeInt(newyear.getId());
            case 5, 7, 9, 0xB ->
                    // Nexon's stupid and makes 4 modes do the same operation..
                    mplew.write(msg);

            // 0x10: You have no free slot to store card.\r\ntry later on please.
            // 0x11: You have no card to send.
            // 0x12: Wrong inventory information !
            // 0x13: Cannot find such character !
            // 0x14: Incoherent Data !
            // 0x15: An error occured during DB operation.
            // 0x16: An unknown error occured !
            // 0xF: You cannot send a card to yourself !
            case 0xA -> {
                // GetUnreceivedList_Done
                int nSN = 1;
                mplew.writeInt(nSN);
                if ((nSN - 1) <= 98 && nSN > 0) {//lol nexon are you kidding
                    for (int i = 0; i < nSN; i++) {
                        mplew.writeInt(newyear.getId());
                        mplew.writeInt(newyear.getSenderId());
                        mplew.writeMapleAsciiString(newyear.getSenderName());
                    }
                }
            }
            case 0xC -> {
                // NotiArrived
                mplew.writeInt(newyear.getId());
                mplew.writeMapleAsciiString(newyear.getSenderName());
            }
            case 0xD -> {
                // BroadCast_AddCardInfo
                mplew.writeInt(newyear.getId());
                mplew.writeInt(user.getId());
            }
            case 0xE ->
                    // BroadCast_RemoveCardInfo
                    mplew.writeInt(newyear.getId());
        }
        return mplew.getPacket();
    }

    public static byte[] getNPCShop(MapleClient c, int sid, List<MapleShopItem> items) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.OPEN_NPC_SHOP.getValue());
        mplew.writeInt(sid);
        mplew.writeShort(items.size()); // item count
        for (MapleShopItem item : items) {
            mplew.writeInt(item.getItemId());
            mplew.writeInt(item.getPrice());
            mplew.writeInt(item.getPrice() == 0 ? item.getPitch() : 0); //Perfect Pitch
            mplew.writeInt(0); //Can be used x minutes after purchase
            mplew.writeInt(0); //Hmm
            if (!ItemConstants.isRechargeable(item.getItemId())) {
                mplew.writeShort(1); // stacksize o.o
                mplew.writeShort(item.getBuyable());
            } else {
                mplew.writeShort(0);
                mplew.writeInt(0);
                mplew.writeShort(PacketUtil.doubleToShortBits(ii.getUnitPrice(item.getItemId())));
                mplew.writeShort(ii.getSlotMax(c, item.getItemId()));
            }
        }
        return mplew.getPacket();
    }

    /* 00 = /
         * 01 = You don't have enough in stock
         * 02 = You do not have enough mesos
         * 03 = Please check if your inventory is full or not
         * 05 = You don't have enough in stock
         * 06 = Due to an error, the trade did not happen
         * 07 = Due to an error, the trade did not happen
         * 08 = /
         * 0D = You need more items
         * 0E = CRASH; LENGTH NEEDS TO BE LONGER :O
     */
    public static byte[] shopTransaction(byte code) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
        mplew.write(code);
        return mplew.getPacket();
    }

    public static byte[] catchMessage(int message) { // not done, I guess
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BRIDLE_MOB_CATCH_FAIL.getValue());
        mplew.write(message); // 1 = too strong, 2 = Elemental Rock
        mplew.writeInt(0);//Maybe itemid?
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] sendMapleLifeCharacterInfo() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MAPLELIFE_RESULT.getValue());
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] sendMapleLifeNameError() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MAPLELIFE_RESULT.getValue());
        mplew.writeInt(2);
        mplew.writeInt(3);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] sendMapleLifeError(int code) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MAPLELIFE_ERROR.getValue());
        mplew.write(0);
        mplew.writeInt(code);
        return mplew.getPacket();
    }

    /**
     *
     * @param chr
     * @return
     */
    public static byte[] charInfo(MapleCharacter chr) {
        //3D 00 0A 43 01 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CHAR_INFO.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel());
        mplew.writeShort(chr.getJob().getId());
        mplew.writeShort(chr.getFame());
        mplew.write(chr.getMarriageRing() != null ? 1 : 0);
        String guildName = "";
        String allianceName = "";
        if (chr.getGuildId() > 0) {
            MapleGuild mg = Server.getInstance().getGuild(chr.getGuildId());
            guildName = mg.getName();

            MapleAlliance alliance = Server.getInstance().getAlliance(chr.getGuild().getAllianceId());
            if (alliance != null) {
                allianceName = alliance.getName();
            }
        }
        mplew.writeMapleAsciiString(guildName);
        mplew.writeMapleAsciiString(allianceName);  // does not seem to work
        mplew.write(0); // pMedalInfo, thanks to Arnah (Vertisy)

        MaplePet[] pets = chr.getPets();
        Item inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -114);
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                mplew.write(pets[i].getUniqueId());
                mplew.writeInt(pets[i].getItemId()); // petid
                mplew.writeMapleAsciiString(pets[i].getName());
                mplew.write(pets[i].getLevel()); // pet level
                mplew.writeShort(pets[i].getCloseness()); // pet closeness
                mplew.write(pets[i].getFullness()); // pet fullness
                mplew.writeShort(0);
                mplew.writeInt(inv != null ? inv.getItemId() : 0);
            }
        }
        mplew.write(0); //end of pets

        Item mount;     //mounts can potentially crash the client if the player's level is not properly checked
        if (chr.getMount() != null && (mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18)) != null && MapleItemInformationProvider.getInstance().getEquipLevelReq(mount.getItemId()) <= chr.getLevel()) {
            MapleMount mmount = chr.getMount();
            mplew.write(mmount.getId()); //mount
            mplew.writeInt(mmount.getLevel()); //level
            mplew.writeInt(mmount.getExp()); //exp
            mplew.writeInt(mmount.getTiredness()); //tiredness
        } else {
            mplew.write(0);
        }
        mplew.write(chr.getCashShop().getWishList().size());
        for (int sn : chr.getCashShop().getWishList()) {
            mplew.writeInt(sn);
        }

        MonsterBook book = chr.getMonsterBook();
        mplew.writeInt(book.getBookLevel());
        mplew.writeInt(book.getNormalCard());
        mplew.writeInt(book.getSpecialCard());
        mplew.writeInt(book.getTotalCards());
        mplew.writeInt(chr.getMonsterBookCover() > 0 ? MapleItemInformationProvider.getInstance().getCardMobId(chr.getMonsterBookCover()) : 0);
        Item medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -49);
        if (medal != null) {
            mplew.writeInt(medal.getItemId());
        } else {
            mplew.writeInt(0);
        }
        ArrayList<Short> medalQuests = new ArrayList<>();
        List<MapleQuestStatus> completed = chr.getCompletedQuests();
        for (MapleQuestStatus q : completed) {
            if (q.getQuest().getId() >= 29000) { // && q.getQuest().getId() <= 29923
                medalQuests.add(q.getQuest().getId());
            }
        }

        Collections.sort(medalQuests);
        mplew.writeShort(medalQuests.size());
        for (Short s : medalQuests) {
            mplew.writeShort(s);
        }
        return mplew.getPacket();
    }

    /*        mplew.writeInt(cid);
             writeLongMask(mplew, statups);
             for (Pair<MapleBuffStat, Integer> statup : statups) {
             if (morph) {
             mplew.writeInt(statup.getRight().intValue());
             } else {
             mplew.writeShort(statup.getRight().shortValue());
             }
             }
             mplew.writeShort(0);
             mplew.write(0);*/
    /**
     *
     * @param quest
     * @return
     */
    public static byte[] forfeitQuest(short quest) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest);
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     *
     * @param quest
     * @return
     */
    public static byte[] completeQuest(short quest, long time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest);
        mplew.write(2);
        mplew.writeLong(PacketUtil.getTime(time));
        return mplew.getPacket();
    }

    public static byte[] updateQuest(MapleQuestStatus q, boolean infoUpdate) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(infoUpdate ? q.getQuest().getInfoNumber() : q.getQuest().getId());
        if (infoUpdate) {
            mplew.write(1);
        } else {
            mplew.write(q.getStatus().getId());
        }

        mplew.writeMapleAsciiString(q.getQuestData());
        mplew.skip(5);
        return mplew.getPacket();
    }

    public static byte[] getPlayerShopChat(MapleCharacter c, String chat, boolean owner) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
        mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        mplew.write(owner ? 0 : 1);
        mplew.writeMapleAsciiString(c.getName() + " : " + chat);
        return mplew.getPacket();
    }

    public static byte[] getPlayerShopNewVisitor(MapleCharacter c, int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(slot);
        PacketUtil.addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        return mplew.getPacket();
    }

    public static byte[] getPlayerShopRemoveVisitor(int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        if (slot != 0) {
            mplew.writeShort(slot);
        }
        return mplew.getPacket();
    }

    public static byte[] getTradePartnerAdd(MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(1);
        PacketUtil.addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        return mplew.getPacket();
    }

    public static byte[] tradeInvite(MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.INVITE.getCode());
        mplew.write(3);
        mplew.writeMapleAsciiString(c.getName());
        mplew.write(new byte[]{(byte) 0xB7, (byte) 0x50, 0, 0});
        return mplew.getPacket();
    }

    public static byte[] getTradeMesoSet(byte number, int meso) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.SET_MESO.getCode());
        mplew.write(number);
        mplew.writeInt(meso);
        return mplew.getPacket();
    }

    public static byte[] getTradeItemAdd(byte number, Item item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.SET_ITEMS.getCode());
        mplew.write(number);
        mplew.write(item.getPosition());
        PacketUtil.addItemInfoZeroPos(mplew, item);
        return mplew.getPacket();
    }

    public static byte[] getPlayerShopItemUpdate(MaplePlayerShop shop) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
        mplew.write(shop.getItems().size());
        for (MaplePlayerShopItem item : shop.getItems()) {
            mplew.writeShort(item.getBundles());
            mplew.writeShort(item.getItem().getQuantity());
            mplew.writeInt(item.getPrice());
            PacketUtil.addItemInfoZeroPos(mplew, item.getItem());
        }
        return mplew.getPacket();
    }

    public static byte[] getPlayerShopOwnerUpdate(MaplePlayerShop.SoldItem item, int position) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.UPDATE_PLAYERSHOP.getCode());
        mplew.write(position);
        mplew.writeShort(item.getQuantity());
        mplew.writeMapleAsciiString(item.getBuyer());

        return mplew.getPacket();
    }

    /**
     *
     * @param shop
     * @param owner
     * @return
     */
    public static byte[] getPlayerShop(MaplePlayerShop shop, boolean owner) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(4);
        mplew.write(4);
        mplew.write(owner ? 0 : 1);

        if (owner) {
            List<MaplePlayerShop.SoldItem> sold = shop.getSold();
            mplew.write(sold.size());
            for (MaplePlayerShop.SoldItem s : sold) {
                mplew.writeInt(s.getItemId());
                mplew.writeShort(s.getQuantity());
                mplew.writeInt(s.getMesos());
                mplew.writeMapleAsciiString(s.getBuyer());
            }
        } else {
            mplew.write(0);
        }

        PacketUtil.addCharLook(mplew, shop.getOwner(), false);
        mplew.writeMapleAsciiString(shop.getOwner().getName());

        MapleCharacter visitors[] = shop.getVisitors();
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null) {
                mplew.write(i + 1);
                PacketUtil.addCharLook(mplew, visitors[i], false);
                mplew.writeMapleAsciiString(visitors[i].getName());
            }
        }

        mplew.write(0xFF);
        mplew.writeMapleAsciiString(shop.getDescription());
        List<MaplePlayerShopItem> items = shop.getItems();
        mplew.write(0x10);  //TODO SLOTS, which is 16 for most stores...slotMax
        mplew.write(items.size());
        for (MaplePlayerShopItem item : items) {
            mplew.writeShort(item.getBundles());
            mplew.writeShort(item.getItem().getQuantity());
            mplew.writeInt(item.getPrice());
            PacketUtil.addItemInfoZeroPos(mplew, item.getItem());
        }
        return mplew.getPacket();
    }

    public static byte[] getTradeStart(MapleClient c, MapleTrade trade, byte number) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(3);
        mplew.write(2);
        mplew.write(number);
        if (number == 1) {
            mplew.write(0);
            PacketUtil.addCharLook(mplew, trade.getPartner().getChr(), false);
            mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
        }
        mplew.write(number);
        PacketUtil.addCharLook(mplew, c.getPlayer(), false);
        mplew.writeMapleAsciiString(c.getPlayer().getName());
        mplew.write(0xFF);
        return mplew.getPacket();
    }

    public static byte[] getTradeConfirmation() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CONFIRM.getCode());
        return mplew.getPacket();
    }

    /**
     * Possible values for <code>operation</code>:<br> 2: Trade cancelled by the
     * other character<br> 7: Trade successful<br> 8: Trade unsuccessful<br>
     * 9: Cannot carry more one-of-a-kind items<br> 12: Cannot trade on
     * different maps<br>
     * 13: Cannot trade, game files damaged<br>
     *
     * @param number
     * @param operation
     * @return
     */
    public static byte[] getTradeResult(byte number, byte operation) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        mplew.write(number);
        mplew.write(operation);
        return mplew.getPacket();
    }

    public static byte[] showOwnBuffEffect(int skillid, int effectid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.LocalEffect.getValue());
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(0xA9);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] showOwnBerserk(int skilllevel, boolean Berserk) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.LocalEffect.getValue());
        mplew.write(1);
        mplew.writeInt(1320006);
        mplew.write(0xA9);
        mplew.write(skilllevel);
        mplew.write(Berserk ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] getShowInventoryFull() {
        return getShowInventoryStatus(0xff);
    }

    public static byte[] showItemUnavailable() {
        return getShowInventoryStatus(0xfe);
    }

    public static byte[] getShowInventoryStatus(int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0);
        mplew.write(mode);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static boolean isMovementAffectingSkill(Map<MonsterStatus, Integer> stats) {
        return stats.containsKey(MonsterStatus.DOOM) || stats.containsKey(MonsterStatus.STUN) || stats.containsKey(MonsterStatus.SPEED) || stats.containsKey(MonsterStatus.FREEZE) || stats.containsKey(MonsterStatus.RISE_BY_TOSS);
    }

    public static void writeMonsterStatMask(final MaplePacketLittleEndianWriter mplew, Map<MonsterStatus, Integer> stats) {
        int[] mask = new int[4];
        for (Entry<MonsterStatus, Integer> stat : stats.entrySet()) {
            mask[stat.getKey().getSet()] |= stat.getKey().getMask();
        }
        for (int i = 3; i >= 0; i--) {
            mplew.writeInt(mask[i]);
        }
    }

    public static void mobStat(MaplePacketLittleEndianWriter mplew, MonsterStatusEffect mse) {//MobStat::EncodeTemporary
        if (mse == null) {
            for (int i = 3; i >= 0; i--) {
                mplew.writeInt(0);
            }
            return;
        }
        writeMonsterStatMask(mplew, mse.getStati());
        Map<MonsterStatus, Integer> stats = mse.getStati();
        Skill skill = mse.getMobSkill() != null ? mse.getMobSkill() : mse.getPlayerSkill();
        if (stats.containsKey(MonsterStatus.WATK)) {
            mplew.writeShort(stats.get(MonsterStatus.WATK));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.WDEF)) {
            mplew.writeShort(stats.get(MonsterStatus.WDEF));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.MATK)) {
            mplew.writeShort(stats.get(MonsterStatus.MATK));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.MDEF)) {
            mplew.writeShort(stats.get(MonsterStatus.MDEF));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.ACC)) {
            mplew.writeShort(stats.get(MonsterStatus.ACC));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.AVOID)) {
            mplew.writeShort(stats.get(MonsterStatus.AVOID));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.SPEED)) {
            mplew.writeShort(stats.get(MonsterStatus.SPEED));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.STUN)) {
            mplew.writeShort(stats.get(MonsterStatus.STUN));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.FREEZE)) {
            mplew.writeShort(stats.get(MonsterStatus.FREEZE));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.POISON)) {
            mplew.writeShort(stats.get(MonsterStatus.POISON));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.SEAL)) {
            mplew.writeShort(stats.get(MonsterStatus.SEAL));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.DARKNESS)) {
            mplew.writeShort(stats.get(MonsterStatus.DARKNESS));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.WEAPON_ATTACK_UP)) {
            mplew.writeShort(stats.get(MonsterStatus.WEAPON_ATTACK_UP));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.WEAPON_DEFENSE_UP)) {
            mplew.writeShort(stats.get(MonsterStatus.WEAPON_DEFENSE_UP));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.MAGIC_ATTACK_UP)) {
            mplew.writeShort(stats.get(MonsterStatus.MAGIC_ATTACK_UP));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.MAGIC_DEFENSE_UP)) {
            mplew.writeShort(stats.get(MonsterStatus.MAGIC_DEFENSE_UP));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.DOOM)) {
            mplew.writeShort(stats.get(MonsterStatus.DOOM));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.SHADOW_WEB)) {
            mplew.writeShort(stats.get(MonsterStatus.SHADOW_WEB));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.WEAPON_IMMUNITY)) {
            mplew.writeShort(stats.get(MonsterStatus.WEAPON_IMMUNITY));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.MAGIC_IMMUNITY)) {
            mplew.writeShort(stats.get(MonsterStatus.MAGIC_IMMUNITY));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.SHOWDOWN)) {
            mplew.writeShort(stats.get(MonsterStatus.SHOWDOWN));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.HARD_SKIN)) {
            mplew.writeShort(stats.get(MonsterStatus.HARD_SKIN));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.NINJA_AMBUSH)) {
            mplew.writeShort(stats.get(MonsterStatus.NINJA_AMBUSH));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.ELEMENTAL_ATTRIBUTE)) {
            mplew.writeShort(stats.get(MonsterStatus.ELEMENTAL_ATTRIBUTE));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.VENOMOUS_WEAPON)) {
            mplew.writeShort(stats.get(MonsterStatus.VENOMOUS_WEAPON));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.BLIND)) {
            mplew.writeShort(stats.get(MonsterStatus.BLIND));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.SEAL_SKILL)) {
            mplew.writeShort(stats.get(MonsterStatus.SEAL_SKILL));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.DAZZLE)) {
            mplew.writeShort(stats.get(MonsterStatus.DAZZLE));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.WEAPON_REFLECT)) {
            mplew.writeShort(stats.get(MonsterStatus.WEAPON_REFLECT));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.MAGIC_REFLECT)) {
            mplew.writeShort(stats.get(MonsterStatus.MAGIC_REFLECT));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.RISE_BY_TOSS)) {
            mplew.writeShort(stats.get(MonsterStatus.RISE_BY_TOSS));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.BODY_PRESSURE)) {
            mplew.writeShort(stats.get(MonsterStatus.BODY_PRESSURE));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.WEAKEN)) {
            mplew.writeShort(stats.get(MonsterStatus.WEAKEN));
            skill.writeSkillInfo(mplew);
            mplew.writeShort(mse.getDurationAsShort()); //buffTime
        }
        if (stats.containsKey(MonsterStatus.BURNED)) {
        	/*
        	     oPacket.Encode4(lBurnedInfo.size());
			    for (BurnedInfo pBurnedInfo : lBurnedInfo) {
			        oPacket.Encode4(pBurnedInfo.dwCharacterID);
			        oPacket.Encode4(pBurnedInfo.nSkillID);
			        oPacket.Encode4(pBurnedInfo.nDamage);
			        oPacket.Encode4(pBurnedInfo.tInterval);
			        oPacket.Encode4((int) (System.currentTimeMillis() - pBurnedInfo.tEnd));
			        oPacket.Encode4(pBurnedInfo.nDotCount);
			    }
        	 */
            mplew.writeInt(stats.get(MonsterStatus.BURNED));
        }
        if (stats.containsKey(MonsterStatus.WEAPON_REFLECT)) {
            mplew.writeInt(stats.get(MonsterStatus.WEAPON_REFLECT)); //opt.wOption
        }
        if (stats.containsKey(MonsterStatus.MAGIC_REFLECT)) {
            mplew.writeInt(stats.get(MonsterStatus.MAGIC_REFLECT)); //opt.wOption
        }
        if (stats.containsKey(MonsterStatus.WEAPON_REFLECT) || stats.containsKey(MonsterStatus.MAGIC_REFLECT)) {
            mplew.writeInt((int) (mse.getMobSkill().getProp() * 100)); //oPacket.Encode4(nCounterProb);
        }
        if (stats.containsKey(MonsterStatus.DISABLE)) {
        	/*
    	    	oPacket.Encode1(bInvincible);
    			oPacket.Encode1(bDisable);
        	 */
            mplew.write(0);
            mplew.write(0);
        }
    }

    public static byte[] updateBuddylist(Collection<BuddylistEntry> buddylist) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
        mplew.write(7);
        mplew.write(buddylist.size());
        for (BuddylistEntry buddy : buddylist) {
            if (buddy.isVisible()) {
                mplew.writeInt(buddy.getCharacterId()); // cid
                mplew.writeAsciiString(PacketUtil.getRightPaddedStr(buddy.getName(), '\0', 13));
                mplew.write(0); // opposite status
                mplew.writeInt(buddy.getChannel() - 1);
                mplew.writeAsciiString(PacketUtil.getRightPaddedStr(buddy.getGroup(), '\0', 13));
                mplew.writeInt(0);//mapid?
            }
        }
        for (int x = 0; x < buddylist.size(); x++) {
            mplew.writeInt(0);//mapid?
        }
        return mplew.getPacket();
    }

    public static byte[] buddylistMessage(byte message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
        mplew.write(message);
        return mplew.getPacket();
    }

    public static byte[] requestBuddylistAdd(int cidFrom, int cid, String nameFrom) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
        mplew.write(9);
        mplew.writeInt(cidFrom);
        mplew.writeMapleAsciiString(nameFrom);
        mplew.writeInt(cidFrom);
        mplew.writeAsciiString(PacketUtil.getRightPaddedStr(nameFrom, '\0', 11));
        mplew.write(0x09);
        mplew.write(0xf0);
        mplew.write(0x01);
        mplew.writeInt(0x0f);
        mplew.writeNullTerminatedAsciiString("Default Group");
        mplew.writeInt(cid);
        return mplew.getPacket();
    }

    public static byte[] updateBuddyChannel(int characterid, int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
        mplew.write(0x14);
        mplew.writeInt(characterid);
        mplew.write(0);
        mplew.writeInt(channel);
        return mplew.getPacket();
    }

    public static byte[] updateBuddyCapacity(int capacity) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
        mplew.write(0x15);
        mplew.write(capacity);
        return mplew.getPacket();
    }

    public static byte[] environmentMoveList(Set<Entry<String, Integer>> envList) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FieldObstacleOnOffStatus.getValue());
        mplew.writeInt(envList.size());

        for (Entry<String, Integer> envMove : envList) {
            mplew.writeMapleAsciiString(envMove.getKey());
            mplew.writeInt(envMove.getValue());
        }

        return mplew.getPacket();
    }

    public static byte[] messengerInvite(String from, int messengerid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x03);
        mplew.writeMapleAsciiString(from);
        mplew.write(0);
        mplew.writeInt(messengerid);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x00);
        mplew.write(position);
        PacketUtil.addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.write(channel);
        mplew.write(0x00);
        return mplew.getPacket();
    }

    public static byte[] removeMessengerPlayer(int position) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x02);
        mplew.write(position);
        return mplew.getPacket();
    }

    public static byte[] updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x07);
        mplew.write(position);
        PacketUtil.addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.write(channel);
        mplew.write(0x00);
        return mplew.getPacket();
    }

    public static byte[] joinMessenger(int position) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x01);
        mplew.write(position);
        return mplew.getPacket();
    }

    public static byte[] messengerChat(String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x06);
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    public static byte[] messengerNote(String text, int mode, int mode2) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(text);
        mplew.write(mode2);
        return mplew.getPacket();
    }

    public static byte[] getMacros(SkillMacro[] macros) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MACRO_SYS_DATA_INIT.getValue());
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (macros[i] != null) {
                count++;
            }
        }
        mplew.write(count);
        for (int i = 0; i < 5; i++) {
            SkillMacro macro = macros[i];
            if (macro != null) {
                mplew.writeMapleAsciiString(macro.getName());
                mplew.write(macro.getShout());
                mplew.writeInt(macro.getSkill1());
                mplew.writeInt(macro.getSkill2());
                mplew.writeInt(macro.getSkill3());
            }
        }
        return mplew.getPacket();
    }

    public static byte[] crogBoatPacket(boolean type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CONTI_MOVE.getValue());
        mplew.write(10);
        mplew.write(type ? 4 : 5);
        return mplew.getPacket();
    }

    public static byte[] boatPacket(boolean type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CONTI_STATE.getValue());
        mplew.write(type ? 1 : 2);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] getMiniGame(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(1);
        mplew.write(0);
        mplew.write(owner ? 0 : 1);
        mplew.write(0);
        PacketUtil.addCharLook(mplew, minigame.getOwner(), false);
        mplew.writeMapleAsciiString(minigame.getOwner().getName());
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            PacketUtil.addCharLook(mplew, visitor, false);
            mplew.writeMapleAsciiString(visitor.getName());
        }
        mplew.write(0xFF);
        mplew.write(0);
        mplew.writeInt(1);
        mplew.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.WIN, true));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.TIE, true));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.LOSS, true));
        mplew.writeInt(minigame.getOwnerScore());
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            mplew.writeInt(1);
            mplew.writeInt(visitor.getMiniGamePoints(MiniGameResult.WIN, true));
            mplew.writeInt(visitor.getMiniGamePoints(MiniGameResult.TIE, true));
            mplew.writeInt(visitor.getMiniGamePoints(MiniGameResult.LOSS, true));
            mplew.writeInt(minigame.getVisitorScore());
        }
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(minigame.getDescription());
        mplew.write(piece);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameReady(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.READY.getCode());
        return mplew.getPacket();
    }

    public static byte[] getMiniGameUnReady(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.UN_READY.getCode());
        return mplew.getPacket();
    }

    public static byte[] getMiniGameStart(MapleMiniGame game, int loser) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.START.getCode());
        mplew.write(loser);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameSkipOwner(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.SKIP.getCode());
        mplew.write(0x01);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameRequestTie(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.REQUEST_TIE.getCode());
        return mplew.getPacket();
    }

    public static byte[] getMiniGameDenyTie(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ANSWER_TIE.getCode());
        return mplew.getPacket();
    }

    /**
     * 1 = Room already closed 2 = Can't enter due full cappacity 3 = Other
     * requests at this minute 4 = Can't do while dead 5 = Can't do while middle
     * event 6 = This character unable to do it 7, 20 = Not allowed to trade
     * anymore 9 = Can only trade on same map 10 = May not open store near
     * portal 11, 14 = Can't start game here 12 = Can't open store at this
     * channel 13 = Can't estabilish miniroom 15 = Stores only an the free
     * market 16 = Lists the rooms at FM (?) 17 = You may not enter this store
     * 18 = Owner undergoing store maintenance 19 = Unable to enter tournament
     * room 21 = Not enough mesos to enter 22 = Incorrect password
     *
     * @param status
     * @return
     */
    public static byte[] getMiniRoomError(int status) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(0);
        mplew.write(status);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameSkipVisitor(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.writeShort(PlayerInteractionHandler.Action.SKIP.getCode());
        return mplew.getPacket();
    }

    public static byte[] getMiniGameMoveOmok(MapleMiniGame game, int move1, int move2, int move3) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(12);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.MOVE_OMOK.getCode());
        mplew.writeInt(move1);
        mplew.writeInt(move2);
        mplew.write(move3);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameNewVisitor(MapleMiniGame minigame, MapleCharacter c, int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(slot);
        PacketUtil.addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(1);
        mplew.writeInt(c.getMiniGamePoints(MiniGameResult.WIN, true));
        mplew.writeInt(c.getMiniGamePoints(MiniGameResult.TIE, true));
        mplew.writeInt(c.getMiniGamePoints(MiniGameResult.LOSS, true));
        mplew.writeInt(minigame.getVisitorScore());
        return mplew.getPacket();
    }

    public static byte[] getMiniGameRemoveVisitor() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        mplew.write(1);
        return mplew.getPacket();
    }

    private static byte[] getMiniGameResult(MapleMiniGame game, int tie, int result, int forfeit) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.GET_RESULT.getCode());

        int matchResultType;
        if (tie == 0 && forfeit != 1) {
            matchResultType = 0;
        } else if (tie != 0) {
            matchResultType = 1;
        } else {
            matchResultType = 2;
        }

        mplew.write(matchResultType);
        mplew.writeBool(result == 2); // host/visitor wins

        boolean omok = game.isOmok();
        if (matchResultType == 1) {
            mplew.write(0);
            mplew.writeShort(0);
            mplew.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.WIN, omok)); // wins
            mplew.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.TIE, omok)); // ties
            mplew.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.LOSS, omok)); // losses
            mplew.writeInt(game.getOwnerScore()); // points

            mplew.writeInt(0); // unknown
            mplew.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.WIN, omok)); // wins
            mplew.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.TIE, omok)); // ties
            mplew.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.LOSS, omok)); // losses
            mplew.writeInt(game.getVisitorScore()); // points
            mplew.write(0);
        } else {
            mplew.writeInt(0);
            mplew.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.WIN, omok)); // wins
            mplew.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.TIE, omok)); // ties
            mplew.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.LOSS, omok)); // losses
            mplew.writeInt(game.getOwnerScore()); // points
            mplew.writeInt(0);
            mplew.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.WIN, omok)); // wins
            mplew.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.TIE, omok)); // ties
            mplew.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.LOSS, omok)); // losses
            mplew.writeInt(game.getVisitorScore()); // points
        }

        return mplew.getPacket();
    }

    public static byte[] getMiniGameOwnerWin(MapleMiniGame game, boolean forfeit) {
        return getMiniGameResult(game, 0, 1, forfeit ? 1 : 0);
    }

    public static byte[] getMiniGameVisitorWin(MapleMiniGame game, boolean forfeit) {
        return getMiniGameResult(game, 0, 2, forfeit ? 1 : 0);
    }

    public static byte[] getMiniGameTie(MapleMiniGame game) {
        return getMiniGameResult(game, 1, 3, 0);
    }

    public static byte[] getMiniGameClose(boolean visitor, int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        mplew.writeBool(visitor);
        mplew.write(type);
        /* 2 : CRASH 3 : The room has been closed 4 : You have left the room 5 : You have been expelled  */
        return mplew.getPacket();
    }

    public static byte[] getMatchCard(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(2);
        mplew.write(2);
        mplew.write(owner ? 0 : 1);
        mplew.write(0);
        PacketUtil.addCharLook(mplew, minigame.getOwner(), false);
        mplew.writeMapleAsciiString(minigame.getOwner().getName());
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            PacketUtil.addCharLook(mplew, visitor, false);
            mplew.writeMapleAsciiString(visitor.getName());
        }
        mplew.write(0xFF);
        mplew.write(0);
        mplew.writeInt(2);
        mplew.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.WIN, false));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.TIE, false));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.LOSS, false));

        //set vs
        mplew.writeInt(minigame.getOwnerScore());
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            mplew.writeInt(2);
            mplew.writeInt(visitor.getMiniGamePoints(MiniGameResult.WIN, false));
            mplew.writeInt(visitor.getMiniGamePoints(MiniGameResult.TIE, false));
            mplew.writeInt(visitor.getMiniGamePoints(MiniGameResult.LOSS, false));
            mplew.writeInt(minigame.getVisitorScore());
        }
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(minigame.getDescription());
        mplew.write(piece);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] getMatchCardStart(MapleMiniGame game, int loser) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.START.getCode());
        mplew.write(loser);

        int last;
        if (game.getMatchesToWin() > 10) {
            last = 30;
        } else if (game.getMatchesToWin() > 6) {
            last = 20;
        } else {
            last = 12;
        }

        mplew.write(last);
        for (int i = 0; i < last; i++) {
            mplew.writeInt(game.getCardId(i));
        }
        return mplew.getPacket();
    }

    public static byte[] getMatchCardNewVisitor(MapleMiniGame minigame, MapleCharacter c, int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(slot);
        PacketUtil.addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(1);
        mplew.writeInt(c.getMiniGamePoints(MiniGameResult.WIN, false));
        mplew.writeInt(c.getMiniGamePoints(MiniGameResult.TIE, false));
        mplew.writeInt(c.getMiniGamePoints(MiniGameResult.LOSS, false));
        mplew.writeInt(minigame.getVisitorScore());
        return mplew.getPacket();
    }

    public static byte[] getMatchCardSelect(MapleMiniGame game, int turn, int slot, int firstslot, int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.SELECT_CARD.getCode());
        mplew.write(turn);
        if (turn == 1) {
            mplew.write(slot);
        } else if (turn == 0) {
            mplew.write(slot);
            mplew.write(firstslot);
            mplew.write(type);
        }
        return mplew.getPacket();
    }

    public static byte[] getPlayerShopChat(MapleCharacter c, String chat, byte slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
        mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        mplew.write(slot);
        mplew.writeMapleAsciiString(c.getName() + " : " + chat);
        return mplew.getPacket();
    }

    public static byte[] getTradeChat(MapleCharacter c, String chat, boolean owner) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
        mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        mplew.write(owner ? 0 : 1);
        mplew.writeMapleAsciiString(c.getName() + " : " + chat);
        return mplew.getPacket();
    }

    // 0: Success
    // 1: The room is already closed.
    // 2: You can't enter the room due to full capacity.
    // 3: Other requests are being fulfilled this minute.
    // 4: You can't do it while you're dead.
    // 7: You are not allowed to trade other items at this point.
    // 17: You may not enter this store.
    // 18: The owner of the store is currently undergoing store maintenance. Please try again in a bit.
    // 23: This can only be used inside the Free Market.
    // default: This character is unable to do it.	
    public static byte[] getOwlMessage(int msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        mplew.writeShort(SendOpcode.SHOP_LINK_RESULT.getValue());
        mplew.write(msg); // depending on the byte sent, a different message is sent.

        return mplew.getPacket();
    }

    public static byte[] owlOfMinerva(MapleClient c, int itemid, List<Pair<MaplePlayerShopItem, AbstractMapleMapObject>> hmsAvailable) {
        byte itemType = ItemConstants.getInventoryType(itemid).getType();

        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOP_SCANNER_RESULT.getValue()); // header.
        mplew.write(6);
        mplew.writeInt(0);
        mplew.writeInt(itemid);
        mplew.writeInt(hmsAvailable.size());
        for (Pair<MaplePlayerShopItem, AbstractMapleMapObject> hme : hmsAvailable) {
            MaplePlayerShopItem item = hme.getLeft();
            AbstractMapleMapObject mo = hme.getRight();

            if (mo instanceof MaplePlayerShop ps) {
                MapleCharacter owner = ps.getOwner();

                mplew.writeMapleAsciiString(owner.getName());
                mplew.writeInt(owner.getMapId());
                mplew.writeMapleAsciiString(ps.getDescription());
                mplew.writeInt(item.getBundles());
                mplew.writeInt(item.getItem().getQuantity());
                mplew.writeInt(item.getPrice());
                mplew.writeInt(owner.getId());
                mplew.write(owner.getClient().getChannel() - 1);
            } else {
                MapleHiredMerchant hm = (MapleHiredMerchant) mo;

                mplew.writeMapleAsciiString(hm.getOwner());
                mplew.writeInt(hm.getMapId());
                mplew.writeMapleAsciiString(hm.getDescription());
                mplew.writeInt(item.getBundles());
                mplew.writeInt(item.getItem().getQuantity());
                mplew.writeInt(item.getPrice());
                mplew.writeInt(hm.getOwnerId());
                mplew.write(hm.getChannel() - 1);
            }

            mplew.write(itemType);
            if (itemType == MapleInventoryType.EQUIP.getType()) {
                PacketUtil.addItemInfoZeroPos(mplew, item.getItem());
            }
        }
        return mplew.getPacket();
    }

    public static byte[] getOwlOpen(List<Integer> owlLeaderboards) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendOpcode.SHOP_SCANNER_RESULT.getValue());
        mplew.write(7);
        mplew.write(owlLeaderboards.size());
        for (Integer i : owlLeaderboards) {
            mplew.writeInt(i);
        }

        return mplew.getPacket();
    }

    /*
         * Possible things for ENTRUSTED_SHOP_CHECK_RESULT
         * 0x0E = 00 = Renaming Failed - Can't find the merchant, 01 = Renaming successful
         * 0x10 = Changes channel to the store (Store is open at Channel 1, do you want to change channels?)
         * 0x11 = You cannot sell any items when managing.. blabla
         * 0x12 = FKING POPUP LOL
     */
    public static byte[] getHiredMerchant(MapleCharacter chr, MapleHiredMerchant hm, boolean firstTime) {//Thanks Dustin
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(0x05);
        mplew.write(0x04);
        mplew.writeShort(hm.getVisitorSlotThreadsafe(chr) + 1);
        mplew.writeInt(hm.getItemId());
        mplew.writeMapleAsciiString("Hired Merchant");

        MapleCharacter visitors[] = hm.getVisitors();
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null) {
                mplew.write(i + 1);
                PacketUtil.addCharLook(mplew, visitors[i], false);
                mplew.writeMapleAsciiString(visitors[i].getName());
            }
        }
        mplew.write(-1);
        if (hm.isOwner(chr)) {
            List<Pair<String, Byte>> msgList = hm.getMessages();

            mplew.writeShort(msgList.size());
            for (Pair<String, Byte> stringBytePair : msgList) {
                mplew.writeMapleAsciiString(stringBytePair.getLeft());
                mplew.write(stringBytePair.getRight());
            }
        } else {
            mplew.writeShort(0);
        }
        mplew.writeMapleAsciiString(hm.getOwner());
        if (hm.isOwner(chr)) {
            mplew.writeShort(0);
            mplew.writeShort(hm.getTimeOpen());
            mplew.write(firstTime ? 1 : 0);
            List<MapleHiredMerchant.SoldItem> sold = hm.getSold();
            mplew.write(sold.size());
            for (MapleHiredMerchant.SoldItem s : sold) {
                mplew.writeInt(s.getItemId());
                mplew.writeShort(s.getQuantity());
                mplew.writeInt(s.getMesos());
                mplew.writeMapleAsciiString(s.getBuyer());
            }
            mplew.writeInt(chr.getMerchantMeso());//:D?
        }
        mplew.writeMapleAsciiString(hm.getDescription());
        mplew.write(0x10); //TODO SLOTS, which is 16 for most stores...slotMax
        mplew.writeInt(hm.isOwner(chr) ? chr.getMerchantMeso() : chr.getMeso());
        mplew.write(hm.getItems().size());
        if (hm.getItems().isEmpty()) {
            mplew.write(0);//Hmm??
        } else {
            for (MaplePlayerShopItem item : hm.getItems()) {
                mplew.writeShort(item.getBundles());
                mplew.writeShort(item.getItem().getQuantity());
                mplew.writeInt(item.getPrice());
                PacketUtil.addItemInfoZeroPos(mplew, item.getItem());
            }
        }
        return mplew.getPacket();
    }

    public static byte[] updateHiredMerchant(MapleHiredMerchant hm, MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
        mplew.writeInt(hm.isOwner(chr) ? chr.getMerchantMeso() : chr.getMeso());
        mplew.write(hm.getItems().size());
        for (MaplePlayerShopItem item : hm.getItems()) {
            mplew.writeShort(item.getBundles());
            mplew.writeShort(item.getItem().getQuantity());
            mplew.writeInt(item.getPrice());
            PacketUtil.addItemInfoZeroPos(mplew, item.getItem());
        }
        return mplew.getPacket();
    }

    public static byte[] hiredMerchantChat(String message, byte slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
        mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        mplew.write(slot);
        mplew.writeMapleAsciiString(message);
        return mplew.getPacket();
    }

    public static byte[] hiredMerchantVisitorLeave(int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        if (slot != 0) {
            mplew.write(slot);
        }
        return mplew.getPacket();
    }

    public static byte[] hiredMerchantOwnerLeave() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.REAL_CLOSE_MERCHANT.getCode());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] hiredMerchantOwnerMaintenanceLeave() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.REAL_CLOSE_MERCHANT.getCode());
        mplew.write(5);
        return mplew.getPacket();
    }

    public static byte[] hiredMerchantMaintenanceMessage() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(0x00);
        mplew.write(0x12);
        return mplew.getPacket();
    }

    public static byte[] leaveHiredMerchant(int slot, int status2) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        mplew.write(slot);
        mplew.write(status2);
        return mplew.getPacket();
    }

    public static byte[] hiredMerchantVisitorAdd(MapleCharacter chr, int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(slot);
        PacketUtil.addCharLook(mplew, chr, false);
        mplew.writeMapleAsciiString(chr.getName());
        return mplew.getPacket();
    }

    public static byte[] getPlayerNPC(MaplePlayerNPC npc) {     // thanks to Arnah
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.IMITATED_NPC_DATA.getValue());
        mplew.write(0x01);
        mplew.writeInt(npc.getScriptId());
        mplew.writeMapleAsciiString(npc.getName());
        mplew.write(npc.getGender());
        mplew.write(npc.getSkin());
        mplew.writeInt(npc.getFace());
        mplew.write(0);
        mplew.writeInt(npc.getHair());
        Map<Short, Integer> equip = npc.getEquips();
        Map<Short, Integer> myEquip = new LinkedHashMap<>();
        Map<Short, Integer> maskedEquip = new LinkedHashMap<>();
        for (short position : equip.keySet()) {
            short pos = (byte) (position * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, equip.get(position));
            } else if ((pos > 100 && pos != 111) || pos == -128) { // don't ask. o.o
                pos -= 100;
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, equip.get(position));
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, equip.get(position));
            }
        }
        for (Entry<Short, Integer> entry : myEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF);
        for (Entry<Short, Integer> entry : maskedEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF);
        Integer cWeapon = equip.get((byte) -111);
        if (cWeapon != null) {
            mplew.writeInt(cWeapon);
        } else {
            mplew.writeInt(0);
        }
        for (int i = 0; i < 3; i++) {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static byte[] removePlayerNPC(int oid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.IMITATED_NPC_DATA.getValue());
        mplew.write(0x00);
        mplew.writeInt(oid);

        return mplew.getPacket();
    }

    public static byte[] sendYellowTip(String tip) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_WEEK_EVENT_MESSAGE.getValue());
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(tip);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static byte[] sendMTS(List<MTSItemInfo> items, int tab, int type, int page, int pages) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x15); //operation
        mplew.writeInt(pages * 16); //testing, change to 10 if fails
        mplew.writeInt(items.size()); //number of items
        mplew.writeInt(tab);
        mplew.writeInt(type);
        mplew.writeInt(page);
        mplew.write(1);
        mplew.write(1);
        for (MTSItemInfo item : items) {
            PacketUtil.addItemInfoZeroPos(mplew, item.getItem());
            mplew.writeInt(item.getID()); //id
            mplew.writeInt(item.getTaxes()); //this + below = price
            mplew.writeInt(item.getPrice()); //price
            mplew.writeInt(0);
            mplew.writeLong(PacketUtil.getTime(item.getEndingDate()));
            mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
            mplew.writeMapleAsciiString(item.getSeller()); //char name
            for (int j = 0; j < 28; j++) {
                mplew.write(0);
            }
        }
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] showMTSCash(MapleCharacter p) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION2.getValue());
        mplew.writeInt(p.getCashShop().getCash(4));
        mplew.writeInt(p.getCashShop().getCash(2));
        return mplew.getPacket();
    }

    public static byte[] MTSWantedListingOver(int nx, int items) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x3D);
        mplew.writeInt(nx);
        mplew.writeInt(items);
        return mplew.getPacket();
    }

    public static byte[] MTSConfirmSell() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x1D);
        return mplew.getPacket();
    }

    public static byte[] MTSConfirmBuy() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x33);
        return mplew.getPacket();
    }

    public static byte[] MTSFailBuy() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x34);
        mplew.write(0x42);
        return mplew.getPacket();
    }

    public static byte[] MTSConfirmTransfer(int quantity, int pos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x27);
        mplew.writeInt(quantity);
        mplew.writeInt(pos);
        return mplew.getPacket();
    }

    public static byte[] notYetSoldInv(List<MTSItemInfo> items) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x23);
        mplew.writeInt(items.size());
        if (!items.isEmpty()) {
            for (MTSItemInfo item : items) {
                PacketUtil.addItemInfoZeroPos(mplew, item.getItem());
                mplew.writeInt(item.getID()); //id
                mplew.writeInt(item.getTaxes()); //this + below = price
                mplew.writeInt(item.getPrice()); //price
                mplew.writeInt(0);
                mplew.writeLong(PacketUtil.getTime(item.getEndingDate()));
                mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
                mplew.writeMapleAsciiString(item.getSeller()); //char name
                for (int i = 0; i < 28; i++) {
                    mplew.write(0);
                }
            }
        } else {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static byte[] transferInventory(List<MTSItemInfo> items) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x21);
        mplew.writeInt(items.size());
        if (!items.isEmpty()) {
            for (MTSItemInfo item : items) {
                PacketUtil.addItemInfoZeroPos(mplew, item.getItem());
                mplew.writeInt(item.getID()); //id
                mplew.writeInt(item.getTaxes()); //taxes
                mplew.writeInt(item.getPrice()); //price
                mplew.writeInt(0);
                mplew.writeLong(PacketUtil.getTime(item.getEndingDate()));
                mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
                mplew.writeMapleAsciiString(item.getSeller()); //char name
                for (int i = 0; i < 28; i++) {
                    mplew.write(0);
                }
            }
        }
        mplew.write(0xD0 + items.size());
        mplew.write(new byte[]{-1, -1, -1, 0});
        return mplew.getPacket();
    }

    public static byte[] updateGender(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.SET_GENDER.getValue());
        mplew.write(chr.getGender());
        return mplew.getPacket();
    }

    public static byte[] updateAreaInfo(int area, String info) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0x0A); //0x0B in v95
        mplew.writeShort(area);//infoNumber
        mplew.writeMapleAsciiString(info);
        return mplew.getPacket();
    }

    public static byte[] getGPMessage(int gpChange) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(6);
        mplew.writeInt(gpChange);
        return mplew.getPacket();
    }

    public static byte[] getItemMessage(int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(7);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    public static byte[] addCard(boolean full, int cardid, int level) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        mplew.writeShort(SendOpcode.MONSTER_BOOK_SET_CARD.getValue());
        mplew.write(full ? 0 : 1);
        mplew.writeInt(cardid);
        mplew.writeInt(level);
        return mplew.getPacket();
    }

    public static byte[] changeCover(int cardid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.MONSTER_BOOK_SET_COVER.getValue());
        mplew.writeInt(cardid);
        return mplew.getPacket();
    }

    public static byte[] itemMegaphone(String msg, boolean whisper, int channel, Item item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
        mplew.write(8);
        mplew.writeMapleAsciiString(msg);
        mplew.write(channel - 1);
        mplew.write(whisper ? 1 : 0);
        if (item == null) {
            mplew.write(0);
        } else {
            mplew.write(item.getPosition());
            PacketUtil.addItemInfoZeroPos(mplew, item);
        }
        return mplew.getPacket();
    }

    /**
     * Sends a report response
     *
     * Possible values for <code>mode</code>:<br> 0: You have succesfully
     * reported the user.<br> 1: Unable to locate the user.<br> 2: You may only
     * report users 10 times a day.<br> 3: You have been reported to the GM's by
     * a user.<br> 4: Your request did not go through for unknown reasons.
     * Please try again later.<br>
     *
     * @param mode The mode
     * @return Report Reponse packet
     */
    public static byte[] reportResponse(byte mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SUE_CHARACTER_RESULT.getValue());
        mplew.write(mode);
        return mplew.getPacket();
    }

    public static byte[] sendHammerData(int hammerUsed) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.VICIOUS_HAMMER.getValue());
        mplew.write(0x39);
        mplew.writeInt(0);
        mplew.writeInt(hammerUsed);
        return mplew.getPacket();
    }

    public static byte[] sendHammerMessage() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.VICIOUS_HAMMER.getValue());
        mplew.write(0x3D);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] showInfoText(String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    public static byte[] getMultiMegaphone(String[] messages, int channel, boolean showEar) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
        mplew.write(0x0A);
        if (messages[0] != null) {
            mplew.writeMapleAsciiString(messages[0]);
        }
        mplew.write(messages.length);
        for (int i = 1; i < messages.length; i++) {
            if (messages[i] != null) {
                mplew.writeMapleAsciiString(messages[i]);
            }
        }
        for (int i = 0; i < 10; i++) {
            mplew.write(channel - 1);
        }
        mplew.write(showEar ? 1 : 0);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] sendMesoLimit() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.TRADE_MONEY_LIMIT.getValue()); //Players under level 15 can only trade 1m per day
        return mplew.getPacket();
    }

    public static byte[] removeItemFromDuey(boolean remove, int Package) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARCEL.getValue());
        mplew.write(0x17);
        mplew.writeInt(Package);
        mplew.write(remove ? 3 : 4);
        return mplew.getPacket();
    }

    public static byte[] sendDueyParcelReceived(String from, boolean quick) {    // thanks inhyuk
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARCEL.getValue());
        mplew.write(0x19);
        mplew.writeMapleAsciiString(from);
        mplew.writeBool(quick);
        return mplew.getPacket();
    }

    public static byte[] sendDueyParcelNotification(boolean quick) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARCEL.getValue());
        mplew.write(0x1B);
        mplew.writeBool(quick);  // 0 : package received, 1 : quick delivery package

        return mplew.getPacket();
    }

    public static byte[] sendDueyMSG(byte operation) {
        return sendDuey(operation, null);
    }

    public static byte[] sendDuey(int operation, List<DueyPackage> packages) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARCEL.getValue());
        mplew.write(operation);
        if (operation == 8) {
            mplew.write(0);
            mplew.write(packages.size());
            for (DueyPackage dp : packages) {
                mplew.writeInt(dp.getPackageId());
                mplew.writeAsciiString(dp.getSender());
                for (int i = dp.getSender().length(); i < 13; i++) {
                    mplew.write(0);
                }
                mplew.writeInt(dp.getMesos());
                mplew.writeLong(PacketUtil.getTime(dp.sentTimeInMilliseconds()));
                mplew.writeLong(0); // Contains message o____o.
                for (int i = 0; i < 48; i++) {
                    mplew.writeInt(Randomizer.nextInt(Integer.MAX_VALUE));
                }
                mplew.writeInt(0);
                mplew.write(0);
                if (dp.getItem() != null) {
                    mplew.write(1);
                    PacketUtil.addItemInfoZeroPos(mplew, dp.getItem());
                } else {
                    mplew.write(0);
                }
            }
            mplew.write(0);
        }

        return mplew.getPacket();
    }

    public static byte[] getDojoInfo(String info) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(10);
        mplew.write(new byte[]{(byte) 0xB7, 4});//QUEST ID f5
        mplew.writeMapleAsciiString(info);
        return mplew.getPacket();
    }

    public static byte[] getDojoInfoMessage(String message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(message);
        return mplew.getPacket();
    }

    public static byte[] updateDojoStats(MapleCharacter chr, int belt) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(10);
        mplew.write(new byte[]{(byte) 0xB7, 4}); //?
        mplew.writeMapleAsciiString("pt=" + chr.getDojoPoints() + ";belt=" + belt + ";tuto=" + (chr.getFinishedDojoTutorial() ? "1" : "0"));
        return mplew.getPacket();
    }

    /**
     * Sends a "levelup" packet to the guild or family.
     *
     * Possible values for <code>type</code>:<br> 0: <Family> ? has reached Lv.
     * ?.<br> - The Reps you have received from ? will be reduced in half. 1:
     * <Family> ? has reached Lv. ?.<br> 2: <Guild> ? has reached Lv. ?.<br>
     *
     * @param type The type
     * @return The "levelup" packet.
     */
    public static byte[] levelUpMessage(int type, int level, String charname) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NOTIFY_LEVELUP.getValue());
        mplew.write(type);
        mplew.writeInt(level);
        mplew.writeMapleAsciiString(charname);

        return mplew.getPacket();
    }

    /**
     * Sends a "married" packet to the guild or family.
     *
     * Possible values for <code>type</code>:<br> 0: <Guild ? is now married.
     * Please congratulate them.<br> 1: <Family ? is now married. Please
     * congratulate them.<br>
     *
     * @param type The type
     * @return The "married" packet.
     */
    public static byte[] marriageMessage(int type, String charname) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NOTIFY_MARRIAGE.getValue());
        mplew.write(type);  // 0: guild, 1: family
        mplew.writeMapleAsciiString("> " + charname); //To fix the stupid packet lol

        return mplew.getPacket();
    }

    /**
     * Sends a "job advance" packet to the guild or family.
     *
     * Possible values for <code>type</code>:<br> 0: <Guild ? has advanced to
     * a(an) ?.<br> 1: <Family ? has advanced to a(an) ?.<br>
     *
     * @param type The type
     * @return The "job advance" packet.
     */
    public static byte[] jobMessage(int type, int job, String charname) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NOTIFY_JOB_CHANGE.getValue());
        mplew.write(type);
        mplew.writeInt(job); //Why fking int?
        mplew.writeMapleAsciiString("> " + charname); //To fix the stupid packet lol

        return mplew.getPacket();
    }

    public static byte[] getEnergy(String info, int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SESSION_VALUE.getValue());
        mplew.writeMapleAsciiString(info);
        mplew.writeMapleAsciiString(Integer.toString(amount));
        return mplew.getPacket();
    }

    public static byte[] itemExpired(int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(2);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    public static byte[] shopErrorMessage(int error, int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x0A);
        mplew.write(type);
        mplew.write(error);
        return mplew.getPacket();
    }

    public static byte[] finishedGather(int inv) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.GATHER_ITEM_RESULT.getValue());
        mplew.write(0);
        mplew.write(inv);
        return mplew.getPacket();
    }

    public static byte[] finishedSort2(int inv) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.SORT_ITEM_RESULT.getValue());
        mplew.write(0);
        mplew.write(inv);
        return mplew.getPacket();
    }

    public static byte[] customPacket(byte[] packet) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(packet.length);
        mplew.write(packet);
        return mplew.getPacket();
    }

    public static void addCashItemInformation(final MaplePacketLittleEndianWriter mplew, Item item, int accountId) {
        addCashItemInformation(mplew, item, accountId, null);
    }

    public static void addCashItemInformation(final MaplePacketLittleEndianWriter mplew, Item item, int accountId, String giftMessage) {
        boolean isGift = giftMessage != null;
        boolean isRing = false;
        Equip equip = null;
        if (item.getInventoryType().equals(MapleInventoryType.EQUIP)) {
            equip = (Equip) item;
            isRing = equip.getRingId() > -1;
        }
        mplew.writeLong(item.getPetId() > -1 ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId());
        if (!isGift) {
            mplew.writeInt(accountId);
            mplew.writeInt(0);
        }
        mplew.writeInt(item.getItemId());
        if (!isGift) {
            mplew.writeInt(item.getSN());
            mplew.writeShort(item.getQuantity());
        }
        mplew.writeAsciiString(StringUtil.getRightPaddedStr(item.getGiftFrom(), '\0', 13));
        if (isGift) {
            mplew.writeAsciiString(StringUtil.getRightPaddedStr(giftMessage, '\0', 73));
            return;
        }
        mplew.writeLong(PacketUtil.getTime(item.getExpiration()));
        mplew.writeLong(0);
    }

    public static byte[] openCashShop(MapleClient c, boolean betaCs) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_CASH_SHOP.getValue());

        PacketUtil.addCharacterInfo(mplew, c.getPlayer());
        mplew.writeBool(!betaCs); //beta cashshop boolean
        //sub_A25DB4
        if (!betaCs) {
            mplew.writeMapleAsciiString(c.getAccountName()); //if above is true write this
        }
        Set<Integer> blocked = CashItemFactory.getBlockedCashItems();
        mplew.writeInt(blocked.size());
        for (Integer serial : blocked) {
            mplew.writeInt(serial);
        }

        Set<SpecialCashItem> lsci = CashItemFactory.getSpecialCashItems();
        mplew.writeShort(lsci.size());
        for (SpecialCashItem sci : lsci) {
            mplew.writeInt(sci.getSN());
            int flag = sci.getFlag();
            mplew.writeInt(flag);
            if ((flag & CommodityFlags.ITEM_ID.getFlag()) == CommodityFlags.ITEM_ID.getFlag()) {
                mplew.writeInt(sci.getItemId());
            }
            if ((flag & CommodityFlags.COUNT.getFlag()) == CommodityFlags.COUNT.getFlag()) {
                mplew.writeShort(sci.getCount());
            }
            if ((flag & CommodityFlags.PRICE.getFlag()) == CommodityFlags.PRICE.getFlag()) {
                mplew.writeInt(sci.getPrice());
            }
            if ((flag & CommodityFlags.PRIORITY.getFlag()) == CommodityFlags.PRIORITY.getFlag()) {
                mplew.write(sci.getPriority());
            }
            if ((flag & CommodityFlags.PERIOD.getFlag()) == CommodityFlags.PERIOD.getFlag()) {
                mplew.writeShort(sci.getPeriod());
            }
            if ((flag & CommodityFlags.MAPLE_POINTS.getFlag()) == CommodityFlags.MAPLE_POINTS.getFlag()) {
                mplew.writeInt(sci.getPeriod());
            }
            if ((flag & CommodityFlags.MESOS.getFlag()) == CommodityFlags.MESOS.getFlag()) {
                mplew.writeInt(sci.getMesos());
            }
            if ((flag & CommodityFlags.PREMIUM_USER.getFlag()) == CommodityFlags.PREMIUM_USER.getFlag()) {
                mplew.writeBool(sci.isPremiumUser());
            }
            if ((flag & CommodityFlags.GENDER.getFlag()) == CommodityFlags.GENDER.getFlag()) {
                mplew.write(sci.getGender());
            }
            if ((flag & CommodityFlags.SALE.getFlag()) == CommodityFlags.SALE.getFlag()) {
                mplew.writeBool(sci.getSale());
            }
            if ((flag & CommodityFlags.CLASS.getFlag()) == CommodityFlags.CLASS.getFlag()) {
                mplew.write(sci.getJob());
            }
            if ((flag & CommodityFlags.REQUIRED_LEVEL.getFlag()) == CommodityFlags.REQUIRED_LEVEL.getFlag()) {
                mplew.writeShort(sci.getRequiredLevel());
            }
            if ((flag & CommodityFlags.CASH.getFlag()) == CommodityFlags.CASH.getFlag()) {
                mplew.writeShort(sci.getCash());
            }
            if ((flag & CommodityFlags.POINT.getFlag()) == CommodityFlags.POINT.getFlag()) {
                mplew.writeShort(sci.getPoint());
            }
            if ((flag & CommodityFlags.GIFT.getFlag()) == CommodityFlags.GIFT.getFlag()) {
                mplew.writeShort(sci.getGift());
            }
            if ((flag & CommodityFlags.PACKAGE_COUNT.getFlag()) == CommodityFlags.PACKAGE_COUNT.getFlag()) {
                mplew.write(sci.getItems().size());
                for (Integer item : sci.getItems()) {
                    mplew.writeInt(item);
                }
            }
            if ((flag & CommodityFlags.LIMIT.getFlag()) == CommodityFlags.LIMIT.getFlag()) {
                mplew.write(sci.getLimit());
            }
        }

        List<CategoryDiscount> discounts = CashItemFactory.getDiscountedCategories();
        mplew.write(discounts.size()); //size
        for (CategoryDiscount discount : discounts) {
            mplew.write(discount.getCategory());
            mplew.write(discount.getSubCategory());
            mplew.write(discount.getDiscontRate());
        }
        /*
         13333 CS_MAIN_BEST struct {char nClass;int nCommoditySN;} 8
         */
        for (int i = 0; i < 90; i++) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }

        Set<ItemStock> stock = CashItemFactory.getStock();
        mplew.writeShort(stock.size());
        for (ItemStock item : stock) {
            mplew.writeInt(item.getSN());
            mplew.writeInt(item.getStockState());
        }

        Set<LimitedGoods> goods = CashItemFactory.getLimitedGoods();
        mplew.writeShort(goods.size()); //DecodeLimitGoods
        for (LimitedGoods good : goods) {
            mplew.writeInt(good.getStartSN());
            mplew.writeInt(good.getEndSN());
            mplew.writeInt(good.getGoodsCount());
            mplew.writeInt(good.getEventSN());
            mplew.writeInt(good.getExpireDays());
            mplew.writeInt(good.getFlag());
            mplew.writeInt(good.getStartDate());
            mplew.writeInt(good.getEndDate());
            mplew.writeInt(good.getStartHour());
            mplew.writeInt(good.getEndHour());
            for (Integer day : good.getDaysOfWeek()) {
                mplew.writeInt(day);
            }
            mplew.write(new byte[36]); //Its padded for some odd reason or I am missing data
        }
        Set<LimitedGoods> zeroGoods = CashItemFactory.getLimitedGoods();
        mplew.writeShort(goods.size());
        for (LimitedGoods good : zeroGoods) {
            mplew.writeInt(good.getStartSN());
            mplew.writeInt(good.getEndSN());
            mplew.writeInt(good.getGoodsCount());
            mplew.writeInt(good.getEventSN());
            mplew.writeInt(good.getExpireDays());
            mplew.writeInt(good.getFlag());
            mplew.writeInt(good.getStartDate());
            mplew.writeInt(good.getEndDate());
            mplew.writeInt(good.getStartHour());
            mplew.writeInt(good.getEndHour());
            for (Integer day : good.getDaysOfWeek()) {
                mplew.writeInt(day);
            }
        }
        mplew.write(0); //CShopInfo::IsEventOn
        mplew.writeInt(75);
        return mplew.getPacket();
    }

    public static byte[] sendVegaScroll(int op) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.VEGA_SCROLL.getValue());
        mplew.write(op);
        return mplew.getPacket();
    }

    public static byte[] earnTitleMessage(String msg) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SCRIPT_PROGRESS_MESSAGE.getValue());
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }

    public static byte[] incubatorResult() {//lol
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(SendOpcode.INCUBATOR_RESULT.getValue());
        mplew.skip(6);
        return mplew.getPacket();
    }

    // thanks NPC Quiz packets thanks to Eric
    public static byte[] OnAskQuiz(int nSpeakerTypeID, int nSpeakerTemplateID, int nResCode, String sTitle, String sProblemText, String sHintText, int nMinInput, int nMaxInput, int tRemainInitialQuiz) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ScriptMessage.getValue());
        mplew.write(nSpeakerTypeID);
        mplew.writeInt(nSpeakerTemplateID);
        mplew.write(0x6);
        mplew.write(0);
        mplew.write(nResCode);
        if (nResCode == 0x0) {//fail has no bytes <3 
            mplew.writeMapleAsciiString(sTitle);
            mplew.writeMapleAsciiString(sProblemText);
            mplew.writeMapleAsciiString(sHintText);
            mplew.writeShort(nMinInput);
            mplew.writeShort(nMaxInput);
            mplew.writeInt(tRemainInitialQuiz);
        }
        return mplew.getPacket();
    }

    public static byte[] OnAskSpeedQuiz(int nSpeakerTypeID, int nSpeakerTemplateID, int nResCode, int nType, int dwAnswer, int nCorrect, int nRemain, int tRemainInitialQuiz) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ScriptMessage.getValue());
        mplew.write(nSpeakerTypeID);
        mplew.writeInt(nSpeakerTemplateID);
        mplew.write(0x7);
        mplew.write(0);
        mplew.write(nResCode);
        if (nResCode == 0x0) {//fail has no bytes <3 
            mplew.writeInt(nType);
            mplew.writeInt(dwAnswer);
            mplew.writeInt(nCorrect);
            mplew.writeInt(nRemain);
            mplew.writeInt(tRemainInitialQuiz);
        }
        return mplew.getPacket();
    }
}