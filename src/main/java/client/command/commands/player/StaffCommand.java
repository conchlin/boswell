/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
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

/*
   @Author: Arthur L - Refactored command content into modules
*/
package client.command.commands.player;

import client.MapleClient;
import client.command.Command;
import enums.BroadcastMessageType;
import network.packet.context.BroadcastMsgPacket;
import tools.MaplePacketCreator;

public class StaffCommand extends Command {
    {
        setDescription(" Displays current staff list");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(), "Boswell Staff:"));
        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(), "Contempt - Admin"));
        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(), "Saffron - Admin/Developer"));
        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(), "michu - Developer"));
        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(), "w - Developer"));
        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(), "Ponzu - Developer"));
        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(), "Elon - Game Master"));
        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(), "Frida - Game Master"));
        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(), "Alex - Game Master"));

    }
}