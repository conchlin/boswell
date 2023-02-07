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
import net.AbstractMaplePacketHandler;
import network.packet.CCashShop;
import network.packet.context.WvsContext;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Ronan
 */
public final class CheckTransferWorldPossibleHandler extends AbstractMaplePacketHandler {
    
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt(); //cid
        int birthday = slea.readInt();
        if (!CashOperationHandler.checkBirthday(c, birthday)) {
            c.announce(CCashShop.Packet.onCashItemResultMessage((byte) 0xC4));
            c.announce(WvsContext.Packet.enableActions());
            return;
        }
        
        c.announce(CCashShop.Packet.onCheckTransferWorldPossibleResult(9));
    }
}