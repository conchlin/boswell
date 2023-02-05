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
package scripting.npc;

import java.io.File;
import java.sql.SQLException;

import client.inventory.manipulator.MapleInventoryManipulator;
import constants.ServerConstants;
import enums.AllianceResultType;
import enums.BroadcastMessageType;
import enums.FieldEffectType;
import net.server.Server;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import network.packet.context.BroadcastMsgPacket;
import network.packet.field.CField;
import network.packet.ScriptMan;
import network.packet.StoreBank;
import network.packet.context.WvsContext;
import network.packet.context.AlliancePacket;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import script.ScriptMessageType;
import scripting.AbstractPlayerInteraction;
import server.*;
import server.events.gm.MapleEvent;
import server.gachapon.MapleGach;
import server.gachapon.MapleGach.MapleGachaponItem;
import server.life.MaplePlayerNPC;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.event.AramiaTree;
import server.partyquest.Pyramid;
import server.partyquest.Pyramid.PyramidMode;
import server.skills.PlayerSkill;
import server.skills.SkillFactory;
import tools.LogHelper;
import tools.MaplePacketCreator;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleSkinColor;
import client.MapleStat;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MaplePet;
import constants.GameConstants;
import constants.ItemConstants;
import constants.LanguageConstants;
import net.server.PlayerStorage;
import net.server.channel.Channel;
import net.server.coordinator.matchchecker.MatchCheckerListenerFactory.MatchCheckerType;
import server.MapleSkillbookInformationProvider.SkillBookEntry;
import server.life.MapleLifeFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.expeditions.MapleExpedition;
import server.expeditions.MapleExpeditionType;
import server.partyquest.AriantColiseum;
import server.partyquest.MonsterCarnival;
import tools.packets.Wedding;
import server.partyquest.PQTour;

import java.awt.Point;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Matze
 */
public class NPCConversationManager extends AbstractPlayerInteraction {

    private int npc;
    private int npcOid;
    private String scriptName;
    private String getText;
    private boolean itemScript;
    private List < MaplePartyCharacter > otherParty;

    private static Map < Integer, String > npcDefaultTalks = new HashMap < > ();

    private static String getDefaultTalk(int npcid) {
        String talk = npcDefaultTalks.get(npcid);
        if (talk == null) {
            talk = MapleLifeFactory.getNPCDefaultTalk(npcid);
            npcDefaultTalks.put(npcid, talk);
        }

        return talk;
    }

    public NPCConversationManager(MapleClient c, int npc, String scriptName) {
        this(c, npc, -1, scriptName, false);
    }

    public NPCConversationManager(MapleClient c, int npc, List < MaplePartyCharacter > otherParty, boolean test) {
        super(c);
        this.c = c;
        this.npc = npc;
        this.otherParty = otherParty;
    }

    public NPCConversationManager(MapleClient c, int npc, int oid, String scriptName, boolean itemScript) {
        super(c);
        this.npc = npc;
        this.npcOid = oid;
        this.scriptName = scriptName;
        this.itemScript = itemScript;
    }

    public int getNpc() {
        return npc;
    }

    public int getNpcObjectId() {
        return npcOid;
    }

    public String getScriptName() {
        return scriptName;
    }

    public boolean isItemScript() {
        return itemScript;
    }

    public void resetItemScript() {
        this.itemScript = false;
    }

    public void dispose() {
        NPCScriptManager.getInstance().dispose(this);
        getClient().announce(WvsContext.Packet.enableActions());
    }

    public void sendNext(String text) {
        getClient().announce(ScriptMan.Packet.getNPCTalk(npc, (byte) 0, text, "00 01", (byte) 0));
    }

    public void sendPrev(String text) {
        getClient().announce(ScriptMan.Packet.getNPCTalk(npc, (byte) 0, text, "01 00", (byte) 0));
    }

    public void sendNextPrev(String text) {
        getClient().announce(ScriptMan.Packet.getNPCTalk(npc, (byte) 0, text, "01 01", (byte) 0));
    }

    public void sendOk(String text) {
        getClient().announce(ScriptMan.Packet.onScriptMessage(npc, ScriptMessageType.Say, text, 0));
    }

    public void sendDefault() {
        sendOk(getDefaultTalk(npc));
    }

    public void sendYesNo(String text) {
        getClient().announce(ScriptMan.Packet.getNPCTalk(npc, (byte) 1, text, "", (byte) 0));
    }

    public void sendAcceptDecline(String text) {
        getClient().announce(ScriptMan.Packet.getNPCTalk(npc, (byte) 0x0C, text, "", (byte) 0));
    }

    public void sendSimple(String text) {
        getClient().announce(ScriptMan.Packet.getNPCTalk(npc, (byte) 4, text, "", (byte) 0));
    }

    public void sendNext(String text, byte speaker) {
        getClient().announce(ScriptMan.Packet.getNPCTalk(npc, (byte) 0, text, "00 01", speaker));
    }

    public void sendPrev(String text, byte speaker) {
        getClient().announce(ScriptMan.Packet.getNPCTalk(npc, (byte) 0, text, "01 00", speaker));
    }

    public void sendNextPrev(String text, byte speaker) {
        getClient().announce(ScriptMan.Packet.getNPCTalk(npc, (byte) 0, text, "01 01", speaker));
    }

    public void sendOk(String text, byte speaker) {
        getClient().announce(ScriptMan.Packet.onScriptMessage(npc, ScriptMessageType.Say, text, speaker));
    }

    public void sendYesNo(String text, byte speaker) {
        getClient().announce(ScriptMan.Packet.getNPCTalk(npc, (byte) 1, text, "", speaker));
    }

    public void sendAcceptDecline(String text, byte speaker) {
        getClient().announce(ScriptMan.Packet.getNPCTalk(npc, (byte) 0x0C, text, "", speaker));
    }

    public void sendSimple(String text, byte speaker) {
        getClient().announce(ScriptMan.Packet.getNPCTalk(npc, (byte) 4, text, "", speaker));
    }

    public void sendStyle(String text, int styles[]) {
        getClient().announce(ScriptMan.Packet.getNPCTalkStyle(npc, text, styles));
    }

    public void sendGetNumber(String text, int def, int min, int max) {
        getClient().announce(ScriptMan.Packet.getNPCTalkNum(npc, text, def, min, max));
    }

    public void sendGetText(String text) {
        getClient().announce(ScriptMan.Packet.getNPCTalkText(npc, text, ""));
    }

    /*
     * 0 = ariant colliseum
     * 1 = Dojo
     * 2 = Carnival 1
     * 3 = Carnival 2
     * 4 = Ghost Ship PQ?
     * 5 = Pyramid PQ
     * 6 = Kerning Subway
     */
    public void sendDimensionalMirror(String text) {
        getClient().announce(ScriptMan.Packet.getDimensionalMirror(text));
    }

    public void setGetText(String text) {
        this.getText = text;
    }

    public String getText() {
        return this.getText;
    }

    public int getJobId() {
        return getPlayer().getJob().getId();
    }

    public MapleJob getJob() {
        return getPlayer().getJob();
    }

    @Override
    public boolean forceStartQuest(int id) {
        return forceStartQuest(id, npc);
    }

    @Override
    public boolean forceCompleteQuest(int id) {
        return forceCompleteQuest(id, npc);
    }

    @Override
    public boolean startQuest(short id) {
        return startQuest((int) id);
    }

    @Override
    public boolean completeQuest(short id) {
        return completeQuest((int) id);
    }

    @Override
    public boolean startQuest(int id) {
        return startQuest(id, npc);
    }

    @Override
    public boolean completeQuest(int id) {
        return completeQuest(id, npc);
    }

    public int getMeso() {
        return getPlayer().getMeso();
    }

    public void gainMeso(int gain) {
        getPlayer().gainMeso(gain);
    }

    public void gainExp(int gain) {
        getPlayer().gainExp(gain, true, true);
    }

    public int getLevel() {
        return getPlayer().getLevel();
    }

    @Override
    public void showEffect(String effect) {
        getPlayer().getMap().broadcastMessage(CField.Packet.onFieldEffect(FieldEffectType.Effect.getMode(), effect));
    }

    public void setHair(int hair) {
        getPlayer().setHair(hair);
        getPlayer().updateSingleStat(MapleStat.HAIR, hair);
        getPlayer().equipChanged();
    }

    public void setFace(int face) {
        getPlayer().setFace(face);
        getPlayer().updateSingleStat(MapleStat.FACE, face);
        getPlayer().equipChanged();
    }

    public void setSkin(int color) {
        getPlayer().setSkinColor(MapleSkinColor.getById(color));
        getPlayer().updateSingleStat(MapleStat.SKIN, color);
        getPlayer().equipChanged();
    }

    public int itemQuantity(int itemid) {
        return getPlayer().getInventory(ItemConstants.getInventoryType(itemid)).countById(itemid);
    }

    public void displayGuildRanks() {
        MapleGuild.displayGuildRanks(getClient(), npc);
    }

    public boolean canSpawnPlayerNpc(int mapid) {
        MapleCharacter chr = getPlayer();
        return !ServerConstants.PLAYERNPC_AUTODEPLOY && chr.getLevel() >= chr.getMaxClassLevel() && !chr.isGM() && MaplePlayerNPC.canSpawnPlayerNpc(chr.getName(), mapid);
    }

    public MaplePlayerNPC getPlayerNPCByScriptid(int scriptId) {
        for (MapleMapObject pnpcObj: getPlayer().getMap().getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER_NPC))) {
            MaplePlayerNPC pn = (MaplePlayerNPC) pnpcObj;

            if (pn.getScriptId() == scriptId) {
                return pn;
            }
        }

        return null;
    }

    @Override
    public MapleParty getParty() {
        return getPlayer().getParty();
    }

    @Override
    public void resetMap(int mapid) {
        getClient().getChannelServer().getMapFactory().getMap(mapid).resetReactors();
    }

    public void gainCloseness(int closeness) {
        for (MaplePet pet: getPlayer().getPets()) {
            if (pet != null) {
                pet.gainClosenessFullness(getPlayer(), closeness, 0, 0);
            }
        }
    }

    public String getName() {
        return getPlayer().getName();
    }

    public int getGender() {
        return getPlayer().getGender();
    }

    public int getClearance() {
        return getClient().getClearance();
    }

    public void setClearance(byte c) {
        this.getClient().setClearance(c);
    }

    public int getCharClearance() {
        return this.getPlayer().getClearance();
    }

    public void setCharClearance(int c) {
        this.getPlayer().setClearance(c);
    }

    public int getTrophy() {
        return getClient().getTrophy();
    }

    public void setTrophy(int c) {
        this.getClient().setTrophy(c);
    }

    public int getCharTrophy() {
        return this.getPlayer().getTrophy();
    }

    public void setCharTrophy(int c) {
        this.getPlayer().setTrophy(c);
    }

    public void changeJobById(int a) {
        getPlayer().changeJob(MapleJob.getById(a));
    }

    public void changeJob(MapleJob job) {
        getPlayer().changeJob(job);
    }

    public String getJobName(int id) {
        return GameConstants.getJobName(id);
    }

    public MapleStatEffect getItemEffect(int itemId) {
        return MapleItemInformationProvider.getInstance().getItemEffect(itemId);
    }

    public void resetStats() {
        getPlayer().resetStats();
    }

    public void openShopNPC(int id) {
        MapleShopFactory.getInstance().getShop(id).sendShop(c);
    }

    public void maxMastery() {
        for (MapleData skill_: MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()) {
            try {
                PlayerSkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                getPlayer().changeSkillLevel(skill, (byte) 0, skill.getMaxLevel(), -1);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
                break;
            } catch (NullPointerException npe) {
                npe.printStackTrace();
                continue;
            }
        }
    }

    public void doGachapon() {
        int[] maps = {
            100000000,
            101000000,
            102000000,
            103000000,
            105040300,
            800000000,
            809000101,
            809000201,
            600000000,
            120000000
        };

        MapleGachaponItem item = MapleGach.getInstance().process(npc);
        Item itemGained = gainItem(item.getId(), (short)(item.getId() / 10000 == 200 ? 100 : 1), true, true); // For normal potions, make it give 100.
        
        sendNext("You have obtained a #b#t" + item.getId() + "##k.");
        
        String map = c.getChannelServer().getMapFactory().getMap(maps[(getNpc() != 9100117 && getNpc() != 9100109) ? (getNpc() - 9100100) : getNpc() == 9100109 ? 8 : 9]).getMapName();
        LogHelper.logGacha(getPlayer(), item.getId(), map);

        if (ItemConstants.isGachNotificationPrize(item.getId())) { // only the good stuff
            Server.getInstance().broadcastMessage(c.getWorld(), BroadcastMsgPacket.Packet.onBroadcastGachapon(getPlayer(), itemGained, map));
        }
    }

    public void giveEventDrop() {
        MapleGachaponItem item = MapleGach.getInstance().process(npc);

        if (MapleInventoryManipulator.checkSpace(getClient(), item.getId(), (short) (item.getId() / 10000 == 200 ? 100 : 1), "")) {
            Item itemGained = gainItem(item.getId(), (short) (item.getId() / 10000 == 200 ? 100 : 1), true, true); // For normal potions, make it give 100

            sendNext("You have obtained a #b#t" + item.getId() + "##k.");
            if (isTreeContributor(getPlayer())) removeTreeContributor(getPlayer()); // for aramia event
            if (hasItem(4001168)) gainItem(4001168, (short) -1); // take away golden maple leaf
        } else {
            getPlayer().message("You do not have space for this transaction.");
            getPlayer().getClient().announce(WvsContext.Packet.enableActions());
        }
    }

    public void upgradeAlliance() {
        MapleAlliance alliance = Server.getInstance().getAlliance(c.getPlayer().getGuild().getAllianceId());
        alliance.increaseCapacity(1);

        Server.getInstance().allianceMessage(alliance.getId(),
                AlliancePacket.Packet.onAllianceResult(alliance, AllianceResultType.GuildInfo.getResult(), c.getWorld()), -1, -1);
        Server.getInstance().allianceMessage(alliance.getId(),
                AlliancePacket.Packet.onAllianceResult(alliance, AllianceResultType.Notice.getResult(), alliance.getNotice()), -1, -1);
        c.announce(AlliancePacket.Packet.onAllianceResult(alliance, AllianceResultType.UpdateInfo.getResult(), c.getWorld()));
    }

    public void disbandAlliance(MapleClient c, int allianceId) {
        MapleAlliance.disbandAlliance(allianceId);
    }

    public boolean canBeUsedAllianceName(String name) {
        return MapleAlliance.canBeUsedAllianceName(name);
    }

    public MapleAlliance createAlliance(String name) {
        return MapleAlliance.createAlliance(getParty(), name);
    }

    public int getAllianceCapacity() {
        return Server.getInstance().getAlliance(getPlayer().getGuild().getAllianceId()).getCapacity();
    }

    public boolean hasMerchant() {
        return getPlayer().hasMerchant();
    }

    public boolean hasMerchantItems() {
        try {
            if (!ItemFactory.MERCHANT.loadItems(getPlayer().getId(), false).isEmpty()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        if (getPlayer().getMerchantMeso() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void showFredrick() {
        c.announce(StoreBank.Packet.onStoreBankResult(getPlayer()));
    }

    public int partyMembersInMap() {
        int inMap = 0;
        for (MapleCharacter char2: getPlayer().getMap().getCharacters()) {
            if (char2.getParty() == getPlayer().getParty()) {
                inMap++;
            }
        }
        return inMap;
    }

    public MapleEvent getEvent() {
        return c.getChannelServer().getEvent();
    }

    public void divideTeams() {
        if (getEvent() != null) {
            getPlayer().setTeam(getEvent().getLimit() % 2); //muhaha :D
        }
    }

    public MapleCharacter getMapleCharacter(String player) {
        MapleCharacter target = Server.getInstance().getWorld(c.getWorld()).getChannel(c.getChannel()).getPlayerStorage().getCharacterByName(player);
        return target;
    }

    public void logLeaf(String prize) {
        LogHelper.logLeaf(getPlayer(), true, prize);
    }

    public boolean createPyramid(String mode, boolean party) { //lol
        PyramidMode mod = PyramidMode.valueOf(mode);

        MapleParty partyz = getPlayer().getParty();
        MapleMapFactory mf = c.getChannelServer().getMapFactory();

        MapleMap map = null;
        int mapid = 926010100;
        if (party) {
            mapid += 10000;
        }
        mapid += (mod.getMode() * 1000);

        for (byte b = 0; b < 5; b++) { //They cannot warp to the next map before the timer ends (:
            map = mf.getMap(mapid + b);
            if (map.getCharacters().size() > 0) {
                continue;
            } else {
                break;
            }
        }

        if (map == null) {
            return false;
        }

        if (!party) {
            partyz = new MapleParty(-1, new MaplePartyCharacter(getPlayer()));
        }
        Pyramid py = new Pyramid(partyz, mod, map.getId());
        getPlayer().setPartyQuest(py);
        py.warp(mapid);
        dispose();
        return true;
    }

    public boolean itemExists(int itemid) {
        return MapleItemInformationProvider.getInstance().getName(itemid) != null;
    }

    public int getCosmeticItem(int itemid) {
        if (itemExists(itemid)) {
            return itemid;
        }

        int baseid;
        if (itemid < 30000) {
            baseid = (itemid / 1000) * 1000 + (itemid % 100);
        } else {
            baseid = (itemid / 10) * 10;
        }

        return itemid != baseid && itemExists(baseid) ? baseid : -1;
    }

    private int getEquippedItemid(int itemid) {
        if (itemid < 30000) {
            return getPlayer().getFace();
        } else {
            return getPlayer().getHair();
        }
    }

    public boolean isCosmeticEquipped(int itemid) {
        return getEquippedItemid(itemid) == itemid;
    }

    public boolean isUsingOldPqNpcStyle() {
        return ServerConstants.USE_OLD_GMS_STYLED_PQ_NPCS && this.getPlayer().getParty() != null;
    }

    public Object[] getAvailableMasteryBooks() {
        return MapleItemInformationProvider.getInstance().usableMasteryBooks(this.getPlayer()).toArray();
    }

    public Object[] getAvailableSkillBooks() {
        return MapleItemInformationProvider.getInstance().usableSkillBooks(this.getPlayer()).toArray();
    }

    public Object[] getNamesWhoDropsItem(Integer itemId) {
        return MapleItemInformationProvider.getInstance().getWhoDrops(itemId).toArray();
    }

    public String getSkillBookInfo(int itemid) {
        SkillBookEntry sbe = MapleSkillbookInformationProvider.getInstance().getSkillbookAvailability(itemid);
        return sbe != SkillBookEntry.UNAVAILABLE ? "    Obtainable through #rquestline#k." : "";
    }

    // By Drago/Dragohe4rt CPQ + WED
    public int cpqCalcAvgLvl(int map) {
        int num = 0;
        int avg = 0;
        for (MapleMapObject mmo: c.getChannelServer().getMapFactory().getMap(map).getAllPlayer()) {
            avg += ((MapleCharacter) mmo).getLevel();
            num++;
        }
        avg /= num;
        return avg;
    }

    public boolean sendCPQMapLists() {
        String msg = LanguageConstants.getMessage(getPlayer(), LanguageConstants.CPQPickRoom);
        int msgLen = msg.length();
        for (int i = 0; i < 6; i++) {
            if (fieldTaken(i)) {
                if (fieldLobbied(i)) {
                    msg += "#b#L" + i + "#Carnival Field " + (i + 1) + " (Level: " // "Carnival field" GMS-like improvement thanks to Jayd
                        +
                        cpqCalcAvgLvl(980000100 + i * 100) + " / " +
                        getPlayerCount(980000100 + i * 100) + "x" +
                        getPlayerCount(980000100 + i * 100) + ")  #l\r\n";
                }
            } else {
                if (i >= 0 && i <= 3) {
                    msg += "#b#L" + i + "#Carnival Field " + (i + 1) + " (2x2) #l\r\n";
                } else {
                    msg += "#b#L" + i + "#Carnival Field " + (i + 1) + " (3x3) #l\r\n";
                }
            }
        }

        if (msg.length() > msgLen) {
            sendSimple(msg);
            return true;
        } else {
            return false;
        }
    }

    public boolean fieldTaken(int field) {
        if (!c.getChannelServer().canInitMonsterCarnival(true, field)) {
            return true;
        }
        if (!c.getChannelServer().getMapFactory().getMap(980000100 + field * 100).getAllPlayer().isEmpty()) {
            return true;
        }
        if (!c.getChannelServer().getMapFactory().getMap(980000101 + field * 100).getAllPlayer().isEmpty()) {
            return true;
        }
        if (!c.getChannelServer().getMapFactory().getMap(980000102 + field * 100).getAllPlayer().isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean fieldLobbied(int field) {
        if (!c.getChannelServer().getMapFactory().getMap(980000100 + field * 100).getAllPlayer().isEmpty()) {
            return true;
        }
        return false;
    }

    public void cpqLobby(int field) {
        try {
            final MapleMap map, mapExit;
            Channel cs = c.getChannelServer();
            PlayerStorage ps = cs.getPlayerStorage();

            map = cs.getMapFactory().getMap(980000100 + 100 * field);
            mapExit = cs.getMapFactory().getMap(980000000);
            for (MaplePartyCharacter mpc: c.getPlayer().getParty().getMembers()) {
                final MapleCharacter mc;
                mc = ps.getCharacterById(mpc.getId());
                if (mc != null) {
                    mc.changeMap(map, map.getPortal(0));
                    mc.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.BlueText.getType(),
                            LanguageConstants.getMessage(mc, LanguageConstants.CPQEntryLobby)));
                    TimerManager tMan = TimerManager.getInstance();
                    tMan.schedule(() -> mapClock(3 * 60), 1500);

                    mc.setCpqTimer(TimerManager.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            mc.changeMap(mapExit, mapExit.getPortal(0));
                        }
                    }, 3 * 60 * 1000));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public MapleCharacter getChrById(int id) {
        return c.getChannelServer().getPlayerStorage().getCharacterById(id);
    }

    public void cancelCPQLobby() {
        PlayerStorage ps = c.getChannelServer().getPlayerStorage();
        for (MaplePartyCharacter mpc: c.getPlayer().getParty().getMembers()) {
            MapleCharacter mc = ps.getCharacterById(mpc.getId());
            if (mc != null) {
                mc.clearCpqTimer();
            }
        }
    }

    private void warpoutCPQLobby() {
        MapleMap lobbyMap = c.getPlayer().getMap();
        MapleMap out = this.getWarpMap((lobbyMap.getId() > 980030000) ? 980000000 : 980030000);
        for (MapleCharacter mc: lobbyMap.getAllPlayers()) {
            mc.resetCP();
            mc.setTeam(-1);
            mc.setMonsterCarnival(null);
            mc.changeMap(out, out.getPortal(0));
        }
    }

    public void startCPQ(final MapleCharacter challenger, final int field) {
        try {
            cancelCPQLobby();
            if (challenger != null) {
                if (challenger.getParty() == null) {
                    throw new RuntimeException("Nao existe oponente!");
                }
                PlayerStorage ps = c.getChannelServer().getPlayerStorage();
                for (MaplePartyCharacter mpc: challenger.getParty().getMembers()) {
                    MapleCharacter mc = ps.getCharacterById(mpc.getId());
                    if (mc != null) {
                        mc.changeMap(getPlayer().getMap(), getPlayer().getMap().getPortal(0));
                        TimerManager tMan = TimerManager.getInstance();
                        tMan.schedule(new Runnable() {
                            @Override
                            public void run() {
                                mapClock(10);
                            }
                        }, 1500);
                    }
                }
                for (MaplePartyCharacter mpc: getPlayer().getParty().getMembers()) {
                    MapleCharacter mc = ps.getCharacterById(mpc.getId());
                    if (mc != null) {
                        TimerManager tMan = TimerManager.getInstance();
                        tMan.schedule(new Runnable() {
                            @Override
                            public void run() {
                                mapClock(10);
                            }
                        }, 1500);
                    }
                }
            }
            final int mapid = c.getPlayer().getMapId() + 1;
            TimerManager tMan = TimerManager.getInstance();
            tMan.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        PlayerStorage ps = c.getChannelServer().getPlayerStorage();
                        for (MaplePartyCharacter mpc: getPlayer().getParty().getMembers()) {
                            MapleCharacter mc = ps.getCharacterById(mpc.getId());
                            mc.setMonsterCarnival(null);
                        }
                        for (MaplePartyCharacter mpc: challenger.getParty().getMembers()) {
                            MapleCharacter mc = ps.getCharacterById(mpc.getId());
                            mc.setMonsterCarnival(null);
                        }
                    } catch (NullPointerException npe) {
                        warpoutCPQLobby();
                        return;
                    }

                    new MonsterCarnival(getPlayer().getParty(), challenger.getParty(), mapid, true, (field / 100) % 10);
                }
            }, 11000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCPQ2(final MapleCharacter challenger, final int field) {
        try {
            cancelCPQLobby();
            if (challenger != null) {
                if (challenger.getParty() == null) {
                    throw new RuntimeException("Não existe oponente!");
                }
                PlayerStorage ps = c.getChannelServer().getPlayerStorage();
                for (MaplePartyCharacter mpc: challenger.getParty().getMembers()) {
                    MapleCharacter mc = ps.getCharacterById(mpc.getId());
                    if (mc != null) {
                        mc.changeMap(getPlayer().getMap(), getPlayer().getMap().getPortal(0));
                        mapClock(10);
                    }
                }
            }
            final int mapid = c.getPlayer().getMapId() + 100;
            TimerManager tMan = TimerManager.getInstance();
            tMan.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        PlayerStorage ps = c.getChannelServer().getPlayerStorage();
                        for (MaplePartyCharacter mpc: getPlayer().getParty().getMembers()) {
                            MapleCharacter mc = ps.getCharacterById(mpc.getId());
                            mc.setMonsterCarnival(null);
                        }
                        for (MaplePartyCharacter mpc: challenger.getParty().getMembers()) {
                            MapleCharacter mc = ps.getCharacterById(mpc.getId());
                            mc.setMonsterCarnival(null);
                        }
                    } catch (NullPointerException npe) {
                        warpoutCPQLobby();
                        return;
                    }

                    new MonsterCarnival(getPlayer().getParty(), challenger.getParty(), mapid, false, (field / 1000) % 10);
                }
            }, 10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean sendCPQMapLists2() {
        String msg = LanguageConstants.getMessage(getPlayer(), LanguageConstants.CPQPickRoom);
        int msgLen = msg.length();
        for (int i = 0; i < 3; i++) {
            if (fieldTaken2(i)) {
                if (fieldLobbied2(i)) {
                    msg += "#b#L" + i + "#Carnival Field " + (i + 1) + " (Level: " // "Carnival field" GMS-like improvement thanks to Jayd
                        +
                        cpqCalcAvgLvl(980031000 + i * 1000) + " / " +
                        getPlayerCount(980031000 + i * 1000) + "x" +
                        getPlayerCount(980031000 + i * 1000) + ")  #l\r\n";
                }
            } else {
                if (i == 0 || i == 1) {
                    msg += "#b#L" + i + "#Carnival Field " + (i + 1) + " (2x2) #l\r\n";
                } else {
                    msg += "#b#L" + i + "#Carnival Field " + (i + 1) + " (3x3) #l\r\n";
                }
            }
        }

        if (msg.length() > msgLen) {
            sendSimple(msg);
            return true;
        } else {
            return false;
        }
    }

    public boolean fieldTaken2(int field) {
        if (!c.getChannelServer().canInitMonsterCarnival(false, field)) {
            return true;
        }
        if (!c.getChannelServer().getMapFactory().getMap(980031000 + field * 1000).getAllPlayer().isEmpty()) {
            return true;
        }
        if (!c.getChannelServer().getMapFactory().getMap(980031100 + field * 1000).getAllPlayer().isEmpty()) {
            return true;
        }
        if (!c.getChannelServer().getMapFactory().getMap(980031200 + field * 1000).getAllPlayer().isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean fieldLobbied2(int field) {
        if (!c.getChannelServer().getMapFactory().getMap(980031000 + field * 1000).getAllPlayer().isEmpty()) {
            return true;
        }
        return false;
    }

    public void cpqLobby2(int field) {
        try {
            final MapleMap map, mapExit;
            Channel cs = c.getChannelServer();
            PlayerStorage ps = c.getChannelServer().getPlayerStorage();

            mapExit = cs.getMapFactory().getMap(980030000);
            map = cs.getMapFactory().getMap(980031000 + 1000 * field);
            for (MaplePartyCharacter mpc: c.getPlayer().getParty().getMembers()) {
                final MapleCharacter mc;
                mc = ps.getCharacterById(mpc.getId());
                if (mc != null) {
                    mc.changeMap(map, map.getPortal(0));
                    mc.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.BlueText.getType(),
                            LanguageConstants.getMessage(mc, LanguageConstants.CPQEntryLobby)));
                    TimerManager tMan = TimerManager.getInstance();
                    tMan.schedule(new Runnable() {
                        @Override
                        public void run() {
                            mapClock(3 * 60);
                        }
                    }, 1500);

                    mc.setCpqTimer(TimerManager.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            mc.changeMap(mapExit, mapExit.getPortal(0));
                        }
                    }, 3 * 60 * 1000));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void mapClock(int time) {
        getPlayer().getMap().broadcastMessage(CField.Packet.onClock(true, time));
    }

    private boolean sendCPQChallenge(String cpqType, int leaderid) {
        Set < Integer > cpqLeaders = new HashSet < > ();
        cpqLeaders.add(leaderid);
        cpqLeaders.add(getPlayer().getId());

        return c.getWorldServer().getMatchCheckerCoordinator().createMatchConfirmation(MatchCheckerType.CPQ_CHALLENGE, c.getWorld(), getPlayer().getId(), cpqLeaders, cpqType);
    }

    public void answerCPQChallenge(boolean accept) {
        c.getWorldServer().getMatchCheckerCoordinator().answerMatchConfirmation(getPlayer().getId(), accept);
    }

    public void challengeParty2(int field) {
        MapleCharacter leader = null;
        MapleMap map = c.getChannelServer().getMapFactory().getMap(980031000 + 1000 * field);
        for (MapleMapObject mmo: map.getAllPlayer()) {
            MapleCharacter mc = (MapleCharacter) mmo;
            if (mc.getParty() == null) {
                sendOk(LanguageConstants.getMessage(mc, LanguageConstants.CPQFindError));
                return;
            }
            if (mc.getParty().getLeader().getId() == mc.getId()) {
                leader = mc;
                break;
            }
        }
        if (leader != null) {
            if (!leader.isChallenged()) {
                if (!sendCPQChallenge("cpq2", leader.getId())) {
                    sendOk(LanguageConstants.getMessage(leader, LanguageConstants.CPQChallengeRoomAnswer));
                }
            } else {
                sendOk(LanguageConstants.getMessage(leader, LanguageConstants.CPQChallengeRoomAnswer));
            }
        } else {
            sendOk(LanguageConstants.getMessage(leader, LanguageConstants.CPQLeaderNotFound));
        }
    }

    public void challengeParty(int field) {
        MapleCharacter leader = null;
        MapleMap map = c.getChannelServer().getMapFactory().getMap(980000100 + 100 * field);
        if (map.getAllPlayer().size() != getPlayer().getParty().getMembers().size()) {
            sendOk("An unexpected error regarding the other party has occurred.");
            return;
        }
        for (MapleMapObject mmo: map.getAllPlayer()) {
            MapleCharacter mc = (MapleCharacter) mmo;
            if (mc.getParty() == null) {
                sendOk(LanguageConstants.getMessage(mc, LanguageConstants.CPQFindError));
                return;
            }
            if (mc.getParty().getLeader().getId() == mc.getId()) {
                leader = mc;
                break;
            }
        }
        if (leader != null) {
            if (!leader.isChallenged()) {
                if (!sendCPQChallenge("cpq1", leader.getId())) {
                    sendOk(LanguageConstants.getMessage(leader, LanguageConstants.CPQChallengeRoomAnswer));
                }
            } else {
                sendOk(LanguageConstants.getMessage(leader, LanguageConstants.CPQChallengeRoomAnswer));
            }
        } else {
            sendOk(LanguageConstants.getMessage(leader, LanguageConstants.CPQLeaderNotFound));
        }
    }

    private synchronized boolean setupAriantBattle(MapleExpedition exped, int mapid) {
        MapleMap arenaMap = this.getMap().getChannelServer().getMapFactory().getMap(mapid + 1);
        if (!arenaMap.getAllPlayers().isEmpty()) {
            return false;
        }

        new AriantColiseum(arenaMap, exped);
        return true;
    }

    public String startAriantBattle(MapleExpeditionType expedType, int mapid) {
        if (!GameConstants.isAriantColiseumLobby(mapid)) {
            return "You cannot start an Ariant tournament from outside the Battle Arena Entrance.";
        }

        MapleExpedition exped = this.getMap().getChannelServer().getExpedition(expedType);
        if (exped == null) {
            return "Please register on an expedition before attempting to start an Ariant tournament.";
        }

        List < MapleCharacter > players = exped.getActiveMembers();

        int playersSize = players.size();
        if (!(playersSize >= exped.getMinSize() && playersSize <= exped.getMaxSize())) {
            return "Make sure there are between #r" + exped.getMinSize() + " ~ " + exped.getMaxSize() + " players#k in this room to start the battle.";
        }

        MapleMap leaderMap = this.getMap();
        for (MapleCharacter mc: players) {
            if (mc.getMap() != leaderMap) {
                return "All competing players should be on this area to start the battle.";
            }

            if (mc.getParty() != null) {
                return "All competing players must not be on a party to start the battle.";
            }

            int level = mc.getLevel();
            if (!(level >= expedType.getMinLevel() && level <= expedType.getMaxLevel())) {
                return "There are competing players outside of the acceptable level range in this room. All players must be on #blevel between 20~30#k to start the battle.";
            }
        }

        if (setupAriantBattle(exped, mapid)) {
            return "";
        } else {
            return "Other players are already competing on the Ariant tournament in this room. Please wait a while until the arena becomes available again.";
        }
    }

    public void sendMarriageWishlist(boolean groom) {
        MapleCharacter player = this.getPlayer();
        MapleMarriage marriage = player.getMarriageInstance();
        if (marriage != null) {
            int cid = marriage.getIntProperty(groom ? "groomId" : "brideId");
            MapleCharacter chr = marriage.getPlayerById(cid);
            if (chr != null) {
                if (chr.getId() == player.getId()) {
                    player.announce(Wedding.OnWeddingGiftResult((byte) 0xA, marriage.getWishlistItems(groom), marriage.getGiftItems(player.getClient(), groom)));
                } else {
                    marriage.setIntProperty("wishlistSelection", groom ? 0 : 1);
                    player.announce(Wedding.OnWeddingGiftResult((byte) 0x09, marriage.getWishlistItems(groom), marriage.getGiftItems(player.getClient(), groom)));
                }
            }
        }
    }

    public void sendMarriageGifts(List < Item > gifts) {
        this.getPlayer().announce(Wedding.OnWeddingGiftResult((byte) 0xA, Collections.singletonList(""), gifts));
    }

    public boolean createMarriageWishlist() {
        MapleMarriage marriage = this.getPlayer().getMarriageInstance();
        if (marriage != null) {
            Boolean groom = marriage.isMarriageGroom(this.getPlayer());
            if (groom != null) {
                String wlKey;
                if (groom) {
                    wlKey = "groomWishlist";
                } else {
                    wlKey = "brideWishlist";
                }

                if (marriage.getProperty(wlKey).contentEquals("")) {
                    getClient().announce(Wedding.sendWishList());
                    return true;
                }
            }
        }

        return false;
    }

    public void addSunshines(int sun) {
        AramiaTree.getInstance().addSunshines(getPlayer().getClient(), sun);
    }

    public int getTreeSize() {
        return AramiaTree.getInstance().getTreeSize();
    }

    public boolean isTreeContributor(MapleCharacter chr) {
        if (AramiaTree.isContributor(chr))
            return true;
        else
            return false;
    }

    public void removeTreeContributor(MapleCharacter chr) {
        AramiaTree.removeContributor(chr);
    }

    public String getTourPQ() {
        return PQTour.getTourPQ();
    }
}
