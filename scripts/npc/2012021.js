
/**
----------------------------------------------------------------------------------
	Cabin from Orbis to Leafre 
	2012021 Ramini
----------------------------------------------------------------------------------
**/

function start() {
    cm.sendYesNo("This ship is ready for takeoff. Within Leafre lies the Minar Forest and Dragon Canyon both crawling with strong monsters! Are you done with all your tasks in #bOrbis#k, would you like to go to #bLeafre#k?\r\n\r\n The trip costs #b10000 Mesos#k");
}

function action(mode, type, selection) {
    if (mode == 1) { // yes
        if (cm.getMeso() < 10000) {
            cm.sendOk("Hmm.. Are you sure that you have #b1000 Mesos#k? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you aboard the Genie.");
        } else {
            cm.gainMeso(-10000);
            cm.warp(240000110, 0);
        }
    } else if (mode == 0) { // no
        cm.sendOk("Okay, talk to me if you change your mind!");
    }
    cm.dispose();
}