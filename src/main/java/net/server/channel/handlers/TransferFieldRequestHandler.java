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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

import enums.UserEffectType;
import net.AbstractMaplePacketHandler;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.inventory.manipulator.MapleInventoryManipulator;
import network.packet.field.CField;
import network.packet.CLogin;
import network.packet.UserLocal;
import network.packet.context.WvsContext;
import server.MaplePortal;
import server.MapleTrade;
import server.maps.MapleMap;
import tools.FilePrinter;
import tools.data.input.SeekableLittleEndianAccessor;

public final class TransferFieldRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();

        if (chr.isChangingMaps() || chr.isBanned()) {
            if (chr.isChangingMaps()) {
                FilePrinter.printError(FilePrinter.PORTAL_STUCK + chr.getName() + ".txt", "Player " + chr.getName() + " got stuck when changing maps. Timestamp: " + Calendar.getInstance().getTime().toString() + " Last visited mapids: " + chr.getLastVisitedMapids());
            }

            c.announce(WvsContext.Packet.enableActions());
            return;
        }

        if (chr.isCheater() && chr.getClient().getChannel() != 4) {
            chr.message("You cannot access this map on this channel.");
            return;
        }

        if (chr.getTrade() != null) {
            MapleTrade.cancelTrade(chr, MapleTrade.TradeResult.UNSUCCESSFUL_ANOTHER_MAP);
        }
        if (slea.available() == 0) { //Cash Shop :)
            if (!chr.getCashShop().isOpened()) {
                c.disconnect(false, false);
                return;
            }
            String[] socket = c.getChannelServer().getIP().split(":");
            chr.getCashShop().open(false);

            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
            chr.setSessionTransitionState();
            try {
                c.announce(CLogin.Packet.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            }
        } else {
            if (chr.getCashShop().isOpened()) {
                c.disconnect(false, false);
                return;
            }
            try {
                slea.readByte(); // 1 = from dying 0 = regular portals
                int targetid = slea.readInt();
                String startwp = slea.readMapleAsciiString();
                MaplePortal portal = chr.getMap().getPortal(startwp);
                slea.readByte();
                boolean wheel = slea.readShort() > 0;

                if (targetid != -1) {
                    if (!chr.isAlive()) {
                        MapleMap map = chr.getMap();
                        if (wheel && chr.haveItemWithId(5510000, false)) {
                            // thanks lucasziron for showing revivePlayer() also being triggered by Wheel

                            MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, 5510000, 1, true, false);
                            chr.announce(UserLocal.Packet.onEffect(UserEffectType.WHEEL_DESTINY.getEffect(), "", chr.getItemQuantity(5510000, false)));

                            chr.updateHp(50);
                            chr.changeMap(map, map.findClosestPlayerSpawnpoint(chr.getPosition()));
                        } else {
                            boolean executeStandardPath = true;
                            /*if (chr.getEventInstance() != null) {
                                executeStandardPath = chr.getEventInstance().revivePlayer(chr);
                            }*/
                            if (executeStandardPath) {
                                chr.respawn(map.getReturnMapId());
                            }
                        }
                    } else {
                        if (chr.isGM()) {
                            MapleMap to = chr.getWarpMap(targetid);
                            chr.changeMap(to, to.getPortal(0));
                        } else {
                            final int divi = chr.getMapId() / 100;
                            boolean warp = false;
                            if (divi == 0) {
                                if (targetid == 10000) {
                                    warp = true;
                                }
                            } else if (divi == 20100) {
                                if (targetid == 104000000) {
                                    c.announce(UserLocal.Packet.setDirectionMode(false));
                                    c.announce(UserLocal.Packet.onDisableUI(false));
                                    warp = true;
                                }
                            } else if (divi == 9130401) { // Only allow warp if player is already in Intro map, or else = hack
                                if (targetid == 130000000 || targetid / 100 == 9130401) { // Cygnus introduction
                                    warp = true;
                                }
                            } else if (divi == 9140900) { // Aran Introduction
                                if (targetid == 914090011 || targetid == 914090012 || targetid == 914090013 || targetid == 140090000) {
                                    warp = true;
                                }
                            } else if (divi / 10 == 1020) { // Adventurer movie clip Intro
                                if (targetid == 1020000) {
                                    warp = true;
                                }
                            } else if (divi / 10 >= 980040 && divi / 10 <= 980045) {
                                if (targetid == 980040000) {
                                    warp = true;
                                }
                            }
                            if (warp) {
                                final MapleMap to = chr.getWarpMap(targetid);
                                chr.changeMap(to, to.getPortal(0));
                            }
                        }
                    }
                }

                if (portal != null && !portal.getPortalStatus()) {
                    c.announce(CField.Packet.onTransferFieldRequestIgnored(1));
                    c.announce(WvsContext.Packet.enableActions());
                    return;
                }

                if (chr.getMapId() == 109040004) {
                    chr.getFitness().resetTimes();
                } else if (chr.getMapId() == 109030003 || chr.getMapId() == 109030103) {
                    chr.getOla().resetTimes();
                }

                if (portal != null) {
                    if (portal.getPosition().distanceSq(chr.getPosition()) > 400000) {
                        c.announce(WvsContext.Packet.enableActions());
                        return;
                    }

                    portal.enterPortal(c);
                } else {
                    c.announce(WvsContext.Packet.enableActions());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}