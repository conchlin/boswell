var reward = new Array(
    ["You maplers have already taken all my items! Come back tomorrow and maybe then i'll have some goods.", 0],
    ["I don't have much right now but since you are one my regulars I'll give you some mesos for your troubles.", 0], // meso
    ["Stock has been pretty low these days but I do have a few of these trophies.", 4008002], //trophy
    ["Lucky for you we just got a huge shipment of trophies. My house isn't big enough to store them all!", 4008002]); // trophy

    function start() {
        status = -1;
        action(1, 0, 0);
    }

    function action(mode, type, selection) {
        if (mode == 0 && type == 0) {
            status--;
        } else if (mode == -1) {
            cm.dispose();
            return;
        } else {
            status++;
        }
    
        if (status == 0) {
            if (!cm.hasDailyEntry("MASON")) {       
                cm.sendYesNo("#d#eBoswell Daily#k\r\n \r\n#nI'm Mason the Collector, and I am a hoarder of precious items. Would you like to take a look at what I have today?");
            } else {
                cm.sendOk("Try again tomorrow, you have already completed this daily challenge!");
                status = 1;  
                cm.dispose();      
            }
        } else if (status == 1) {
            if (mode == 0) {//decline
                cm.sendNext("In that case you should come back later.");
            } else {
                var num = ~~(Math.random() * 4);
                var amount =  num == 3 ? 25 : 10;
                cm.sendNext("Hopefully Mason has some precious items in stock... \r\n \r\n #e" + reward[num][0] + "#n");
                switch(num) {
                    case 1:
                        cm.gainMeso(50000);
                        break;
                    case 2:
                    case 3: 
                        cm.gainItem(reward[num][1], amount);
                        break;
                }
            cm.completeDaily("MASON", false);
            }
        cm.dispose();
        }
    }