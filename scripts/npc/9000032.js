/* function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else
        cm.dispose();
    if (cm.getClearance() > 0) {
        if (cm.getClearance() == 2) {
            cm.gainItem(1142104, 1);
        }
        if (status == 0 && mode == 1) {
            var selStr = "Hey #h #, thank you for helping us out during our alpha testing. You will notice that you still have all alpha rewards accredited to your account. However, player progress has been wiped and this is your chance to switch job type if you so choose."
            var options = new Array("I would like to remain an adventurer!","I'd like to be a Knight of Cygnus!","Aran is the choice for me!");
            for (var i = 0; i < options.length; i++){
                selStr += "\r\n#L" + i + "# " + options[i] + "#l";
            }
                    
            cm.sendSimple(selStr);
        }
        else if (status == 1 && mode == 1) {
            selectedType = selection;
            if (selectedType == 0){ // adventurer
                var selStr = "Wonderful! You will remain an adventurer. Good luck with your journey. :)";
                cm.sendOk(selStr);
                cm.setClearance(0);
                cm.dispose();
                if (cm.getPlayer().getGender < 0) { //female
                    cm.gainItem(1041006, 1); // shirt
                    cm.gainItem(1061008, 1); // pant
                    cm.gainItem(1302000, 1); // weapon
                    cm.gainItem(1072005, 1); // sandal
                } else { // male
                    cm.gainItem(1040002, 1); // shirt
                    cm.gainItem(1060002, 1); // pant
                    cm.gainItem(1302000, 1); // weapon
                    cm.gainItem(1072005, 1); // sandal
                }
            }
            if (selectedType == 1){ // KoC 
                cm.warp(130030000);
                cm.changeJobById(1000);
                cm.setClearance(0);
                cm.dispose();
                if (cm.getPlayer().getGender < 0) { //female
                    cm.gainItem(1041006, 1); // shirt
                    cm.gainItem(1061008, 1); // pant
                    cm.gainItem(1302000, 1); // weapon
                    cm.gainItem(1072005, 1); // sandal
                } else { // male
                    cm.gainItem(1040002, 1); // shirt
                    cm.gainItem(1060002, 1); // pant
                    cm.gainItem(1302000, 1); // weapon
                    cm.gainItem(1072005, 1); // sandal
                }
            }
            if (selectedType == 2){ // aran 
                cm.warp(914090010);
                cm.changeJobById(2000);
                cm.setClearance(0);
                cm.dispose();

                cm.gainItem(1042167, 1); // shirt
                cm.gainItem(1062115, 1); // pant
                cm.gainItem(1072383, 1); // sandal
                cm.gainItem(1442079, 1); // weapon
            }
        }
    } else {
        cm.sendOk("It seems that you were not part of alpha testing. However, make sure to enjoy Beta!");
        cm.dispose();
    }
} */