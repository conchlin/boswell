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

/* Name: Shumi JQ Chest #2 
 * Map: The Shumi JQ map -> shumi jq reward
 * Map: Turban Shell Hill -> TREASURE daily 
*/

var status;
var reward = new Array(
        ["It appears others have been a bit too greedy... The treasure chest is completely empty.", 0],
        ["A big bag of Mesos!", 0], // meso
        ["They look a bit dirty but there are definitely some trophies here.", 4008002], //trophy
        ["Score! There are a bunch of trophies. You should grab them before someone else does.", 4008002]); // trophy

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0 && type == 0) {
	    status--;
    } else if (mode == -1) {
	    cm.dispose();
	    return;
    } else {
	    status++;
    }

    if (status == 0) {
        var prizes = [4020005,4020006,4020007,4020008,4010000];
        if (cm.getMapId() == 230010300) {
            if (!cm.hasDailyEntry("TREASURE")) {       
	            cm.sendYesNo("#d#eBoswell Daily#k\r\n \r\n#nThis treasure chest looks like it could hold some valuable items! Would you like to open it?");
            } else {
                cm.sendOk("You have already completed this daily challenge!");
                status = 1;  
                cm.dispose();      
            }
        } else {
            if (cm.isQuestStarted(2056)) {
                cm.gainItem(4031040,1);
            } else {
                cm.gainItem(prizes[parseInt(Math.random() * prizes.length)],1);
            }

            cm.warp(103000100, 0);
            cm.dispose();
            }
    } else if (status == 1) {
	    if (mode == 0) {//decline
	        cm.sendNext("no");
	    } else {
            var num = ~~(Math.random() * 4);
            var amount =  num == 3 ? 25 : 10;
            cm.sendNext("You open the treasure chest to find... \r\n \r\n #e" + reward[num][0] + "#n");
            switch(num) {
                case 1:
                    cm.gainMeso(50000);
                    break;
                case 2:
                case 3: 
                    cm.gainItem(reward[num][1], amount);
                    break;
            }
        cm.completeDaily("TREASURE", false);
	    }
	cm.dispose();
    }
}