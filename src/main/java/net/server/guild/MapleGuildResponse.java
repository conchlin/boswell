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
package net.server.guild;

import network.packet.context.GuildPacket;

public enum MapleGuildResponse {
    NOT_IN_CHANNEL(0x2a),
    ALREADY_IN_GUILD(0x28),
    NOT_IN_GUILD(0x2d),
    NOT_FOUND_INVITE(0x2e),
    MANAGING_INVITE(0x36),
    DENIED_INVITE(0x37);
    
    private int value;

    MapleGuildResponse(int val) {
        value = val;
    }

    public final byte[] getPacket(String targetName) {
        if (value >= MANAGING_INVITE.value) {
            return GuildPacket.Packet.onGuildMessage((byte) value, targetName);
        } else {
            return GuildPacket.Packet.onGuildMessage((byte) value);
        }
    }
}
