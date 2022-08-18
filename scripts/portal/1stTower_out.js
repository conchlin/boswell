function enter(pi) {
    if (pi.getPlayer().getMap().countMonsters() == 0) {
        var eim = pi.getEventInstance();
        
        pi.message("The first ward of the Lion King's Castle has been broken");
        pi.warp(211060200, 3);
        pi.completeQuest(3139);
        eim.clearPQ();
        return true;
    } else {
        return false;
    }
}