/**
 * npc: Luden
 * starts the spell breaker quests through the corresponding portal scripts
 * so very confusing because of all the quest and map checks T-T
 */

 var status = -1;

 function start() {
     action(1,0,0);
 }
 
 function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (cm.isQuestCompleted(3164) && !cm.isQuestCompleted(3139)) {
            if (status == 0) {
                cm.sendNext("It has been some time since anyone was foolhardy enough to enter our castle. You are brave, but you must also be wise!");
            } else if (status == 1) {
                cm.sendNext("... W-who's there..?! Are you a g-ghost???", 2);
            } else if (status == 2) {
                cm.sendNext("Sorry for startling you. I am Luden, guardian of this castle. I passed on to the spirit realm long ago, but i remain trapped here."); 
            } else if (status == 3) {
                cm.sendNext("Why are you stuck here? Are you one of those 'avenge me so I can go free' type ghosts? That's kind of a cliche, you know?", 2);
            } else if (status == 4) {
                cm.sendNext("I-well, yes. Sort of, I will tell you more if you come to me, but you will have to eliminate those evil Red Crockys " 
                + "on the Roof of the First Tower and break the magic ward there. I remember seeing an extraordinary keysmith in the area. He " 
                + "may be able to help you gain access to the Roof of the First Tower.");
            } else if (status == 5) {
                cm.startQuest(3139);
                cm.dispose();
            }
        } else if (cm.isQuestCompleted(3139) && cm.getPlayer().getMapId() == 211060200) {
            if (status == 0) {
                cm.sendNext("You eliminated the Red Crockys and broke the first ward. You are strong, but you will need to break two more wards to reach me. How about you turn back while you still can?");
            } else if (status == 1) {
                cm.sendNext("I'm getting all fired up! I'll be right there!", 2);
            } else if (status == 2) {
                cm.sendNext("I will pray for blessings of your victory. Please save these corrupted creatures.");
            } else if (status == 3) {
                cm.gainItem(4032832, -1); // we take away the first tower key
                cm.warp(211060300, 2);
                cm.dispose()
            }
        } else if (cm.isQuestCompleted(3139) && !cm.isQuestCompleted(3140) && cm.getPlayer().getMapId() == 211060400) {
            if (status == 0) {
                cm.sendNext("You've made it to the Second Gate so I'll get to the point. The ward on the Second Gate will break once you eliminate all the Prison Guard Boars on the Roof of the Second Tower.");
            } else if (status == 1) {
                cm.sendNext("Prison Guard Boar, huh? Can I eat him afterwards? I carry bacon seasoning with me at all times.", 2);
            } else if (status == 2) {
                cm.sendNext("Do as you like, but they truly are fearsome swines. Return to the locksmith and obtain the Key to the Roof of the Second Tower.");
            } else if (status == 3) {
                cm.startQuest(3140);
                cm.dispose();
            }
        } else if (cm.isQuestCompleted(3140) && cm.getPlayer().getMapId() == 211060400) { 
            if (status == 0) {
                cm.sendNext("The Prison Guard Boars were no match for you, but i fear greater obstacles are ahead! You must break the last ward. I have faith in you!");
            } else if (status == 1) {
                cm.sendNext("You got it ghost-guy!", 2);
            } else if (status == 2) {
                cm.sendNext("I will be waiting beyond the third magic ward. Be careful in these dangerous halls.");
            } else if (status == 3) {
                if (cm.hasItem(4032833)) cm.gainItem(4032833, -1); // we take away the second tower key
                cm.warp(211060500, 1);
                cm.dispose();
            }
        } else if (cm.isQuestCompleted(3140) && !cm.isQuestActive(3167) && !cm.isQuestCompleted(3141) && cm.getPlayer().getMapId() == 211060600) {
            if (status == 0) {
                cm.sendNext("You're finally at the last gate. Among the monsters that lurk in the castle, Prison Guard Rhinos are the most violent and frightening.");
            } else if (status == 1) {
                cm.sendNext("Will the wards be broken once I take them down this time?", 2);
            } else if (status == 2) {
                cm.sendNext("Yes, but do not walk in carelessly. I'd hate to see you get eaten.");
            } else if (status == 3) {
                cm.sendNext("Don't worry! I'll get the key from Jenn and break that ward in a jiffy!", 2);
            } else if (status == 4) {
                cm.startQuest(3141);
                cm.dispose();
            }
        } else if (cm.isQuestActive(3167) && cm.getPlayer().getMapId() == 211060600) {
            if (status == 0) {
                cm.sendNext("Luden, I have to eliminate some Bearwolves to obtain the key to the Roof of the Third, but I don't see them. What do I do?", 2);
            } else if (status == 1) {
                cm.sendNext("Bearwolves lurk at Under the Castle Walls 4, the area beyond your current location. I guess there's no choice. I will attempt to weaken the barrier temporarily. In the meantime, you can get the materials for the key.");
            } else if (status == 2) {
                cm.warp(211060700, 1);
                cm.dispose();
            }
        } else if (cm.isQuestCompleted(3141) && cm.getPlayer().getMapId() == 211060600) { 
            if (status == 0) {
                cm.sendNext("You've actually done it! I can finally fulfill my royal oath!");
            } else if (status == 1) {
                cm.sendNext("A royal oath? Are you talking about the Lion King?", 2);
            } else if (status == 2) {
                cm.sendNext("I am at the Fourth Tower. Please come find me, I grow weak. I will be looking forward to meeting you in person.");
            } else if (status == 3) {
                if (cm.hasItem(4032834)) cm.gainItem(4032834, -1); // we take away the second tower key
                cm.warp(211060700, 1);
                cm.dispose();
            }
        } else {
            cm.sendOk("I once lifted my sword in the king's service, and now I lift the sword against him. The is the proof of my loyalty. In stopping the changed king, I am serving him.");
            cm.dispose();
        }
    }
 }