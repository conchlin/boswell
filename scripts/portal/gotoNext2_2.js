function enter(pi) {
	if (pi.isQuestActive(3141) || pi.isQuestCompleted(3140)) { // looking into the lion king 1
		pi.playPortalSound();
    	pi.warp(211060410, 1);
		return true;
	} else if (pi.isQuestCompleted(3139)) {
		pi.openNpc(2161002);
		return false;
	}
	
	pi.message("You have been blocked by some sort of magical barrier."); 
	return false;
}