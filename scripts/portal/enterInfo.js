importPackage(Packages.server.life);

function enter(pi) {

    if (pi.isQuestActive(21733)) { // gathering some strange informations
        pi.warp(910400000, 1);
        
        var danger = pi.getEventManager("DangerousInfo");
        var puppet = MapleLifeFactory.getMonster(9300345);

		danger.setProperty("player", pi.getPlayer().getName());
        danger.startInstance(pi.getPlayer());

        pi.getMap(910400000).spawnMonsterOnGroundBelow(puppet, new java.awt.Point(0, 0)); 
        pi.getPlayer().message("The puppeteer has taken over the info shop! You should check it out and defeat the puppeteer!");

        if (cm.getPlayer().getMap() == 910400000) { // block leaving hehe
            return;
        }
        
		return;
    }
    
    pi.playPortalSound();
    pi.warp(104000004, 1);
    return true;
}