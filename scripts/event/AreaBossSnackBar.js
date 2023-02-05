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
/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Snack Bar Spawner
-- Edited by --------------------------------------------------------------------------------------
	Ronan - based on xQuasar's King Clang spawner

**/
importPackage(Packages.network.packet.context);
importPackage(Packages.enums);

function init() {
    scheduleNew();
}

function scheduleNew() {
    setupTask = em.schedule("start", 0);    //spawns upon server start. Each 3 hours an server event checks if boss exists, if not spawns it instantly.
}

function cancelSchedule() {
    if (setupTask != null)
        setupTask.cancel(true);
}

function start() {
    var snackBarMap = em.getChannelServer().getMapFactory().getMap(105090310);
    var snackBar = Packages.server.life.MapleLifeFactory.getMonster(8220008);
	
	if(snackBarMap.getMonsterById(8220008) != null || snackBarMap.getMonsterById(8220009) != null) {
        var respawn = em.randomSpawnTime(3 * 60 *60 * 1000);
	    em.schedule("start", respawn);
		return;
	}
	
        var setPos = [[-626, -604], [735, -600]];
        var rndPos = setPos[Math.floor(Math.random() * setPos.length)];
        
        snackBarMap.spawnMonsterOnGroundBelow(snackBar, new Packages.java.awt.Point(rndPos[0], rndPos[1]));
        snackBarMap.broadcastMessage(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.BlueText.getType(), "Slowly, a suspicious food stand opens up on a strangely remote place."));
        var respawn = em.randomSpawnTime(3 * 60 *60 * 1000);
	    em.schedule("start", respawn);
}

// ---------- FILLER FUNCTIONS ----------

function dispose() {}

function setup(eim, leaderid) {}

function monsterValue(eim, mobid) {return 0;}

function disbandParty(eim, player) {}

function playerDisconnected(eim, player) {}

function playerEntry(eim, player) {}

function monsterKilled(mob, eim) {}

function scheduledTimeout(eim) {}

function afterSetup(eim) {}

function changedLeader(eim, leader) {}

function playerExit(eim, player) {}

function leftParty(eim, player) {}

function clearPQ(eim) {}

function allMonstersDead(eim) {}

function playerUnregistered(eim, player) {}

