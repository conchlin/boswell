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

import client.inventory.ItemFactory;
import client.MapleCharacter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import client.MapleClient;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import network.packet.WvsContext;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author XoticStory
 */
public final class HiredMerchantRequest extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr.getMap().getMapObjectsInRange(chr.getPosition(), 23000,
                List.of(MapleMapObjectType.HIRED_MERCHANT)).isEmpty() && (GameConstants.isFreeMarketRoom(chr.getMapId()))) {
            if (!chr.hasMerchant()) {
                try {
                    if (ItemFactory.MERCHANT.loadItems(chr.getId(), false).isEmpty() && chr.getMerchantMeso() == 0) {
                        c.announce(WvsContext.Packet.hiredMerchantBox());
                    } else {
                        chr.announce(WvsContext.Packet.retrieveFirstMessage());
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                chr.dropMessage(1, "You already have a store open.");
            }
        } else {
            chr.dropMessage(1, "You cannot open your hired merchant here.");
        }
    }
}
