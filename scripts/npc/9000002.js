/*
    This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
var status = -1;

var trophy1 = 1142097; // Beta Tester
var trophy2 = 1142098; // Established Beta Tester
var trophy3 = 1142099; // Outstanding Beta Tester
function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {

    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0) {
            cm.dispose();
            return;
        }

        if (mode == 1)
            status++;
        else
            status--;

        if (status == 0) {
            cm.sendSimple("Congratulations on finishing the event! Trade in your trophy for a cool surprise!" +
                "\r\n #L0##b 1st Place Trophy#l" +
                "\r\n #L1##b 2nd Place Trophy#l" +
                "\r\n #L2##b 3rd Place Trophy#l");
        } else if (status == 1) {
            if (selection === 0) {
                if (cm.haveItem(trophy1, 1)) {
                    if(cm.canHold(1302000) && cm.canHold(2000000) && cm.canHold(3010001) && cm.canHold(4000000)) { // One free slot in every inventory.
                        cm.gainItem(trophy1, -1);
                        cm.giveEventDrop();
                    } else {
                        cm.sendOk("Please have at least one slot in your #rEQUIP, USE, SET-UP, #kand #rETC#k inventories free.");
                    }
                }
            } else if (selection === 1) {
                if (cm.haveItem(trophy2, 1)) {
                    if(cm.canHold(1302000) && cm.canHold(2000000) && cm.canHold(3010001) && cm.canHold(4000000)) { // One free slot in every inventory.
                        cm.gainItem(trophy2, -1);
                        cm.giveEventDrop();
                    } else {
                        cm.sendOk("Please have at least one slot in your #rEQUIP, USE, SET-UP, #kand #rETC#k inventories free.");
                    }
                }
            } else if (selection === 2) {
                if (cm.haveItem(trophy3, 1)) {
                    if(cm.canHold(1302000) && cm.canHold(2000000) && cm.canHold(3010001) && cm.canHold(4000000)) { // One free slot in every inventory.
                        cm.gainItem(trophy3, -1);
                        cm.giveEventDrop();
                    } else {
                        cm.sendOk("Please have at least one slot in your #rEQUIP, USE, SET-UP, #kand #rETC#k inventories free.");
                    }
                }
            }
            cm.dispose();
        }
    }
} 
