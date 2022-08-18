/**
----------------------------------------------------------------------------------
	Travel from Kerning to CBD
	Irene
----------------------------------------------------------------------------------
**/

function start() {
    cm.sendYesNo("Hello, I am Irene from Singapore Airport. I can assist you in getting you to Singapore in no time. Do you want to go to Singapore?\r\n\r\n The trip costs #b10000 Mesos#k");
}

function action(mode, type, selection) {
    if (mode == 1) { // yes
        if (cm.getMeso() < 10000) {
            cm.sendOk("Hmm.. Are you sure that you have #b1000 Mesos#k? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you aboard the Genie.");
        } else {
            cm.gainMeso(-10000);
            cm.warp(540010000, 0);
        }
    } else if (mode == 0) { // no
        cm.sendOk("Okay, talk to me if you change your mind!");
    }
    cm.dispose();
}