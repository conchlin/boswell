function enter(pi) {
    if (pi.getPlayer().getMap().countMonsters() == 0) {
        var eim = pi.getEventInstance();
        
        pi.message("The third ward of the Lion King's Castle has been broken.");
        pi.warp(211060600, 6);
        pi.completeQuest(3141);
        eim.clearPQ();
        return true;
    } else {
        return false;
    }
}