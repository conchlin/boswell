/**
----------------------------------------------------------------------------------
	Skyferry from Ellinia to Ereve
	1100007 Kiriru
----------------------------------------------------------------------------------
**/

function start() {
    cm.sendYesNo("Oh, and.. so.. this ship will take you to #bEreve#k, the place where you'll find crimson leaves soaking up the sun, the gently breeze that glides past the stream, and the Empress of Maple, Cygnus. Would you like to head over to #bEreve#k? \r\n\r\n The trip costs #b1000 Mesos#k");
}

function action(mode, type, selection) {
    if (mode == 1) { // yes
        if (cm.getMeso() < 1000) {
            cm.sendOk("Hmm.. Are you sure that you have #b1000 Mesos#k? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you get on...");
        } else {
            cm.gainMeso(-1000);
            cm.warp(130000210);
        }
    } else if (mode == 0) { // no
        cm.sendOk("If you're not interested, then oh well...");
    }
    cm.dispose();
}