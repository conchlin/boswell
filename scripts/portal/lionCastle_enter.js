function enter(pi) {
	if (pi.getPlayer().getLevel >= 115 || pi.getPlayer().isGM()) {
		pi.playPortalSound();
		pi.warp(211060010, "west00");
	} else {
		pi.message("This area is currently off limits to players. Testing is on-going.");
		//pi.getPlayer().dropMessage(5, "An ancient power is blocking your way to the castle.");
		return false;
	}
}