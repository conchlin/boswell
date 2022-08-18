/**
----------------------------------------------------------------------------------
	Subway operator between kerning and nlc
	Bell
----------------------------------------------------------------------------------
**/

function start() {
    if (cm.getChar().getMapId() == 103000100) {
        cm.sendYesNo("This subway is ready for takeoff, next stop #bNew Leaf City#k! Are you done with everything here, would you like to go to #bNew Leaf City#k?\r\n\r\n The trip costs #b10000 Mesos#k");
    } else {
        cm.sendYesNo("This subway is ready for takeoff, next stop #bKerning City#k! Are you done with everything here, would you like to go to #bKerning City#k?\r\n\r\n The trip costs #b10000 Mesos#k");
    }
}

function action(mode, type, selection) {
    if (cm.getChar().getMapId() == 103000100) {
        if (mode == 1) { // yes
            if (cm.getMeso() < 10000) {
                cm.sendOk("Hmm.. Are you sure that you have #b10000 Mesos#k? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you aboard the Genie.");
            } else {
                cm.gainMeso(-10000);
                cm.warp(600010001, 0);
            }
        } else if (mode == 0) { // no
            cm.sendOk("Okay, talk to me if you change your mind!");
        }
    } else {
        if (mode == 1) { // yes
            if (cm.getMeso() < 10000) {
                cm.sendOk("Hmm.. Are you sure that you have #b10000 Mesos#k? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you aboard the Genie.");
            } else {
                cm.gainMeso(-10000);
                cm.warp(103000100, 0);
            }
        } else if (mode == 0) { // no
            cm.sendOk("Okay, talk to me if you change your mind!");
        }
    }
    cm.dispose();
}