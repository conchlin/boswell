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
import enums.CharDeleteResultType;
import enums.PinCodeResultType;
import net.AbstractMaplePacketHandler;
import network.packet.CLogin;
import tools.FilePrinter;
import tools.data.input.SeekableLittleEndianAccessor;

public final class DeleteCharHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String pic = slea.readMapleAsciiString();
        int cid = slea.readInt();

        // for some reason this requests the player's pic in this instance
        CLogin.Packet.onCheckPinCodeResult(PinCodeResultType.Request.getMode());
        
        if (c.checkPic(pic)) {
            if(c.deleteCharacter(cid, c.getAccID())) {
                FilePrinter.print(FilePrinter.DELETED_CHAR + c.getAccountName() + ".txt", c.getAccountName() + " deleted CID: " + cid);
                c.announce(CLogin.Packet.deleteCharResponse(cid, CharDeleteResultType.DeleteOk.getState()));
            } else {
                c.announce(CLogin.Packet.deleteCharResponse(cid, CharDeleteResultType.IncorrectPic.getState()));
            }
        } else {
            c.announce(CLogin.Packet.deleteCharResponse(cid, CharDeleteResultType.IncorrectPic.getState()));
        }
    }
}
