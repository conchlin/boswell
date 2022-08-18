function enter(pi) {
	if (pi.isQuestActive(3147)) {
		pi.playPortalSound();
		pi.warp(211061100, 1);
		return true;
	}

	pi.message("A great sense of evil blocks this door.");
	return false;
}