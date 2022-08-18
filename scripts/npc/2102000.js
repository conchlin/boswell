/**
----------------------------------------------------------------------------------
	Genie from Ariant to Orbis
	2102000 Asesson
----------------------------------------------------------------------------------
**/

function start() {
    cm.sendYesNo("This genie is ready for takeoff. Hopefully those pesky Red Bandits haven't chased you out for good! Are you done with all your tasks in #bAriant#k, would you like to go to #bOrbis#k?\r\n\r\n The trip costs #b10000 Mesos#k");
}

function action(mode, type, selection) {
    if (mode == 1) { // yes
        if (cm.getMeso() < 10000) {
            cm.sendOk("Hmm.. Are you sure that you have #b1000 Mesos#k? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you aboard the Genie.");
        } else {
            cm.gainMeso(-10000);
            cm.warp(200000151, 1);
        }
    } else if (mode == 0) { // no
        cm.sendOk("Okay, talk to me if you change your mind!");
    }
    cm.dispose();
}