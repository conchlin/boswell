/**
----------------------------------------------------------------------------------
	Sky Ferry from  Ereve to Orbis.
	1100004 Kiru
----------------------------------------------------------------------------------
**/

function start() {
    cm.sendYesNo("Hmm... The winds seem favorable. Are you thinking of leaving #bEreve#k and going elsewhere? This ferry sails to the #bOssyria Continent#k! I hope you've taken care of everything you needed to in #bEreve#k. What do you say, would you like to go to #bOrbis#k?\r\n\r\n The trip costs #b1000 Mesos#k");
}

function action(mode, type, selection) {
    if (mode == 1) { // yes
        if (cm.getMeso() < 1000) {
            cm.sendOk("Hmm.. Are you sure that you have #b1000 Mesos#k? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you get on...");
        } else {
            cm.gainMeso(-1000);
            cm.warp(200000161, 1);
        }
    } else if (mode == 0) { // no
        cm.sendOk("If you're not interested, then oh well...");
    }
    cm.dispose();
}