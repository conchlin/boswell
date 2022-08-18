/**
 * Portal to fight Krexel
 * Requires both the Mallet and Soul Lantern to enter 
 * 
 * @author Saffron
 * @Map 541020700
 */

function enter(pi) {
    if (!pi.haveItem(4031942) || !pi.haveItem(4000385)) { // check for mallet and soul lantern
        pi.message("You do not have all the items needed to enter.");
        return false;
    } else {
        pi.playPortalSound(); 
        pi.warp(541020800, 0);
        return true;
    }
}