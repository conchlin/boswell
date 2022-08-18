function enter(pi) {
    if (pi.isQuestActive(3143)) { // looking into the lion king I
        pi.message("You should report your findings back to Krag."); 
        pi.setQuestProgress(3143, 1);
        pi.playPortalSound();
        pi.warp(211060800, 1);
        return true;
    } else if (pi.isQuestCompleted(3141)) {
        pi.playPortalSound();
        pi.warp(211060800, 1);
    }
    
    pi.message("You have been blocked by some sort of magical barrier."); 
    return false;
}