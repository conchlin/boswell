// @Author Groat
// Boss Quest 

importPackage(Packages.world);
importPackage(Packages.client);

var exitMap;
var instanceId;
var monster;
monster = new Array(
	2220000, //Mano
	3220000, // Stumpy,
	3220001, //Deo
	9300003, // Slime King
	4220000, //Seruf
	4130103, // Rombot
	5220002, //Faust
	9600009, //Giant Centipede
	5220000, // King Clang
	5220003, //Timer
	6130101, //Mushmom
	6220000, //Dyle
	6220001, //Zeno
	6300005, //Zombie Mushmom
	7220001, //Old Fox
	7130400, //Yellow King Goblin
	7130401, //Blue King Goblin
	7130402, //Green King Goblin
	7220000, //Tae Roon
	7220002, //King Sage Cat
	8130100, //Jr. Balrog
	8220000, // Elliza
	8220002, //Kimera
	9001000, //Dances with Balrog Clone
	9001001, //Grendel the Really Old Clone
	9001002, //Athena Pierce Clone
	9001003, //Dark Lord Clone
	9001004, //Shadow Kyrin
	9300012, // Alishar
	9410015, //Snack Bar
	8220001, // Yeti on Skis
	9400205, //Blue Mushmom
	9300039, // Papa Pixie
	9300178, //Poison Golem
	9400549, // Headless Horseman
	8180001, // Griffey
	8220005, //Lilydouche / lilynouch
	8220006, //Lyka
	9400014, // Black Crow
	9400575, // BigFoot
	9400121, // Female Boss
	9400405, //Kush master or something / Kacchuu Musha
	9400300, // The Boss
	9400408  // Castellan ] revives into Castellan Toad
); 


function init() {
}

function monsterValue(eim, mobId) {
	return 1;
}

function setup(partyid) {
	exitMap = em.getChannelServer().getMapFactory().getMap(220000000);
	var instanceName = "BossQuestHell" + partyid;

	var eim = em.newInstance(instanceName);
	var mf = eim.getMapFactory();
	var map = mf.getMap(980000604, false, true, false);
	map.toggleDrops();
	map.setEvent(true);
	eim.setProperty("points", 0);
	eim.setProperty("monster_number", 0);

	eim.schedule("beginQuest", 5000);
	return eim;
}

function playerEntry(eim, player) {
	var map = eim.getMapInstance(980000604);
	player.changeMap(map, map.getPortal(0));
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) { 
	player.setHp(player.getMaxHp());
	playerExit(eim, player);
	return false;
}

function playerDisconnected(eim, player) {
	playerExit(eim, player);
}

function leftParty(eim, player) {			
	playerExit(eim, player);
}

function disbandParty(eim) {
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	}
}

function playerExit(eim, player) {
	var party = eim.getPlayers();
	var dispose = false;
	if (party.size() == 1) {
		dispose = true;
	}
	eim.saveBossQuestPoints(parseInt(eim.getProperty("points")), player);
	player.getClient().getSession().write(Packages.tools.MaplePacketCreator.serverNotice(6, "[Boss Quest] Your points have been awarded. Spend them as you wish. Better luck next time!"));
	eim.unregisterPlayer(player);
	player.changeMap(exitMap, exitMap.getPortal(0));
	if (dispose) {
		eim.dispose();
	}
}

function removePlayer(eim, player) {
	var party = eim.getPlayers();
	var dispose = false;
	if (party.size() == 1) {
		dispose = true;
	}
	eim.saveBossQuestPoints(parseInt(eim.getProperty("points")), player);
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(exitMap);
	if (dispose) {
		eim.dispose();
	}
}

function clearPQ(eim) {
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	}
}

function allMonstersDead(eim) {
	var monster_number = parseInt(eim.getProperty("monster_number"));
	var points = parseInt(eim.getProperty("points"));
	
	var monster_end = java.lang.System.currentTimeMillis();
	var monster_time = Math.round((monster_end - parseInt(eim.getProperty("monster_start"))) / 1000);
	
	if (0 - monster_time <= 0) points += (3 * (monster_number + 1));
	else points += (3 * (monster_number + 1));
	
	monster_number++;
	
	eim.setProperty("points", points);
	eim.setProperty("monster_number", monster_number);
	
	var map = eim.getMapInstance(980000604);

	if (monster_number > 43) {
		map.broadcastMessage(Packages.tools.MaplePacketCreator.serverNotice(6, "[Boss Quest] Congratulations! Your team has defeated all of the bosses with " + points + " points!"));
		map.broadcastMessage(Packages.tools.MaplePacketCreator.serverNotice(6, "[Boss Quest] Your points have been awarded, spend them as you wish."));
		//disbandParty();
	}
	else {
		map.broadcastMessage(Packages.tools.MaplePacketCreator.serverNotice(6, "[Boss Quest] Your team now has " + points + " points! The next boss will spawn in 5 seconds."));
		map.broadcastMessage(Packages.network.packet.field.CField.Packet.onClock(true, 5 * 60));
		eim.schedule("monsterSpawn", 5000);
	}
}

function monsterSpawn(eim) {
	var mob = Packages.server.life.MapleLifeFactory.getMonster(monster[parseInt(eim.getProperty("monster_number"))]);
	var overrideStats = new Packages.server.life.MapleMonsterStats();

	if (parseInt(eim.getProperty("monster_number")) > 35) overrideStats.setHp(mob.getHp());
	else overrideStats.setHp(mob.getHp() * 5);

	overrideStats.setExp(mob.getExp());
	overrideStats.setMp(mob.getMaxMp());
	mob.setOverrideStats(overrideStats);

	if (parseInt(eim.getProperty("monster_number")) > 35) mob.setHp(mob.getHp());
	else mob.setHp(mob.getHp() * 5);

	eim.registerMonster(mob);

	var map = eim.getMapInstance(980000604);
	map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(100, 100));
	eim.setProperty("monster_start", java.lang.System.currentTimeMillis());
}

function beginQuest(eim) {
	var map = eim.getMapInstance(980000604);
	map.broadcastMessage(Packages.tools.MaplePacketCreator.serverNotice(6, "[Boss Quest] The creatures of the darkness are coming in 10 seconds. Prepare for the worst!"));
	eim.schedule("monsterSpawn", 10000);
	map.broadcastMessage(Packages.network.packet.field.CField.Packet.onClock(true, 10 * 60));
}

function cancelSchedule() {
}