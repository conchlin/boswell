/**
----------------------------------------------------------------------------------
	Sky Ferry from Ereve to Victoria Island 
	1100003 Kiriru
----------------------------------------------------------------------------------
**/

function start() {
    cm.sendYesNo(" Oh Hello...again. Do you want to leave Ereve and go somewhere else? If so, you've come to the right place. I operate a ferry that goes from #bEreve#k to #bVictoria Island#k, would you like to head over to #bVictoria Island#k? \r\n\r\n The trip costs #b1000 Mesos#k");
}

function action(mode, type, selection) {
    if (mode == 1) { // yes
        if (cm.getMeso() < 1000) {
            cm.sendOk("Hmm.. Are you sure that you have #b1000 Mesos#k? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you get on...");
        } else {
            cm.gainMeso(-1000);
            cm.warp(101000400);
        }
    } else if (mode == 0) { // no
        cm.sendOk("If you're not interested, then oh well...");
    }
    cm.dispose();
}