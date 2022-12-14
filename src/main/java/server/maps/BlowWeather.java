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
package server.maps;

import client.MapleClient;
import network.packet.field.CField;

public class BlowWeather {
    private String msg;
    private int itemId;

    public BlowWeather(String msg, int itemId) {
        this.msg = msg;
        this.itemId = itemId;
    }

    public final byte[] makeDestroyData() {
        return CField.Packet.onBlowWeather(0, msg, false);
    }

    public final byte[] makeStartData() {
        return CField.Packet.onBlowWeather(itemId, msg, true);
    }

    public void sendStartData(MapleClient client) {
        client.announce(makeStartData());
    }
}
