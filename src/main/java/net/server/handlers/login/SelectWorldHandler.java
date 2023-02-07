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
import net.AbstractMaplePacketHandler;
import net.server.Server;
import net.server.world.World;
import network.packet.CLogin;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SelectWorldHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        int world = slea.readByte();
        
        World wserv = Server.getInstance().getWorld(world);
        if(wserv == null || wserv.isWorldCapacityFull()) {
            c.announce(CLogin.Packet.onCheckUserLimitResult(2)); // full
            return;
        }
        
        int channel = slea.readByte() + 1;
        if(wserv.getChannel(channel) == null) {
            c.announce(CLogin.Packet.onCheckUserLimitResult(2)); // full
            return;
        }
        
        c.setWorld(world);
        c.setChannel(channel);
        c.sendCharList(world);
    }
}