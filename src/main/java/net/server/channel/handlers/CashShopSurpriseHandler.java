/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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

import client.MapleClient;
import client.inventory.Item;
import net.AbstractMaplePacketHandler;
import network.packet.CCashShop;
import server.cashshop.CashShop;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.Pair;

/**
 *
 * @author RonanLana
 */
public class CashShopSurpriseHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        CashShop cs = c.getPlayer().getCashShop();
        
        if(cs.isOpened()) {
            Pair<Item, Item> cssResult = cs.openCashShopSurprise();
            
            if(cssResult != null) {
                //Item cssItem = cssResult.getLeft(), cssBox = cssResult.getRight();
                //c.announce(MaplePacketCreator.onCashGachaponOpenSuccess(c.getAccID(), cssBox.getSN(), cssBox.getQuantity(), cssItem, cssItem.getItemId(), cssItem.getQuantity(), true));
                c.announce(CCashShop.Packet.onCashItemResultMessage((byte) 0xA4));
            } else {
                //c.announce(MaplePacketCreator.onCashItemGachaponOpenFailed());
                c.announce(CCashShop.Packet.onCashItemResultMessage((byte) 0x00));
            }
        }
    }
}
