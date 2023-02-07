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
package net.server.handlers.login;

import client.MapleClient;
import java.net.InetAddress;
import java.net.UnknownHostException;

import enums.LoginResultType;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import net.server.coordinator.MapleSessionCoordinator;
import net.server.coordinator.MapleSessionCoordinator.AntiMulticlientResult;
import net.server.world.World;
import network.packet.CLogin;
import org.apache.mina.core.session.IoSession;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SelectCharacterByVACHandler extends AbstractMaplePacketHandler {

    private static int parseAntiMulticlientError(AntiMulticlientResult res) {
        return switch (res) {
            case REMOTE_PROCESSING -> 10;
            case REMOTE_LOGGEDIN -> 7;
            case REMOTE_NO_MATCH -> 17;
            case COORDINATOR_ERROR -> 8;
            default -> 9;
        };
    }
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int charId = slea.readInt();
        slea.readInt(); // please don't let the client choose which world they should login
        
        String macs = slea.readMapleAsciiString();
        String hwid = slea.readMapleAsciiString();
        
        if (!hwid.matches("[0-9A-F]{12}_[0-9A-F]{8}")) {
            c.announce(CLogin.Packet.onSelectCharacterByVACResult(LoginResultType.WrongGateway.getReason()));
            return;
        }
        
        c.updateMacs(macs);
        c.updateHWID(hwid);
        
        if (c.hasBannedMac() || c.hasBannedHWID()) {
            MapleSessionCoordinator.getInstance().closeSession(c.getSession(), true);
            return;
        }
        
        IoSession session = c.getSession();
        AntiMulticlientResult res = MapleSessionCoordinator.getInstance().attemptGameSession(session, c.getAccID(), hwid);
        if (res != AntiMulticlientResult.SUCCESS) {
            c.announce(CLogin.Packet.onSelectCharacterByVACResult(parseAntiMulticlientError(res)));
            return;
        }
        
        Server server = Server.getInstance();
        if(!server.haveCharacterEntry(c.getAccID(), charId)) {
            MapleSessionCoordinator.getInstance().closeSession(c.getSession(), true);
            return;
        }
        
        c.setWorld(server.getCharacterWorld(charId));
        
        World wserv = c.getWorldServer();
        if(wserv == null || wserv.isWorldCapacityFull()) {
            c.announce(CLogin.Packet.onSelectCharacterByVACResult(LoginResultType.TooManyConnections.getReason()));
            return;
        }
        
        try {
            int channel = Randomizer.rand(1, wserv.getChannelsSize());
            c.setChannel(channel);
        } catch (Exception e) {
            e.printStackTrace();
            c.setChannel(1);
        }
        
        String[] socket = server.getInetSocket(c.getWorld(), c.getChannel());
        if(socket == null) {
            c.announce(CLogin.Packet.onSelectCharacterByVACResult(LoginResultType.TooManyConnections.getReason()));
            return;
        }
        
        server.unregisterLoginState(c);
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
        server.setCharacteridInTransition(session, charId);
        
        try {
            c.announce(CLogin.Packet.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
