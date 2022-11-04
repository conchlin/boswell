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
import java.sql.ResultSet;
import java.sql.SQLException;
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
import net.server.guild.MapleGuildCharacter;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import server.cashshop.CashItem;
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
import server.events.gm.MapleSnowball;
import server.life.MapleMonster;
import server.life.MobSpawnType;
import server.maps.MapleHiredMerchant;
import server.maps.MapleMap;
import server.maps.MapleMiniGame;
import server.maps.MapleMiniGame.MiniGameResult;
import server.maps.MaplePlayerShop;
import server.maps.MaplePlayerShopItem;
import server.maps.MapleSummon;
import server.life.MaplePlayerNPC;
import server.movement.LifeMovementFragment;
import server.skills.SkillMacro;
import tools.data.output.MaplePacketLittleEndianWriter;
import server.skills.Skill;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.ModifyInventory;
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
     * Gets a packet to spawn a special map object.
     *
     * @param summon
     * @param animated Animated spawn?
     * @return The spawn packet for the map object.
     */
    public static byte[] spawnSummon(MapleSummon summon, boolean animated) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(25);
        mplew.writeShort(SendOpcode.SPAWN_SPECIAL_MAPOBJECT.getValue());
        mplew.writeInt(summon.getOwner().getId());
        mplew.writeInt(summon.getObjectId());
        mplew.writeInt(summon.getSkill());
        mplew.write(0x0A); //v83
        mplew.write(summon.getSkillLevel());
        mplew.writePos(summon.getPosition());
        mplew.write(summon.getStance());    //bMoveAction & foothold, found thanks to Rien dev team
        //mplew.writeShort(0);
        mplew.writeShort(summon.getFootHold());//foothold
        mplew.write(summon.getMovementType().getValue()); // 0 = don't move, 1 = follow (4th mage summons?), 2/4 = only tele follow, 3 = bird follow
        mplew.write(summon.isPuppet() ? 0 : 1); // 0 and the summon can't attack - but puppets don't attack with 1 either ^.-
        mplew.write(animated ? 0 : 1);
        return mplew.getPacket();
    }

    /**
     * Gets a packet to remove a special map object.
     *
     * @param summon
     * @param animated Animated removal?
     * @return The packet removing the object.
     */
    public static byte[] removeSummon(MapleSummon summon, boolean animated) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        mplew.writeShort(SendOpcode.REMOVE_SPECIAL_MAPOBJECT.getValue());
        mplew.writeInt(summon.getOwner().getId());
        mplew.writeInt(summon.getObjectId());
        mplew.write(animated ? 4 : 1); // ?
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
            mplew.writeShort(SendOpcode.USER_LOCAL_EFFECT.getValue());
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

    public static byte[] movePlayer(int cid, Point p, List<LifeMovementFragment> moves) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MOVE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.writePos(p);
        PacketUtil.serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    /*
        public static byte[] summonAttack(int cid, int summonSkillId, byte direction, List<SummonAttackEntry> allDamage) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                //b2 00 29 f7 00 00 9a a3 04 00 c8 04 01 94 a3 04 00 06 ff 2b 00
                mplew.writeShort(SendOpcode.SUMMON_ATTACK.getValue());
                mplew.writeInt(cid);
                mplew.writeInt(summonSkillId);
                mplew.write(direction);
                mplew.write(4);
                mplew.write(allDamage.size());
                for (SummonAttackEntry attackEntry : allDamage) {
                        mplew.writeInt(attackEntry.getMonsterOid()); // oid
                        mplew.write(6); // who knows
                        mplew.writeInt(attackEntry.getDamage()); // damage
                }
                return mplew.getPacket();
        }
     */

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
    
    public static byte[] modifyInventory(boolean updateTick, final List<ModifyInventory> mods) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.INVENTORY_OPERATION.getValue());
        mplew.writeBool(updateTick);
        mplew.write(mods.size());
        int addMovement = -1;
        for (ModifyInventory mod : mods) {
            mplew.write(mod.getMode());
            mplew.write(mod.getInventoryType());
            mplew.writeShort(mod.getMode() == 2 ? mod.getOldPosition() : mod.getPosition());
            switch (mod.getMode()) {
                case 0 -> {//add item
                    PacketUtil.addItemInfoZeroPos(mplew, mod.getItem());
                }
                case 1 -> {//update quantity
                    mplew.writeShort(mod.getQuantity());
                }
                case 2 -> {//move
                    mplew.writeShort(mod.getPosition());
                    if (mod.getPosition() < 0 || mod.getOldPosition() < 0) {
                        addMovement = mod.getOldPosition() < 0 ? 1 : 2;
                    }
                }
                case 3 -> {//remove
                    if (mod.getPosition() < 0) {
                        addMovement = 2;
                    }
                }
                case 4 -> { //itemexp
                    Equip equip = (Equip) mod.getItem();
                    mplew.writeInt(equip.getItemExp());
                }
            }
            mod.clear();
        }
        if (addMovement > -1) {
            mplew.write(addMovement);
        }
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

    public static byte[] showAllCharacter(int chars, int unk) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        mplew.writeShort(SendOpcode.VIEW_ALL_CHAR.getValue());
        mplew.write(chars > 0 ? 1 : 5); // 2: already connected to server, 3 : unk error (view-all-characters), 5 : cannot find any
        mplew.writeInt(chars);
        mplew.writeInt(unk);
        return mplew.getPacket();
    }

    public static byte[] showAriantScoreBoard() {   // thanks lrenex for pointing match's end scoreboard packet
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ARIANT_ARENA_SHOW_RESULT.getValue());
        return mplew.getPacket();
    }

    public static byte[] updateAriantPQRanking(final MapleCharacter chr, final int score) {
        return updateAriantPQRanking(new LinkedHashMap<MapleCharacter, Integer>() {
            {
                put(chr, score);
            }
        });
    }

    public static byte[] updateAriantPQRanking(Map<MapleCharacter, Integer> playerScore) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ARIANT_ARENA_USER_SCORE.getValue());
        mplew.write(playerScore.size());
        for (Entry<MapleCharacter, Integer> e : playerScore.entrySet()) {
            mplew.writeMapleAsciiString(e.getKey().getName());
            mplew.writeInt(e.getValue());
        }
        return mplew.getPacket();
    }

    public static byte[] updateCharLook(MapleClient target, MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_CHAR_LOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        PacketUtil.addCharLook(mplew, chr, false);
        PacketUtil.addRingLook(mplew, chr, true);
        PacketUtil.addRingLook(mplew, chr, false);
        PacketUtil.addMarriageRingLook(target, mplew, chr);
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

    /**
     *
     * @param quest
     * @param npc
     * @return
     */
    public static byte[] updateQuestInfo(short quest, int npc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.QUEST_RESULT.getValue());
        mplew.write(8); //0x0A in v95
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] addQuestTimeLimit(final short quest, final int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.QUEST_RESULT.getValue());
        mplew.write(6);
        mplew.writeShort(1);//Size but meh, when will there be 2 at the same time? And it won't even replace the old one :)
        mplew.writeShort(quest);
        mplew.writeInt(time);
        return mplew.getPacket();
    }

    public static byte[] removeQuestTimeLimit(final short quest) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.QUEST_RESULT.getValue());
        mplew.write(7);
        mplew.writeShort(1);//Position
        mplew.writeShort(quest);
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
        mplew.writeShort(SendOpcode.USER_LOCAL_EFFECT.getValue());
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(0xA9);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] showOwnBerserk(int skilllevel, boolean Berserk) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.USER_LOCAL_EFFECT.getValue());
        mplew.write(1);
        mplew.writeInt(1320006);
        mplew.write(0xA9);
        mplew.write(skilllevel);
        mplew.write(Berserk ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] updateSkill(int skillid, int level, int masterlevel, long expiration) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_SKILLS.getValue());
        mplew.write(1);
        mplew.writeShort(1);
        mplew.writeInt(skillid);
        mplew.writeInt(level);
        mplew.writeInt(masterlevel);
        mplew.writeLong(PacketUtil.getTime(expiration));
        mplew.write(4);
        return mplew.getPacket();
    }

    public static byte[] getWhisper(String sender, int channel, String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.WHISPER.getValue());
        mplew.write(0x12);
        mplew.writeMapleAsciiString(sender);
        mplew.writeShort(channel - 1); // I guess this is the channel
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    /**
     *
     * @param target name of the target character
     * @param reply error code: 0x0 = cannot find char, 0x1 = success
     * @return the MaplePacket
     */
    public static byte[] getWhisperReply(String target, byte reply) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.WHISPER.getValue());
        mplew.write(0x0A); // whisper?
        mplew.writeMapleAsciiString(target);
        mplew.write(reply);
        return mplew.getPacket();
    }

    public static byte[] getInventoryFull() {
        return modifyInventory(true, Collections.<ModifyInventory>emptyList());
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

    public static byte[] showBossHP(int oid, int currHP, int maxHP, byte tagColor, byte tagBgColor) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
        mplew.write(5);
        mplew.writeInt(oid);
        mplew.writeInt(currHP);
        mplew.writeInt(maxHP);
        mplew.write(tagColor);
        mplew.write(tagBgColor);
        return mplew.getPacket();
    }

    public static byte[] customShowBossHP(byte call, int oid, long currHP, long maxHP, byte tagColor, byte tagBgColor) {
        Pair<Integer, Integer> customHP = PacketUtil.normalizedCustomMaxHP(currHP, maxHP);

        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
        mplew.write(call);
        mplew.writeInt(oid);
        mplew.writeInt(customHP.left);
        mplew.writeInt(customHP.right);
        mplew.write(tagColor);
        mplew.write(tagBgColor);
        return mplew.getPacket();
    }

    public static byte[] partyCreated(MaplePartyCharacter party, int partycharid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.write(8);
        mplew.writeInt(party.getId());

        if (party.getDoor() != null) {
            mplew.writeInt(party.getDoor().getTown().getId());
            mplew.writeInt(party.getDoor().getTarget().getId());
            mplew.writePos(party.getDoor().getTargetPosition());
        } else {
            mplew.writeInt(999999999);
            mplew.writeInt(999999999);
            mplew.writeShort(0);
            mplew.writeShort(0);
        }
        return mplew.getPacket();
    }

    public static byte[] partyInvite(MapleCharacter from) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.write(4);
        mplew.writeInt(from.getParty().getId());
        mplew.writeMapleAsciiString(from.getName());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] partySearchInvite(MapleCharacter from) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.write(4);
        mplew.writeInt(from.getParty().getId());
        mplew.writeMapleAsciiString("PS: " + from.getName());
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * 10: A beginner can't create a party. 1/5/6/11/14/19: Your request for a
     * party didn't work due to an unexpected error. 12: Quit as leader of the
     * party. 13: You have yet to join a party. 16: Already have joined a party.
     * 17: The party you're trying to join is already in full capacity. 19:
     * Unable to find the requested character in this channel. 25: Cannot kick
     * another user in this map. 28/29: Leadership can only be given to a party
     * member in the vicinity. 30: Change leadership only on same channel.
     *
     * @param message
     * @return
     */
    public static byte[] partyStatusMessage(int message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);
        return mplew.getPacket();
    }

    /**
     * 21: Player is blocking any party invitations, 22: Player is taking care
     * of another invitation, 23: Player have denied request to the party.
     *
     * @param message
     * @param charname
     * @return
     */
    public static byte[] partyStatusMessage(int message, String charname) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);
        mplew.writeMapleAsciiString(charname);
        return mplew.getPacket();
    }

    public static byte[] updateParty(int forChannel, MapleParty party, PartyOperation op, MaplePartyCharacter target) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        switch (op) {
            case DISBAND, EXPEL, LEAVE -> {
                mplew.write(0x0C);
                mplew.writeInt(party.getId());
                mplew.writeInt(target.getId());
                if (op == PartyOperation.DISBAND) {
                    mplew.write(0);
                    mplew.writeInt(party.getId());
                } else {
                    mplew.write(1);
                    if (op == PartyOperation.EXPEL) {
                        mplew.write(1);
                    } else {
                        mplew.write(0);
                    }
                    mplew.writeMapleAsciiString(target.getName());
                    PacketUtil.addPartyStatus(forChannel, party, mplew, false);
                }
            }
            case JOIN -> {
                mplew.write(0xF);
                mplew.writeInt(party.getId());
                mplew.writeMapleAsciiString(target.getName());
                PacketUtil.addPartyStatus(forChannel, party, mplew, false);
            }
            case SILENT_UPDATE, LOG_ONOFF -> {
                mplew.write(0x7);
                mplew.writeInt(party.getId());
                PacketUtil.addPartyStatus(forChannel, party, mplew, false);
            }
            case CHANGE_LEADER -> {
                mplew.write(0x1B);
                mplew.writeInt(target.getId());
                mplew.write(0);
            }
            case MYSTIC_DOOR -> {
                mplew.writeShort(0x23);
                if (target.getDoor() != null) {
                    mplew.writeInt(target.getDoor().getTown().getId());
                    mplew.writeInt(target.getDoor().getTarget().getId());
                    mplew.writePos(target.getDoor().getTargetPosition());
                } else {
                    mplew.writeInt(999999999);
                    mplew.writeInt(999999999);
                    mplew.writeInt(0);
                }
            }
        }
        return mplew.getPacket();
    }

    public static byte[] partyPortal(int townId, int targetId, Point position) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.writeShort(0x23);
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        mplew.writePos(position);
        return mplew.getPacket();
    }

    /**
     * mode: 0 buddychat; 1 partychat; 2 guildchat
     *
     * @param name
     * @param chattext
     * @param mode
     * @return
     */
    public static byte[] multiChat(String name, String chattext, int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MULTICHAT.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(name);
        mplew.writeMapleAsciiString(chattext);
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

    public static byte[] getClock(int time) { // time in seconds
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CLOCK.getValue());
        mplew.write(2); // clock type. if you send 3 here you have to send another byte (which does not matter at all) before the timestamp
        mplew.writeInt(time);
        return mplew.getPacket();
    }

    public static byte[] getClockTime(int hour, int min, int sec) { // Current Time
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CLOCK.getValue());
        mplew.write(1); //Clock-Type
        mplew.write(hour);
        mplew.write(min);
        mplew.write(sec);
        return mplew.getPacket();
    }

    public static byte[] removeClock() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.STOP_CLOCK.getValue());
        mplew.write(0);
        return mplew.getPacket();
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

    public static byte[] musicChange(String song) {
        return environmentChange(song, 6);
    }

    public static byte[] showEffect(String effect) {
        return environmentChange(effect, 3);
    }

    public static byte[] playSound(String sound) {
        return environmentChange(sound, 4);
    }

    public static byte[] environmentChange(String env, int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(env);
        return mplew.getPacket();
    }

    public static byte[] environmentMove(String env, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendOpcode.FIELD_OBSTACLE_ONOFF.getValue());
        mplew.writeMapleAsciiString(env);
        mplew.writeInt(mode);   // 0: stop and back to start, 1: move

        return mplew.getPacket();
    }

    public static byte[] environmentMoveList(Set<Entry<String, Integer>> envList) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FIELD_OBSTACLE_ONOFF_LIST.getValue());
        mplew.writeInt(envList.size());

        for (Entry<String, Integer> envMove : envList) {
            mplew.writeMapleAsciiString(envMove.getKey());
            mplew.writeInt(envMove.getValue());
        }

        return mplew.getPacket();
    }

    public static byte[] environmentMoveReset() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FIELD_OBSTACLE_ALL_RESET.getValue());
        return mplew.getPacket();
    }

    public static byte[] startMapEffect(String msg, int itemid, boolean active) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BLOW_WEATHER.getValue());
        mplew.write(active ? 0 : 1);
        mplew.writeInt(itemid);
        if (active) {
            mplew.writeMapleAsciiString(msg);
        }
        return mplew.getPacket();
    }

    public static byte[] removeMapEffect() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BLOW_WEATHER.getValue());
        mplew.write(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] mapEffect(String path) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
        mplew.write(3);
        mplew.writeMapleAsciiString(path);
        return mplew.getPacket();
    }

    public static byte[] mapSound(String path) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
        mplew.write(4);
        mplew.writeMapleAsciiString(path);
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

    /*
        public static byte[] sendSpouseChat(MapleCharacter partner, String msg) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SPOUSE_CHAT.getValue());
                mplew.writeMapleAsciiString(partner.getName());
                mplew.writeMapleAsciiString(msg);
                return mplew.getPacket();
        }
     */
    public static byte[] OnCoupleMessage(String fiance, String text, boolean spouse) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPOUSE_CHAT.getValue());
        mplew.write(spouse ? 5 : 4); // v2 = CInPacket::Decode1(a1) - 4;
        if (spouse) { // if ( v2 ) {
            mplew.writeMapleAsciiString(fiance);
        }
        mplew.write(spouse ? 5 : 1);
        mplew.writeMapleAsciiString(text);
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

    public static byte[] showForcedEquip(int team) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FORCED_MAP_EQUIP.getValue());
        if (team > -1) {
            mplew.write(team);   // 00 = red, 01 = blue
        }
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

    /*  1: cannot find char info,
            2: cannot transfer under 20,
            3: cannot send banned,
            4: cannot send married,
            5: cannot send guild leader,
            6: cannot send if account already requested transfer,
            7: cannot transfer within 30days,
            8: must quit family,
            9: unknown error
     */
    public static byte[] sendWorldTransferRules(int error) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_CHECK_TRANSFER_WORLD_POSSIBLE_RESULT.getValue());
        mplew.writeInt(0);
        mplew.write(0);
        mplew.write(error);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /*  1: name change already submitted
            2: name change within a month
            3: recently banned
            4: unknown error
     */
    public static byte[] sendNameTransferRules(int error) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_CHECK_NAME_CHANGE_POSSIBLE_RESULT.getValue());
        mplew.writeInt(0);
        mplew.write(error);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] sendNameTransferCheck(boolean canUseName) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_CHECK_NAME_CHANGE.getValue());
        mplew.writeShort(0);
        mplew.writeBool(!canUseName);

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

    public static byte[] showCouponRedeemedItem(int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
        mplew.writeShort(0x49); //v72
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeShort(1);
        mplew.writeShort(0x1A);
        mplew.writeInt(itemid);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] showCash(MapleCharacter mc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.QUERY_CASH_RESULT.getValue());

        mplew.writeInt(mc.getCashShop().getCash(1));
        mplew.writeInt(mc.getCashShop().getCash(2));
        mplew.writeInt(mc.getCashShop().getCash(4));

        return mplew.getPacket();
    }

    public static byte[] enableCSUse(MapleCharacter mc) {
        return showCash(mc);
    }

    /**
     *
     * @param target
     * @param mapid
     * @param MTSmapCSchannel 0: MTS 1: Map 2: CS 3: Different Channel
     * @return
     */
    public static byte[] getFindReply(String target, int mapid, int MTSmapCSchannel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.WHISPER.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(target);
        mplew.write(MTSmapCSchannel); // 0: mts 1: map 2: cs
        mplew.writeInt(mapid); // -1 if mts, cs
        if (MTSmapCSchannel == 1) {
            mplew.write(new byte[8]);
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param target
     * @param mapid
     * @param MTSmapCSchannel 0: MTS 1: Map 2: CS 3: Different Channel
     * @return
     */
    public static byte[] getBuddyFindReply(String target, int mapid, int MTSmapCSchannel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.WHISPER.getValue());
        mplew.write(72);
        mplew.writeMapleAsciiString(target);
        mplew.write(MTSmapCSchannel); // 0: mts 1: map 2: cs
        mplew.writeInt(mapid); // -1 if mts, cs
        if (MTSmapCSchannel == 1) {
            mplew.write(new byte[8]);
        }
        return mplew.getPacket();
    }

    public static byte[] showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.OX_QUIZ.getValue());
        mplew.write(askQuestion ? 1 : 0);
        mplew.write(questionSet);
        mplew.writeShort(questionId);
        return mplew.getPacket();
    }

    public static byte[] updateGender(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.SET_GENDER.getValue());
        mplew.write(chr.getGender());
        return mplew.getPacket();
    }

    public static byte[] loadFamily(MapleCharacter player) {
        String[] title = {"Family Reunion", "Summon Family", "My Drop Rate 1.5x (15 min)", "My EXP 1.5x (15 min)", "Family Bonding (30 min)", "My Drop Rate 2x (15 min)", "My EXP 2x (15 min)", "My Drop Rate 2x (30 min)", "My EXP 2x (30 min)", "My Party Drop Rate 2x (30 min)", "My Party EXP 2x (30 min)"};
        String[] description = {"[Target] Me\n[Effect] Teleport directly to the Family member of your choice.", "[Target] 1 Family member\n[Effect] Summon a Family member of choice to the map you're in.", "[Target] Me\n[Time] 15 min.\n[Effect] Monster drop rate will be increased #c1.5x#.\n*  If the Drop Rate event is in progress, this will be nullified.", "[Target] Me\n[Time] 15 min.\n[Effect] EXP earned from hunting will be increased #c1.5x#.\n* If the EXP event is in progress, this will be nullified.", "[Target] At least 6 Family members online that are below me in the Pedigree\n[Time] 30 min.\n[Effect] Monster drop rate and EXP earned will be increased #c2x#. \n* If the EXP event is in progress, this will be nullified.", "[Target] Me\n[Time] 15 min.\n[Effect] Monster drop rate will be increased #c2x#.\n* If the Drop Rate event is in progress, this will be nullified.", "[Target] Me\n[Time] 15 min.\n[Effect] EXP earned from hunting will be increased #c2x#.\n* If the EXP event is in progress, this will be nullified.", "[Target] Me\n[Time] 30 min.\n[Effect] Monster drop rate will be increased #c2x#.\n* If the Drop Rate event is in progress, this will be nullified.", "[Target] Me\n[Time] 30 min.\n[Effect] EXP earned from hunting will be increased #c2x#. \n* If the EXP event is in progress, this will be nullified.", "[Target] My party\n[Time] 30 min.\n[Effect] Monster drop rate will be increased #c2x#.\n* If the Drop Rate event is in progress, this will be nullified.", "[Target] My party\n[Time] 30 min.\n[Effect] EXP earned from hunting will be increased #c2x#.\n* If the EXP event is in progress, this will be nullified."};
        int[] repCost = {3, 5, 7, 8, 10, 12, 15, 20, 25, 40, 50};
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAMILY_PRIVILEGE_LIST.getValue());
        mplew.writeInt(11);
        for (int i = 0; i < 11; i++) {
            mplew.write(i > 4 ? (i % 2) + 1 : i);
            mplew.writeInt(repCost[i] * 100);
            mplew.writeInt(1);
            mplew.writeMapleAsciiString(title[i]);
            mplew.writeMapleAsciiString(description[i]);
        }
        return mplew.getPacket();
    }

    /**
     * Family Result Message
     *
     * Possible values for <code>type</code>:<br>
     * 67: You do not belong to the same family.<br>
     * 69: The character you wish to add as\r\na Junior must be in the same
     * map.<br>
     * 70: This character is already a Junior of another character.<br>
     * 71: The Junior you wish to add\r\nmust be at a lower rank.<br>
     * 72: The gap between you and your\r\njunior must be within 20 levels.<br>
     * 73: Another character has requested to add this character.\r\nPlease try
     * again later.<br>
     * 74: Another character has requested a summon.\r\nPlease try again
     * later.<br>
     * 75: The summons has failed. Your current location or state does not allow
     * a summons.<br>
     * 76: The family cannot extend more than 1000 generations from above and
     * below.<br>
     * 77: The Junior you wish to add\r\nmust be over Level 10.<br>
     * 78: You cannot add a Junior \r\nthat has requested to change worlds.<br>
     * 79: You cannot add a Junior \r\nsince you've requested to change
     * worlds.<br>
     * 80: Separation is not possible due to insufficient Mesos.\r\nYou will
     * need %d Mesos to\r\nseparate with a Senior.<br>
     * 81: Separation is not possible due to insufficient Mesos.\r\nYou will
     * need %d Mesos to\r\nseparate with a Junior.<br>
     * 82: The Entitlement does not apply because your level does not match the
     * corresponding area.<br>
     *
     * @param type The type
     * @return Family Result packet
     */
    public static byte[] sendFamilyMessage(int type, int mesos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.FAMILY_RESULT.getValue());
        mplew.writeInt(type);
        mplew.writeInt(mesos);
        return mplew.getPacket();
    }

    public static byte[] getFamilyInfo(MapleFamilyEntry f) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAMILY_INFO_RESULT.getValue());
        mplew.writeInt(f.getReputation()); // cur rep left
        mplew.writeInt(f.getTotalReputation()); // tot rep left
        mplew.writeInt(f.getTodaysRep()); // todays rep
        mplew.writeShort(f.getJuniors()); // juniors added
        mplew.writeShort(f.getTotalJuniors()); // juniors allowed
        mplew.writeShort(0); //Unknown
        mplew.writeInt(f.getId()); // id?
        mplew.writeMapleAsciiString(f.getFamilyName());
        mplew.writeInt(0);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static byte[] showPedigree(int chrid, Map<Integer, MapleFamilyEntry> members) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAMILY_CHART_RESULT.getValue());
        //Hmmm xD
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

    public static byte[] updateQuestFinish(short quest, int npc, short nextquest) { //Check
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.QUEST_RESULT.getValue()); //0xF2 in v95
        mplew.write(8);//0x0A in v95
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeShort(nextquest);
        return mplew.getPacket();
    }

    public static byte[] showInfoText(String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    public static byte[] questError(short quest) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.QUEST_RESULT.getValue());
        mplew.write(0x0A);
        mplew.writeShort(quest);
        return mplew.getPacket();
    }

    public static byte[] questFailure(byte type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.QUEST_RESULT.getValue());
        mplew.write(type);//0x0B = No meso, 0x0D = Worn by character, 0x0E = Not having the item ?
        return mplew.getPacket();
    }

    public static byte[] questExpire(short quest) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.QUEST_RESULT.getValue());
        mplew.write(0x0F);
        mplew.writeShort(quest);
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

    /**
     * Gets a gm effect packet (ie. hide, banned, etc.)
     *
     * Possible values for <code>type</code>:<br> 0x04: You have successfully
     * blocked access.<br>
     * 0x05: The unblocking has been successful.<br> 0x06 with Mode 0: You have
     * successfully removed the name from the ranks.<br> 0x06 with Mode 1: You
     * have entered an invalid character name.<br> 0x10: GM Hide, mode
     * determines whether or not it is on.<br> 0x1E: Mode 0: Failed to send
     * warning Mode 1: Sent warning<br> 0x13 with Mode 0: + mapid 0x13 with Mode
     * 1: + ch (FF = Unable to find merchant)
     *
     * @param type The type
     * @param mode The mode
     * @return The gm effect packet
     */
    public static byte[] getGMEffect(int type, byte mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ADMIN_RESULT.getValue());
        mplew.write(type);
        mplew.write(mode);
        return mplew.getPacket();
    }

    public static byte[] findMerchantResponse(boolean map, int extra) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ADMIN_RESULT.getValue());
        mplew.write(0x13);
        mplew.write(map ? 0 : 1); //00 = mapid, 01 = ch
        if (map) {
            mplew.writeInt(extra);
        } else {
            mplew.write(extra); //-1 = unable to find
        }
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] disableMinimap() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ADMIN_RESULT.getValue());
        mplew.writeShort(0x1C);
        return mplew.getPacket();
    }

    public static byte[] sendFamilyInvite(int playerId, String inviter) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAMILY_JOIN_REQUEST.getValue());
        mplew.writeInt(playerId);
        mplew.writeMapleAsciiString(inviter);
        return mplew.getPacket();
    }

    public static byte[] showBoughtCashPackage(List<Item> cashPackage, int accountId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x89);
        mplew.write(cashPackage.size());

        for (Item item : cashPackage) {
            addCashItemInformation(mplew, item, accountId);
        }

        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] showBoughtQuestItem(int itemId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x8D);
        mplew.writeInt(1);
        mplew.writeShort(1);
        mplew.write(0x0B);
        mplew.write(0);
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    // Cash Shop Surprise packets found thanks to Arnah (Vertisy)
    public static byte[] onCashItemGachaponOpenFailed() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_CASH_ITEM_GACHAPON_RESULT.getValue());
        mplew.write(189);
        return mplew.getPacket();
    }

    public static byte[] onCashGachaponOpenSuccess(int accountid, long sn, int remainingBoxes, Item item, int itemid, int nSelectedItemCount, boolean bJackpot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_CASH_ITEM_GACHAPON_RESULT.getValue());
        mplew.write(190);
        mplew.writeLong(sn);// sn of the box used
        mplew.writeInt(remainingBoxes);
        addCashItemInformation(mplew, item, accountid);
        mplew.writeInt(itemid);// the itemid of the liSN?
        mplew.write(nSelectedItemCount);// the total count now? o.O
        mplew.writeBool(bJackpot);// "CashGachaponJackpot"
        return mplew.getPacket();
    }

    public static byte[] getAllianceInfo(MapleAlliance alliance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0C);
        mplew.write(1);
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRankTitle(i));
        }
        mplew.write(alliance.getGuilds().size());
        mplew.writeInt(alliance.getCapacity()); // probably capacity
        for (Integer guild : alliance.getGuilds()) {
            mplew.writeInt(guild);
        }
        mplew.writeMapleAsciiString(alliance.getNotice());
        return mplew.getPacket();
    }

    public static byte[] updateAllianceInfo(MapleAlliance alliance, int world) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0F);
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRankTitle(i));
        }
        mplew.write(alliance.getGuilds().size());
        for (Integer guild : alliance.getGuilds()) {
            mplew.writeInt(guild);
        }
        mplew.writeInt(alliance.getCapacity()); // probably capacity
        mplew.writeShort(0);
        for (Integer guildid : alliance.getGuilds()) {
            PacketUtil.getGuildInfo(mplew, Server.getInstance().getGuild(guildid, world));
        }
        return mplew.getPacket();
    }

    public static byte[] getGuildAlliances(MapleAlliance alliance, int worldId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0D);
        mplew.writeInt(alliance.getGuilds().size());
        for (Integer guild : alliance.getGuilds()) {
            PacketUtil.getGuildInfo(mplew, Server.getInstance().getGuild(guild, worldId));
        }
        return mplew.getPacket();
    }

    public static byte[] addGuildToAlliance(MapleAlliance alliance, int newGuild, MapleClient c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x12);
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRankTitle(i));
        }
        mplew.write(alliance.getGuilds().size());
        for (Integer guild : alliance.getGuilds()) {
            mplew.writeInt(guild);
        }
        mplew.writeInt(alliance.getCapacity());
        mplew.writeMapleAsciiString(alliance.getNotice());
        mplew.writeInt(newGuild);
        PacketUtil.getGuildInfo(mplew, Server.getInstance().getGuild(newGuild, c.getWorld(), null));
        return mplew.getPacket();
    }

    public static byte[] allianceMemberOnline(MapleCharacter mc, boolean online) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0E);
        mplew.writeInt(mc.getGuild().getAllianceId());
        mplew.writeInt(mc.getGuildId());
        mplew.writeInt(mc.getId());
        mplew.write(online ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] allianceNotice(int id, String notice) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x1C);
        mplew.writeInt(id);
        mplew.writeMapleAsciiString(notice);
        return mplew.getPacket();
    }

    public static byte[] changeAllianceRankTitle(int alliance, String[] ranks) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x1A);
        mplew.writeInt(alliance);
        for (int i = 0; i < 5; i++) {
            mplew.writeMapleAsciiString(ranks[i]);
        }
        return mplew.getPacket();
    }

    public static byte[] updateAllianceJobLevel(MapleCharacter mc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x18);
        mplew.writeInt(mc.getGuild().getAllianceId());
        mplew.writeInt(mc.getGuildId());
        mplew.writeInt(mc.getId());
        mplew.writeInt(mc.getLevel());
        mplew.writeInt(mc.getJob().getId());
        return mplew.getPacket();
    }

    public static byte[] removeGuildFromAlliance(MapleAlliance alliance, int expelledGuild, int worldId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x10);
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRankTitle(i));
        }
        mplew.write(alliance.getGuilds().size());
        for (Integer guild : alliance.getGuilds()) {
            mplew.writeInt(guild);
        }
        mplew.writeInt(alliance.getCapacity());
        mplew.writeMapleAsciiString(alliance.getNotice());
        mplew.writeInt(expelledGuild);
        PacketUtil.getGuildInfo(mplew, Server.getInstance().getGuild(expelledGuild, worldId, null));
        mplew.write(0x01);
        return mplew.getPacket();
    }

    public static byte[] disbandAlliance(int alliance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x1D);
        mplew.writeInt(alliance);
        return mplew.getPacket();
    }

    public static byte[] allianceInvite(int allianceid, MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x03);
        mplew.writeInt(allianceid);
        mplew.writeMapleAsciiString(chr.getName());
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static byte[] sendMesoLimit() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.TRADE_MONEY_LIMIT.getValue()); //Players under level 15 can only trade 1m per day
        return mplew.getPacket();
    }

    public static byte[] sendFamilyJoinResponse(boolean accepted, String added) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAMILY_JOIN_REQUEST_RESULT.getValue());
        mplew.write(accepted ? 1 : 0);
        mplew.writeMapleAsciiString(added);
        return mplew.getPacket();
    }

    public static byte[] getSeniorMessage(String name) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAMILY_JOIN_ACCEPTED.getValue());
        mplew.writeMapleAsciiString(name);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] sendGainRep(int gain, int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAMILY_FAMOUS_POINT_INC_RESULT.getValue());
        mplew.writeInt(gain);
        mplew.writeShort(0);
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

    public static byte[] sendDojoAnimation(byte firstByte, String animation) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
        mplew.write(firstByte);
        mplew.writeMapleAsciiString(animation);
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

    /**
     * Gets a "block" packet (ie. the cash shop is unavailable, etc)
     *
     * Possible values for <code>type</code>:<br> 1: The portal is closed for
     * now.<br> 2: You cannot go to that place.<br> 3: Unable to approach due to
     * the force of the ground.<br> 4: You cannot teleport to or on this
     * map.<br> 5: Unable to approach due to the force of the ground.<br> 6:
     * This map can only be entered by party members.<br> 7: The Cash Shop is
     * currently not available. Stay tuned...<br>
     *
     * @param type The type
     * @return The "block" packet.
     */
    public static byte[] blockedMessage(int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BLOCKED_MAP.getValue());
        mplew.write(type);
        return mplew.getPacket();
    }

    /**
     * Gets a "block" packet (ie. the cash shop is unavailable, etc)
     *
     * Possible values for <code>type</code>:<br> 1: You cannot move that
     * channel. Please try again later.<br> 2: You cannot go into the cash shop.
     * Please try again later.<br> 3: The Item-Trading Shop is currently
     * unavailable. Please try again later.<br> 4: You cannot go into the trade
     * shop, due to limitation of user count.<br> 5: You do not meet the minimum
     * level requirement to access the Trade Shop.<br>
     *
     * @param type The type
     * @return The "block" packet.
     */
    public static byte[] blockedMessage2(int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BLOCKED_SERVER.getValue());
        mplew.write(type);
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

    /**
     *
     * @param type - (0:Light&Long 1:Heavy&Short)
     * @param delay - seconds
     * @return
     */
    public static byte[] trembleEffect(int type, int delay) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
        mplew.write(1);
        mplew.write(type);
        mplew.writeInt(delay);
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

    public static byte[] MobDamageMobFriendly(MapleMonster mob, int damage, int remainingHp) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DAMAGE_MONSTER.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.write(1); // direction ?
        mplew.writeInt(damage);

        mplew.writeInt(remainingHp);
        mplew.writeInt(mob.getMaxHp());
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

    public static byte[] bunnyPacket() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeAsciiString("Protect the Moon Bunny!!!");
        return mplew.getPacket();
    }

    public static byte[] hpqMessage(String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BLOW_WEATHER.getValue()); // not 100% sure
        mplew.write(0);
        mplew.writeInt(5120016);
        mplew.writeAsciiString(text);
        return mplew.getPacket();
    }

    public static byte[] showEventInstructions() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GMEVENT_INSTRUCTIONS.getValue());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] leftKnockBack() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
        mplew.writeShort(SendOpcode.LEFT_KNOCK_BACK.getValue());
        return mplew.getPacket();
    }

    public static byte[] rollSnowBall(boolean entermap, int state, MapleSnowball ball0, MapleSnowball ball1) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SNOWBALL_STATE.getValue());
        if (entermap) {
            mplew.skip(21);
        } else {
            mplew.write(state);// 0 = move, 1 = roll, 2 is down disappear, 3 is up disappear
            mplew.writeInt(ball0.getSnowmanHP() / 75);
            mplew.writeInt(ball1.getSnowmanHP() / 75);
            mplew.writeShort(ball0.getPosition());//distance snowball down, 84 03 = max
            mplew.write(-1);
            mplew.writeShort(ball1.getPosition());//distance snowball up, 84 03 = max
            mplew.write(-1);
        }
        return mplew.getPacket();
    }

    public static byte[] hitSnowBall(int what, int damage) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.HIT_SNOWBALL.getValue());
        mplew.write(what);
        mplew.writeInt(damage);
        return mplew.getPacket();
    }

    /**
     * Sends a Snowball Message<br>
     *
     * Possible values for <code>message</code>:<br> 1: ... Team's snowball has
     * passed the stage 1.<br> 2: ... Team's snowball has passed the stage
     * 2.<br> 3: ... Team's snowball has passed the stage 3.<br> 4: ... Team is
     * attacking the snowman, stopping the progress<br> 5: ... Team is moving
     * again<br>
     *
     * @param message
     *
     */
    public static byte[] snowballMessage(int team, int message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.SNOWBALL_MESSAGE.getValue());
        mplew.write(team);// 0 is down, 1 is up
        mplew.writeInt(message);
        return mplew.getPacket();
    }

    public static byte[] coconutScore(int team1, int team2) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.COCONUT_SCORE.getValue());
        mplew.writeShort(team1);
        mplew.writeShort(team2);
        return mplew.getPacket();
    }

    public static byte[] hitCoconut(boolean spawn, int id, int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.COCONUT_HIT.getValue());
        if (spawn) {
            mplew.writeShort(-1);
            mplew.writeShort(5000);
            mplew.write(0);
        } else {
            mplew.writeShort(id);
            mplew.writeShort(1000);//delay till you can attack again! 
            mplew.write(type); // What action to do for the coconut.
        }
        return mplew.getPacket();
    }

    public static byte[] customPacket(String packet) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(HexTool.getByteArrayFromHexString(packet));
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

    public static byte[] showWishList(MapleCharacter mc, boolean update) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        if (update) {
            mplew.write(0x55);
        } else {
            mplew.write(0x4F);
        }

        for (int sn : mc.getCashShop().getWishList()) {
            mplew.writeInt(sn);
        }

        for (int i = mc.getCashShop().getWishList().size(); i < 10; i++) {
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    public static byte[] showBoughtCashItem(Item item, int accountId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x57);
        addCashItemInformation(mplew, item, accountId);

        return mplew.getPacket();
    }

    /*
         * 00 = Due to an unknown error, failed
         * A4 = Due to an unknown error, failed + warpout
         * A5 = You don't have enough cash.
         * A6 = long as shet msg
         * A7 = You have exceeded the allotted limit of price for gifts.
         * A8 = You cannot send a gift to your own account. Log in on the char and purchase
         * A9 = Please confirm whether the character's name is correct.
         * AA = Gender restriction!
         * AB = gift cannot be sent because recipient inv is full
         * AC = exceeded the number of cash items you can have
         * AD = check and see if the character name is wrong or there is gender restrictions
         * //Skipped a few
         * B0 = Wrong Coupon Code
         * B1 = Disconnect from CS because of 3 wrong coupon codes < lol
         * B2 = Expired Coupon
         * B3 = Coupon has been used already
         * B4 = Nexon internet cafes? lolfk
         * BB = inv full
         * BC = long as shet "(not?) available to purchase by a use at the premium" msg
         * BD = invalid gift recipient
         * BE = invalid receiver name
         * BF = item unavailable to purchase at this hour
         * C0 = not enough items in stock, therefore not available
         * C1 = you have exceeded spending limit of NX
         * C2 = not enough mesos? Lol not even 1 mesos xD
         * C3 = cash shop unavailable during beta phase
         * C4 = check birthday code
         * C7 = only available to users buying cash item, whatever msg too long
         * C8 = already applied for this
         * D2 = coupon system currently unavailable
         * D3 = item can only be used 15 days after registration
         * D4 = not enough gift tokens
         * D6 = fresh people cannot gift items lul
         * D7 = bad people cannot gift items >:(
         * D8 = cannot gift due to limitations
         * D9 = cannot gift due to amount of gifted times
         * DA = cannot be gifted due to technical difficulties
         * DB = cannot transfer to char below level 20
         * DC = cannot transfer char to same world
         * DD = cannot transfer char to new server world
         * DE = cannot transfer char out of this world
         * DF = cannot transfer char due to no empty char slots
         * E0 = event or free test time ended
         * E6 = item cannot be purchased with MaplePoints
         * E7 = lol sorry for the inconvenience, eh?
         * E8 = cannot be purchased by anyone under 7
     */
    public static byte[] showCashShopMessage(byte message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x5C);
        mplew.write(message);

        return mplew.getPacket();
    }

    public static byte[] showCashInventory(MapleClient c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x4B);
        mplew.writeShort(c.getPlayer().getCashShop().getInventory().size());

        for (Item item : c.getPlayer().getCashShop().getInventory()) {
            addCashItemInformation(mplew, item, c.getAccID());
        }

        mplew.writeShort(c.getPlayer().getStorage().getSlots());
        mplew.writeShort(c.getCharacterSlots());

        return mplew.getPacket();
    }

    public static byte[] showGifts(List<Pair<Item, String>> gifts) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x4D);
        mplew.writeShort(gifts.size());

        for (Pair<Item, String> gift : gifts) {
            addCashItemInformation(mplew, gift.getLeft(), 0, gift.getRight());
        }

        return mplew.getPacket();
    }

    public static byte[] showGiftSucceed(String to, CashItem item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x5E); //0x5D, Couldn't be sent
        mplew.writeMapleAsciiString(to);
        mplew.writeInt(item.getItemId());
        mplew.writeShort(item.getCount());
        mplew.writeInt(item.getPrice());

        return mplew.getPacket();
    }

    public static byte[] showBoughtInventorySlots(int type, short slots) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x60);
        mplew.write(type);
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    public static byte[] showBoughtStorageSlots(short slots) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x62);
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    public static byte[] showBoughtCharacterSlot(short slots) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x64);
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    public static byte[] takeFromCashInventory(Item item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x68);
        mplew.writeShort(item.getPosition());
        PacketUtil.addItemInfoZeroPos(mplew, item);

        return mplew.getPacket();
    }

    public static byte[] putIntoCashInventory(Item item, int accountId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x6A);
        addCashItemInformation(mplew, item, accountId);

        return mplew.getPacket();
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

    public static byte[] CPUpdate(boolean party, int curCP, int totalCP, int team) { // CPQ
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (!party) {
            mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_OBTAINED_CP.getValue());
        } else {
            mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_PARTY_CP.getValue());
            mplew.write(team); // team?
        }
        mplew.writeShort(curCP);
        mplew.writeShort(totalCP);
        return mplew.getPacket();
    }

    public static byte[] CPQMessage(byte message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_MESSAGE.getValue());
        mplew.write(message); // Message
        return mplew.getPacket();
    }

    public static byte[] playerSummoned(String name, int tab, int number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_SUMMON.getValue());
        mplew.write(tab);
        mplew.write(number);
        mplew.writeMapleAsciiString(name);
        return mplew.getPacket();
    }

    public static byte[] playerDiedMessage(String name, int lostCP, int team) { // CPQ
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_DIED.getValue());
        mplew.write(team); // team
        mplew.writeMapleAsciiString(name);
        mplew.write(lostCP);
        return mplew.getPacket();
    }

    public static byte[] startMonsterCarnival(MapleCharacter chr, int team, int oposition) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(25);
        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_START.getValue());
        mplew.write(team); // team
        mplew.writeShort(chr.getCP()); // Obtained CP - Used CP
        mplew.writeShort(chr.getTotalCP()); // Total Obtained CP
        mplew.writeShort(chr.getMonsterCarnival().getCP(team)); // Obtained CP - Used CP of the team
        mplew.writeShort(chr.getMonsterCarnival().getTotalCP(team)); // Total Obtained CP of the team
        mplew.writeShort(chr.getMonsterCarnival().getCP(oposition)); // Obtained CP - Used CP of the team
        mplew.writeShort(chr.getMonsterCarnival().getTotalCP(oposition)); // Total Obtained CP of the team
        mplew.writeShort(0); // Probably useless nexon shit
        mplew.writeLong(0); // Probably useless nexon shit
        return mplew.getPacket();
    }

 /*   public static byte[] sheepRanchInfo(byte wolf, byte sheep) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHEEP_RANCH_INFO.getValue());
        mplew.write(wolf);
        mplew.write(sheep);
        return mplew.getPacket();
    }
    //Know what this is? ?? >=)

    public static byte[] sheepRanchClothes(int id, byte clothes) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHEEP_RANCH_CLOTHES.getValue());
        mplew.writeInt(id); //Character id
        mplew.write(clothes); //0 = sheep, 1 = wolf, 2 = Spectator (wolf without wool)
        return mplew.getPacket();
    }*/

    public static byte[] incubatorResult() {//lol
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(SendOpcode.INCUBATOR_RESULT.getValue());
        mplew.skip(6);
        return mplew.getPacket();
    }

    public static byte[] pyramidGauge(int gauge) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.PYRAMID_GAUGE.getValue());
        mplew.writeInt(gauge);
        return mplew.getPacket();
    }
    // f2

    public static byte[] pyramidScore(byte score, int exp) {//Type cannot be higher than 4 (Rank D), otherwise you'll crash
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.PYRAMID_SCORE.getValue());
        mplew.write(score);
        mplew.writeInt(exp);
        return mplew.getPacket();
    }

    // thanks NPC Quiz packets thanks to Eric
    public static byte[] OnAskQuiz(int nSpeakerTypeID, int nSpeakerTemplateID, int nResCode, String sTitle, String sProblemText, String sHintText, int nMinInput, int nMaxInput, int tRemainInitialQuiz) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NPC_TALK.getValue());
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
        mplew.writeShort(SendOpcode.NPC_TALK.getValue());
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

    public static byte[] updateWitchTowerScore(int score) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.WITCH_TOWER_SCORE_UPDATE.getValue());
        mplew.write(score);
        return mplew.getPacket();
    }
}
