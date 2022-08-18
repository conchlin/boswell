/**
----------------------------------------------------------------------------------
	Train from ludi to orbis 
	2041000 Tian
----------------------------------------------------------------------------------
**/

function start() {
    cm.sendYesNo("This train is ready for takeoff. Looks like you're ready to travel once again! Would you like to go to #bOrbis#k?\r\n\r\n The trip costs #b10000 Mesos#k");
}

function action(mode, type, selection) {
    if (mode == 1) { // yes
        if (cm.getMeso() < 10000) {
            cm.sendOk("Hmm.. Are you sure that you have #b10000 Mesos#k? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you aboard the Genie.");
        } else {
            cm.gainMeso(-10000);
            cm.warp(200000121, 1);
        }
    } else if (mode == 0) { // no
        cm.sendOk("Okay, talk to me if you change your mind!");
    }
    cm.dispose();
}