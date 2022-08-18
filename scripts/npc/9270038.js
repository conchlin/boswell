/**
----------------------------------------------------------------------------------
	Travel from CBD to Kerning
	Shalon
----------------------------------------------------------------------------------
**/

function start() {
    cm.sendYesNo("Hello, I am Shalon from Singapore Airport. I can assist you in getting you to Kerning City in no time. Do you want to go to Kerning City?	\r\n\r\n The trip costs #b10000 Mesos#k");
}

function action(mode, type, selection) {
    if (mode == 1) { // yes
        if (cm.getMeso() < 10000) {
            cm.sendOk("Hmm.. Are you sure that you have #b10000 Mesos#k? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you aboard the Genie.");
        } else {
            cm.gainMeso(-10000);
            cm.warp(103000000, 4);
        }
    } else if (mode == 0) { // no
        cm.sendOk("Okay, talk to me if you change your mind!");
    }
    cm.dispose();
}