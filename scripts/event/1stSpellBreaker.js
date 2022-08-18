importPackage(Packages.tools);

var minPlayers = 1;
var timeLimit = 10; //10 minutes
var eventTimer = 1000 * 60 * timeLimit;
var exitMap = 211060200;
var eventMap = 211060201;

function init(){}

function setup(difficulty, lobbyId){
	var eim = em.newInstance("1stSpellBreaker_" +lobbyId);
	eim.getInstanceMap(eventMap).resetFully();
	eim.getInstanceMap(eventMap).allowSummonState(false);
	respawn(eim);
	eim.startEventTimer(eventTimer);

	return eim;
}

function afterSetup(eim){}

function respawn(eim){}

function playerEntry(eim, player){
	var shop = eim.getMapInstance(eventMap);
	player.changeMap(shop);

	player.getClient().announce(MaplePacketCreator.earnTitleMessage("Defeat the Red Crocky to break the first magic ward of the Lion King's Castle!"));
}

function scheduledTimeout(eim){
	var party = eim.getPlayers();

	for(var i = 0; i < party.size(); i++)
		playerExit(eim, party.get(i));

	eim.dispose();
}

function playerRevive(eim, player){
	player.respawn(eim, exitMap);
	return false;
}

function playerDead(eim, player){}

function playerDisconnected(eim, player){
	var party = eim.getPlayers();

	for(var i = 0; i < party.size(); i++){
		if(party.get(i).equals(player))
			removePlayer(eim, player);
		else
			playerExit(eim, party.get(i));
	}
	eim.dispose();
}

function monsterValue(eim, mobId){
	return -1;
}

function leftParty(eim, player){}

function disbandParty(eim){}

function playerUnregistered(eim, player){}

function playerExit(eim, player){
	eim.unregisterPlayer(player);
	player.changeMap(exitMap);
}

function changedMap(eim, player){}

function removePlayer(eim, player){
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(exitMap);
}

function cancelSchedule(){}

function dispose(){}

function clearPQ(eim){
	eim.stopEventTimer();
	eim.setEventCleared();
}

function monsterKilled(mob, eim){}

function allMonstersDead(eim){}

// ---------- FILLER FUNCTIONS ----------

function changedLeader(eim, leader) {}

