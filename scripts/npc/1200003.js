/**
----------------------------------------------------------------------------------
	Whale from Rien to Lith
	Puro
----------------------------------------------------------------------------------
**/

function start() {
    cm.sendYesNo("Are you thinking about leaving Rien and heading back? If you board this ship, I can take you from #bLith Harbor#k to #bRien#k and back. Would you like to go to #bVinctoria Island#k?\r\n\r\n The trip costs #b1000 Mesos#k");
}

function action(mode, type, selection) {
    if (mode == 1) { // yes
        if (cm.getMeso() < 1000) {
            cm.sendOk("Hmm.. Are you sure that you have #b1000 Mesos#k? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you aboard the Genie.");
        } else {
            cm.gainMeso(-1000);
            cm.warp(104000000, 26);
        }
    } else if (mode == 0) { // no
        cm.sendOk("Okay, talk to me if you change your mind!");
    }
    cm.dispose();
}