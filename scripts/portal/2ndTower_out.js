function enter(pi) {
    if (pi.getPlayer().getMap().countMonsters() == 0) {
        var eim = pi.getEventInstance();
        
        pi.message("The second ward of the Lion King's Castle has been broken.");
        pi.warp(211060400, 4);
        pi.completeQuest(3140);
        eim.clearPQ();
        return true;
    } else {
        return false;
    }
}