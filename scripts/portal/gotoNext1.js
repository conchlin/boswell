function enter(pi) {
	if (pi.isQuestActive(3140) || pi.isQuestCompleted(3140)) { 
		// or you are doing the second spell breaker quest
		// or you've already completed second spell breaker quest
		pi.playPortalSound();
    	pi.warp(211060300, 2);
		return true;
	} else if (pi.isQuestCompleted(3164)) { // completion of Jenn's brother quest
		pi.openNpc(2161002);
		return false;
	}

	pi.message("You have been blocked by some sort of magical barrier."); 
	return false;
}
