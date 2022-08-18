/* 
* @Author Reed
* @NPC Palanquin
* @Map Zipangu: Inside the Castle Gate
* @function warps player back to Kamuna
*/
var status = 0;
function start() {

    if(cm.getPlayer().getMapId() == 800000000) {
        cm.sendYesNo("We are... the palanquin... bearers! Need to... get to... Ninja Castle? Talk to us! Talk to us!");
    } else {
        cm.sendYesNo("We are the bearers of Palanquin! Let the bearers take you anywhere, would you like to return to Mushroom Shrine?");
    }
}

function action(mode, type, selection) {

    if(cm.getPlayer().getMapId() == 800000000) {

        if(mode == -1)
            cm.dispose();
        else {

            if(mode == 0) {
                cm.sendNext("Wait, how else are you going to get to the Ninja Castle?");
                cm.dispose();
                return;
            }
            status++;

            if(status == 1) {
                cm.warp(800040000, 0);
                cm.dispose();
            }
        }
    } else {

        if(mode == -1)
            cm.dispose();
        else {

            if(mode == 0) {
                cm.sendNext("Wait, how else are you going to get back?");
                cm.dispose();
                return;
            }
            status++;

            if(status == 1) {
                cm.warp(800000000, 0);
                cm.dispose();
            }
        }
    }
}
