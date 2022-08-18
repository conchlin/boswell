/**
 * @author  Saffron
 * @Map 741000200 - Fishing Lagoon 
 * @Function warps user out from Fishing Lagoon
 *
 */
function enter(pi) {
    pi.playPortalSound(); pi.warp(pi.getPlayer().getSavedLocation("MIRROR"));
    return true;
}