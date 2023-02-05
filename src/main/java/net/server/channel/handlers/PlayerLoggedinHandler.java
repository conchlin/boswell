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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.JsonObject;
import enums.AllianceResultType;
import enums.FriendResultType;
import enums.LoginResultType;
import enums.PartyResultType;
import net.AbstractMaplePacketHandler;
import net.server.PlayerBuffValueHolder;
import net.server.Server;
import net.server.channel.Channel;
import net.server.channel.CharacterIdChannelPair;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.world.MaplePartyCharacter;
import net.server.world.World;
import network.packet.CLogin;
import network.packet.CStage;
import network.packet.FuncKeyMappedMan;
import network.packet.NpcPool;
import network.packet.context.*;
import server.skills.SkillFactory;
import net.database.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.BuddyList;
import client.BuddylistEntry;
import client.CharacterNameAndId;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleFamily;
import client.MapleKeyBinding;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.GameConstants;
import constants.ScriptableNPCConstants;
import constants.ServerConstants;

import java.util.HashSet;
import java.util.Set;

import net.server.coordinator.MapleSessionCoordinator;
import org.apache.mina.core.session.IoSession;
import tools.packets.Wedding;

public final class PlayerLoggedinHandler extends AbstractMaplePacketHandler {

    private static Set<Integer> attemptingLoginAccounts = new HashSet<>();
    
    private boolean tryAcquireAccount(int accId) {
        synchronized (attemptingLoginAccounts) {
            if (attemptingLoginAccounts.contains(accId)) {
                return false;
            }
            
            attemptingLoginAccounts.add(accId);
            return true;
        }
    }
    
    private void releaseAccount(int accId) {
        synchronized (attemptingLoginAccounts) {
            attemptingLoginAccounts.remove(accId);
        }
    }
    
    @Override
    public boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final int cid = slea.readInt();
        final Server server = Server.getInstance();
        
        if (c.tryacquireClient()) { // thanks MedicOP for assisting on concurrency protection here
            try {
                World wserv = server.getWorld(c.getWorld());
                if(wserv == null) {
                    c.disconnect(true, false);
                    return;
                }

                Channel cserv = wserv.getChannel(c.getChannel());
                if(cserv == null) {
                    c.setChannel(1);
                    cserv = wserv.getChannel(c.getChannel());

                    if(cserv == null) {
                        c.disconnect(true, false);
                        return;
                    }
                }

                MapleCharacter player = wserv.getPlayerStorage().getCharacterById(cid);
                boolean newcomer = false;

                IoSession session = c.getSession();
                String remoteHwid;
                if (player == null) {
                    if (!server.validateCharacteridInTransition(session, cid)) {
                        c.disconnect(true, false);
                        return;
                    }

                    remoteHwid = MapleSessionCoordinator.getInstance().getGameSessionHwid(session);
                    if (remoteHwid == null) {
                        c.disconnect(true, false);
                        return;
                    }

                    try {
                        player = MapleCharacter.loadCharFromDB(cid, c, true);
                        newcomer = true;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    remoteHwid = player.getClient().getHWID();
                }

                if (player == null) { //If you are still getting null here then please just uninstall the game >.>, we dont need you fucking with the logs
                    c.disconnect(true, false);
                    return;
                }

                c.setPlayer(player);
                c.setAccID(player.getAccountID());

                boolean allowLogin = true;

                /*  is this check really necessary?
                if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.LOGIN_NOTLOGGEDIN) {
                    List<String> charNames = c.loadCharacterNames(c.getWorld());
                    if(!newcomer) {
                        charNames.remove(player.getName());
                    }

                    for (String charName : charNames) {
                        if(wserv.getPlayerStorage().getCharacterByName(charName) != null) {
                            allowLogin = false;
                            break;
                        }
                    }
                }
                */
                
                int accId = c.getAccID();
                if (tryAcquireAccount(accId)) { // Sync this to prevent wrong login state for double loggedin handling
                    try {
                        int state = c.getLoginState();
                        if (state != MapleClient.LOGIN_SERVER_TRANSITION || !allowLogin) {
                            c.setPlayer(null);
                            c.setAccID(0);

                            if (state == MapleClient.LOGIN_LOGGEDIN) {
                                c.disconnect(true, false);
                            } else {
                                c.announce(CLogin.Packet.onSelectCharacterByVACResult(LoginResultType.AlreadyLoggedIn.getReason()));
                            }

                            return;
                        }
                        c.updateLoginState(MapleClient.LOGIN_LOGGEDIN);
                    } finally {
                        releaseAccount(accId);
                    }
                } else {
                    c.setPlayer(null);
                    c.setAccID(0);
                    c.announce(CLogin.Packet.onSelectCharacterByVACResult(LoginResultType.TooManyConnections.getReason()));
                    return;
                }
                
                if (!newcomer) {
                    c.setCharacterSlots((byte) player.getClient().getCharacterSlots());
                    player.newClient(c);
                }

                int hwidLen = remoteHwid.length();
                session.setAttribute(MapleClient.CLIENT_HWID, remoteHwid);
                session.setAttribute(MapleClient.CLIENT_NIBBLEHWID, remoteHwid.substring(hwidLen - 8, hwidLen));
                c.setHWID(remoteHwid);

                cserv.addPlayer(player);
                wserv.addPlayer(player);
                player.setEnteredChannelWorld();

                if (ServerConstants.HTTP_SERVER) {
                    JsonObject json = new JsonObject();
                    json.addProperty("up", "" + player.getAccountID());
                    JsonObject players = new JsonObject();
                    for (MapleCharacter _c : wserv.getPlayerStorage().getAllCharacters()) {
                        players.addProperty("" + _c.getAccountID(), _c.getName());
                    }
                    json.add("players", players);

                    Server.httpWorker.add("http://localhost:17003/api/character_connection", json);
                }

                List<PlayerBuffValueHolder> buffs = server.getPlayerBuffStorage().getBuffsFromStorage(cid);
                if (buffs != null) {
                    player.silentGiveBuffs(buffs);
                }

/*                Map<MapleDisease, Pair<Long, MobSkill>> diseases = server.getPlayerBuffStorage().getDiseasesFromStorage(cid);
                if (diseases != null) {
                    player.silentApplyDiseases(diseases);
                }*/

                c.announce(CStage.Packet.onSetField(player)); // character info packet
                if (!player.isHidden()) {
                    player.toggleHide(true); // auto-hide for GMs
                }
                player.sendKeymap();
                player.sendMacros();

                // pot bindings being passed through other characters on the account detected thanks to Croosade dev team
                MapleKeyBinding autohpPot = player.getKeymap().get(91);
                player.announce(FuncKeyMappedMan.Packet.onPetConsumeItemInit(autohpPot != null ? autohpPot.getAction() : 0));

                MapleKeyBinding autompPot = player.getKeymap().get(92);
                player.announce(FuncKeyMappedMan.Packet.onPetConsumeMPItemInit(autompPot != null ? autompPot.getAction() : 0));

                player.getMap().addPlayer(player);
                player.visitMap(player.getMap());

                BuddyList bl = player.getBuddylist();
                int buddyIds[] = bl.getBuddyIds();
                wserv.loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
                for (CharacterIdChannelPair onlineBuddy : wserv.multiBuddyFind(player.getId(), buddyIds)) {
                    BuddylistEntry ble = bl.get(onlineBuddy.getCharacterId());
                    ble.setChannel(onlineBuddy.getChannel());
                    bl.put(ble);
                }
                c.announce(FriendPacket.Packet.onFriendResult(FriendResultType.UpdateList.getType(), bl.getBuddies()));

                c.announce(FamilyPacket.Packet.onPrivilegeList());
                if (player.getFamilyId() > 0) {
                    MapleFamily f = wserv.getFamily(player.getFamilyId());
                    if (f == null) {
                        f = new MapleFamily(player.getId());
                        wserv.addFamily(player.getFamilyId(), f);
                    }
                    player.setFamily(f);
                    c.announce(FamilyPacket.Packet.onInfoResult(f.getMember(player.getId())));
                }
                if (player.getGuildId() > 0) {
                    MapleGuild playerGuild = server.getGuild(player.getGuildId(), player.getWorld(), player);
                    if (playerGuild == null) {
                        player.deleteGuild(player.getGuildId());
                        player.getMGC().setGuildId(0);
                        player.getMGC().setGuildRank(5);
                    } else {
                        playerGuild.getMGC(player.getId()).setCharacter(player);
                        player.setMGC(playerGuild.getMGC(player.getId()));
                        server.setGuildMemberOnline(player, true, c.getChannel());
                        c.announce(GuildPacket.Packet.showGuildInfo(player));
                        int allianceId = player.getGuild().getAllianceId();
                        if (allianceId > 0) {
                            MapleAlliance newAlliance = server.getAlliance(allianceId);
                            if (newAlliance == null) {
                                newAlliance = MapleAlliance.loadAlliance(allianceId);
                                if (newAlliance != null) {
                                    server.addAlliance(allianceId, newAlliance);
                                } else {
                                    player.getGuild().setAllianceId(0);
                                }
                            }
                            if (newAlliance != null) {
                                c.announce(AlliancePacket.Packet.onAllianceResult(newAlliance, AllianceResultType.UpdateInfo.getResult(), c.getWorld()));
                                c.announce(AlliancePacket.Packet.onAllianceResult(newAlliance, AllianceResultType.Notice.getResult(), newAlliance.getNotice()));

                                if (newcomer) {
                                    server.allianceMessage(allianceId,
                                            AlliancePacket.Packet.onAllianceResult(player, AllianceResultType.LogInOut.getResult(), 1), player.getId(), -1);
                                }
                            }
                        }
                    }
                }

                player.showNote();
                if (player.getParty() != null) {
                    MaplePartyCharacter pchar = player.getMPC();

                    //Use this in case of enabling party HPbar HUD when logging in, however "you created a party" will appear on chat.
                    //c.announce(MaplePacketCreator.partyCreated(pchar));

                    pchar.setChannel(c.getChannel());
                    pchar.setMapId(player.getMapId());
                    pchar.setOnline(true);
                    wserv.updateParty(player.getParty().getId(), PartyResultType.ChangeLeader.getResult(), pchar);
                    player.updatePartyMemberHP();
                }

                MapleInventory eqpInv = player.getInventory(MapleInventoryType.EQUIPPED);
                eqpInv.lockInventory();
                try {
                    for(Item it : eqpInv.list()) {
                        player.equippedItem((Equip) it);
                    }
                } finally {
                    eqpInv.unlockInventory();
                }

                c.announce(FriendPacket.Packet.onFriendResult(FriendResultType.UpdateList.getType(), player.getBuddylist().getBuddies()));

                CharacterNameAndId pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
                if (pendingBuddyRequest != null) {
                    c.announce(FriendPacket.Packet.onFriendResult(FriendResultType.AddFriend.getType(), pendingBuddyRequest.getId(), c.getPlayer().getId(), pendingBuddyRequest.getName()));
                }

                c.announce(WvsContext.Packet.onSetGender(player));
                player.checkMessenger();
                c.announce(WvsContext.Packet.enableReport());
                player.changeSkillLevel(SkillFactory.getSkill(10000000 * player.getJobType() + 12), (byte) (player.getLinkedLevel() / 10), 20, -1);
                player.checkBerserk(player.isHidden());

                if (newcomer) {
                    for(MaplePet pet : player.getPets()) {
                        if(pet != null)
                            wserv.registerPetHunger(player, player.getPetIndex(pet));
                    }

                    player.reloadQuestExpirations();

                    if (player.isGM()){
                        Server.getInstance().broadcastGMMessage(c.getWorld(), WvsContext.Packet.onScriptProgressMessage((player.gmLevel() < 2 ? "GM " : "Admin ") + player.getName() + " has logged in"));
                    }

                    // instant warp to cheater channel if a cheater tries logging into another channel
                    if (player.isCheater() && player.getClient().getChannel() != GameConstants.CHEATER_CHANNEL) {
                        c.changeChannel(GameConstants.CHEATER_CHANNEL);
                    }
                    // instant warp for non-cheaters if they log into the cheater channel
                    if (!player.isCheater() && player.getClient().getChannel() == GameConstants.CHEATER_CHANNEL) {
                        c.changeChannel(GameConstants.CHEATER_CHANNEL - 1);
                    }
                    
/*                    if(diseases != null) {
                        for(Entry<MapleDisease, Pair<Long, MobSkill>> e : diseases.entrySet()) {
                            final List<Pair<MapleDisease, Integer>> debuff = Collections.singletonList(new Pair<>(e.getKey(), Integer.valueOf(e.getValue().getRight().getX())));
                            //c.announce(MaplePacketCreator.giveDebuff(debuff, e.getValue().getRight()));
                        }

                        //player.announceDiseases();
                    }*/
                } else {
                    if(player.isRidingBattleship()) {
                        player.announceBattleshipHp();
                    }
                }

                player.buffExpireTask();
                player.diseaseExpireTask();
                player.skillCooldownTask();
                player.expirationTask();
                player.questExpirationTask();
                if (GameConstants.hasSPTable(player.getJob()) && player.getJob().getId() != 2001) {
                    player.createDragon();
                }

                player.commitExcludedItems();
                showDueyNotification(c, player);

                if (player.getMap().getHPDec() > 0) player.resetHpDecreaseTask();

                player.resetPlayerRates();
                player.setWorldRates();
                player.updateCouponRates();
                player.setLoginTime();

                player.receivePartyMemberHP();

                if(player.getPartnerId() > 0) {
                    int partnerId = player.getPartnerId();
                    final MapleCharacter partner = wserv.getPlayerStorage().getCharacterById(partnerId);

                    if(partner != null && !partner.isAwayFromWorld()) {
                        player.announce(Wedding.OnNotifyWeddingPartnerTransfer(partnerId, partner.getMapId()));
                        partner.announce(Wedding.OnNotifyWeddingPartnerTransfer(player.getId(), player.getMapId()));
                    }
                }
                
                if (ServerConstants.USE_NPCS_SCRIPTABLE) {
                    c.announce(NpcPool.Packet.setNPCScriptable(ScriptableNPCConstants.SCRIPTABLE_NPCS));
                }
            } finally {
                c.releaseClient();
            }
        } else {
            c.announce(CLogin.Packet.onSelectCharacterByVACResult(LoginResultType.TooManyConnections.getReason()));
        }
    }

    private static void showDueyNotification(MapleClient c, MapleCharacter player) {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT Type FROM duey_packages WHERE ReceiverId = ? AND Checked = true ORDER BY Type DESC")) {
                ps.setInt(1, player.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        try (Connection con2 = DatabaseConnection.getConnection()) {
                            try (PreparedStatement pss = con2.prepareStatement("UPDATE duey_packages SET Checked = false WHERE ReceiverId = ?")) {
                                pss.setInt(1, player.getId());
                                pss.executeUpdate();
                            }

                            c.announce(MaplePacketCreator.sendDueyParcelNotification(rs.getInt("Type") == 1));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
