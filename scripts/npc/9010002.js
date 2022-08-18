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

/*

Ore of the Month Exchange NPC.
by: Nina
for: NobleStory

Adapted by Saffron for Avenue

 */

var EVENT_TROPHY = 4008002; // bronze
var GARNET = 4021000;
var AMETHYST = 4021001;
var AQUAMARINE = 4021002;
var EMERALD = 4021003;
var OPAL = 4021004;
var SAPPHIRE = 4021005;
var TOPAZ = 4021006;
var DIAMOND = 4021007;
var BLACK_CRYSTAL = 4021008;
var GEMS = [GARNET, AMETHYST, AQUAMARINE, EMERALD, OPAL, SAPPHIRE, TOPAZ, DIAMOND, BLACK_CRYSTAL];
var status;

function getDailyDate() {
    var today = new Date();
    var dd = today.getDate();

    if (dd < 10) {
        dd = '0' + dd;
    }

    return parseInt(dd);
}

function calculateGem(id) {
    var mod = cm.getPlayer().getId() % 9;
    if (mod === 0) {
        mod = 9;
    }

    var res = getDailyDate() % mod;

    return GEMS[res];
}


function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {

    if (mode === -1) {
        cm.dispose();
    } else {
        if (mode === 0 && status > -1) {
            cm.dispose();
        }
        if (mode === 1) {
            status++;
        } else {
            status--;
        }

        if (status === 0) {
            var pid = cm.getPlayer().getId()
            var INTRO = ("Greetings, #e#d#h ##k#n! \r\n\r\nMy name is #e#bMia#k#n and I love the twinkle of beautiful gems."
                + "They're so fascinating, don't you agree? I think you do!");
            var OFFER = ("\r\nHow about this: You help me to expand my collection, and I shall liberally reward you. Today,"
                + " I have a preference for #e#b#t" + calculateGem(pid) + "#s#k#n #i" + calculateGem(pid) + "#.");
            var OPTION_0 = ("#L00##e#bTrade gems#k#n#l");
            var OPTION_1 = ("#L01##e#bWhat can you offer me for my efforts?#k#n#l");

            cm.sendSimple(INTRO + OFFER + "\r\n\r\n" + OPTION_0 + "\r\n" + OPTION_1);
        }

        if (status === 1) {
            var pid = cm.getPlayer().getId()
            switch (selection) {
                case 0:
                    if (cm.getDailyOre() === true) {
                        cm.sendOk("Thank you for adding to my collection. Come back tomorrow, when I've sorted through my new gems!");
                        cm.dispose();
                    } else {
                        cm.sendSimple("So, #e#d#h ##k#n... Do you have any gems for me? \r\n"
                            + "#L00##e#bExchange my 2 daily #r#t" + calculateGem(pid) + "#s#k#n #i" + calculateGem(pid) + "##l");
                    }
                    break;
                case 1:
                    cm.sendOk("Don't worry about that. To me, there is nothing that can outclass the majestic appearance "
                        + "of an impeccable gem. And I am very generous when it comes to paying for them. \r\n\r\n\r\n"
                        + "#eBring me #r2#k of my desired gems and I will reward you with:#n "
                        + "\r\n#e#d5 #nx #e#b#t" + EVENT_TROPHY + "# #i" + EVENT_TROPHY + "##k#n");
                    cm.dispose();
            }
        }

        if (status === 2) {
            var pid = cm.getPlayer().getId()
            var AMOUNT = 2;
            var PRIZE = 5;
            if (cm.haveItem(calculateGem(pid), AMOUNT)) {
                if (cm.canHold(EVENT_TROPHY, PRIZE)) {
                    cm.gainItem(calculateGem(pid), -AMOUNT);
                    cm.gainItem(EVENT_TROPHY, PRIZE);
                    cm.setDailyOre(true);
                } else {
                    cm.sendOk("Please free up some space in your ETC slot.");
                }
                cm.dispose();
            } else {
                cm.sendOk("You don't seem to have enough #e#r#t" + calculateGem(pid) + "#s#k#n #i" + calculateGem(pid) + "# for this trade.");
                cm.dispose();
            }
        }
    }
}