var reward = new Array(
    ["As Bart naps you scan the shoreline for any disturbances. However, by the time he wakes you really " 
        + "didn't see much of anything happen. He doesn't really seem to have that hard of a job...", 0],
    ["As Bart naps you scan the shoreline for any disturbances. You also become very aware of just how " 
        + "annoying it must be to work all day with seagulls. However, just before Bart wakes up " 
        + "there is a shimering of a big bag of Mesos on the shoreline. Score!", 0], // meso
    ["As Bart naps you scan the shoreline for any disturbances. It seems the Seagulls are being helpful " 
        + "today and have brought you some Bronze Trophies. Bart will be surprised to hear this when he wakes!", 4008002], //trophy
    ["As Bart naps you scan the shoreline for any disturbances. Through the telescope you see a huge " 
        + "container of Bronze Trophies! It looks like they washed ashore for some reason... Oh well that just means more trophies for you!", 4008002]); // trophy

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
            if (!cm.hasDailyEntry("WATCHTOWER")) {       
                cm.sendYesNo("#d#eBoswell Daily#k\r\n \r\n#nI'm Bart, and I protect the shores of Nautilus Harbour with the help of " 
                            + "my Seagull friends. It's tough work but someone's got to do it. The only problem is that I rarely " 
                            + "get any time off to sleep. Could you watch over the Harbour while i take a quick nap?");
            } else {
                cm.sendOk("I feel so rejuvenated from my nap! You should come back tomorrow.");
                status = 1;  
                cm.dispose();      
            }
        } else if (status == 1) {
            if (mode == 0) {//decline
                cm.sendNext("Y'know I really could use some help I'm rather sleepy.");
            } else {
                var num = ~~(Math.random() * 4);
                var amount =  num == 3 ? 25 : 10;
                cm.sendNext("Wonderful, it's been so long since i've had a good nap. Here take this telescope so you can see" 
                            + " trouble if it comes! \r\n \r\n #e" + reward[num][0] + "#n\r\n\r\n Thanks for helping me out I feel incredibly refreshed!");
                switch(num) {
                    case 1:
                        cm.gainMeso(50000);
                        break;
                    case 2:
                    case 3: 
                        cm.gainItem(reward[num][1], amount);
                        break;
                }
            cm.completeDaily("WATCHTOWER", false);
            }
        cm.dispose();
        }
    }