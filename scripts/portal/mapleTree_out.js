/**
 * @author  Saffron
 * @Map 97001000 - Maple Hill 
 * @Function warps user out from Maple Hill
 *
 */
function enter(pi) {
    pi.playPortalSound(); pi.warp(pi.getPlayer().getSavedLocation("MIRROR"));
    return true;
}