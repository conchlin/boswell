/**
 * Commando Jim
 * Allows player to leave map after fighting krexel
 * 
 * @Author Saffron
 * @Map 541020800
 */

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else
        status--;
    if (status == 0)
        cm.sendYesNo("Would you like to leave this map?");
    else if (status == 1) {
        cm.warp(541020700, 0);
        cm.dispose();
    }
}