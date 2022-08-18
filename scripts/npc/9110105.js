/* 
* @NPC Naosuke
* @Map 800040211 - Zipangu : Castle Corridor
*/

var status = -1;

function start() {
    cm.sendNext("What are you...?");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1){
        if(mode == 0)
           cm.sendNext("Good. This is a dangerous area.");
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.sendYesNo("What? You want to go where? Do you even know where this leads?");
    } else if (status == 1){
        cm.warp(800040301); // phantom's room
        cm.dispose();
    }
}