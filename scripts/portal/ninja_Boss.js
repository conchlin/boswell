/**
 * Warps player to Tenshu Room
 * 
 * @map 800040401
 */
function enter(pi) {
	pi.playPortalSound();
	pi.warp(800040410, 0);
	return true;
}