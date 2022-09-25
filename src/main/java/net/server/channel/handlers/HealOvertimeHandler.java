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

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanManager;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import network.packet.UserRemote;
import server.maps.MapleMapFactory;
import tools.FilePrinter;
import tools.data.input.SeekableLittleEndianAccessor;

public final class HealOvertimeHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if(!chr.isLoggedinWorld()) return;
        
        AutobanManager abm = chr.getAutobanManager();
        int timestamp = Server.getInstance().getCurrentTimestamp();
        slea.skip(8);
        
        short healHP = slea.readShort();
        
        if (healHP != 0) {
            abm.setTimestamp(8, timestamp, 28);  // thanks Vcoc & Thora for pointing out d/c happening here
            if ((abm.getLastSpam(0) + 1500) > timestamp) {
                FilePrinter.print(FilePrinter.HEAL_OVERTIME + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " triggered the HP regen spam alert in map " + c.getPlayer().getMapId());
                //AutobanFactory.FAST_HP_HEALING.addPoint(abm, "Fast hp healing");
            }
            
            int abHeal = (int)(77 * MapleMapFactory.getMapRecoveryRate(chr.getMapId()) * 1.5); // Sleepywood sauna and showa spa...         
            if (healHP > abHeal * 2.5) {
                // AutobanFactory.HIGH_HP_HEALING.alert(chr, "Healing: " + healHP + "; Max is " + abHeal + ".");
                FilePrinter.print(FilePrinter.HEAL_OVERTIME + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " triggered the high hp healing alert in map " + c.getPlayer().getMapId() + "with the values of abHeal: " + abHeal + " and healHP: " + healHP);
                //c.disconnect(true, false);
                return;
            }
            
            chr.addHP(healHP);///////
            chr.getMap().broadcastMessage(chr, UserRemote.Packet.showHpHealed(chr.getId(), healHP), false);
            abm.spam(0, timestamp);
        }
        
        /*short healMP = slea.readShort();
        
        if (healMP != 0 && healMP < 1000) {
            abm.setTimestamp(9, timestamp, 28);
            
            if ((abm.getLastSpam(1) + 1500) > timestamp) {
                FilePrinter.print(FilePrinter.HEAL_OVERTIME + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " triggered the MP regen spam alert in map " + c.getPlayer().getMapId() + "with timestamp of" + timestamp + "and heal mp of " + healMP);
                //AutobanFactory.FAST_MP_HEALING.addPoint(abm, "Fast mp healing");
            }
            
            chr.addMP(healMP);
            abm.spam(1, timestamp);
        }*/
    }
}
