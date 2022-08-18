function enter(pi) {
	if (pi.isQuestCompleted(3141)) { // looking into the lion king 1
		pi.playPortalSound();
    	pi.warp(211060610, 1);
		return true;
	} else if (pi.isQuestCompleted(3140)) {
		pi.openNpc(2161002);
		return false;
	}
	
	pi.message("You have been blocked by some sort of magical barrier."); 
	return false;
}