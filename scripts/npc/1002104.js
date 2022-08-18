// tru
var status = -1;

function start() {
	action(1, 0, 0);	
}

function action(mode, type, selection) {  
	if (mode == -1) {
                cm.dispose();
    	} else {
        if (mode == 1)
            status++;
        else
            status--;

        if (cm.isQuestActive(21733)) {
		if (status == 0) {
			cm.sendNextPrev("Wow, I would have never guessed something like this would happen to me. Never in my wildest imagination did I think the puppeteer would enter here. I should have trained during my free time. I just got owned!", 8);
		} else if (status == 1) {
			cm.sendNextPrev("I'm so sorry. It's all my fault...", 2);
		} else if (status == 2) {
			cm.sendNextPrev("Hm?Why would you feel bad? You couldn't have known that they'd show up. No need to feel bad for me! If anything, they just revealed their weaknesses.", 8);
		} else if (status == 3) {
			cm.sendNextPrev("Their weaknesses?", 2);
		} else if (status == 4) {
			cm.sendNextPrev("There is no reason for the puppeteer to act so urgently if the document he lost was a fake. The proves that the docuement is the real deal and that the ultimate goal of the Black Wings is the Seal Stone of Victoria Island.", 8);
		} else if (status == 5) {
			cm.sendNextPrev("But your location has also been exposed...", 2);
		} else if (status == 6) {
			cm.sendNextPrev("Dont worry! I may have gotten attacked this time when I was preoccupied waiting for the items from Lilin, but I won't let that happen again. Never underestimate the power of the information dealer! I always find a way to escape wherever i go. I know! Since you helped me out I'll do the same for you!", 8);
		} else if (status == 7) {
                        cm.teachSkill(21100000, 0, 20, -1);
                        cm.forceCompleteQuest(21733)
                        cm.sendNextPrev("Oh, this must have been a skill you used previously in the past. Lilin sent it to me thinking it would be perfect for you. She says she discovered it wile investigating the records of the heroes. I was worried I might lose it, that's why I didnt put up much of a fight. I was trying to proctect it... Which I did!", 8);
		} else if (status == 8) {
                        cm.sendOk("No matter how the Black Wings try, they won't be able to stop you from returning to your original state. Keep training until you can deat Black Mage. I'll do my best to gather as much information as possible.");
                        cm.dispose();
                }	
        }
									
	}
}




/*var status = -1;
 
function start() {
        status = -1;
        action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
                cm.dispose();
        } else {
                if (mode == 0 && type > 0) {
                        cm.dispose();
                        return;
                }
                if (mode == 1 || status >= 0)
                        status++;
                else
                        status--;
    
                if(status == 0) {
                        cm.sendNext("Wow, I would have never guessed something like this would happen to me. Never in my wildest imagination did I think the puppeteer would enter here. I should have trained during my free time. I just got owned!");
                } else if (status == 1) {
                        cm.sendNext("Hm?Why would you feel bad? You couldn't have known that they'd show up. No need to feel bad for me! If anything, they just revealed their weaknesses.");
                } else if (status == 2) {
                        cm.sendNext("There is no reason for the puppeteer to act so urgently if the document he lost was a fake. The proves that the docuement is the real deal and that the ultimate goal of the Black Wings is the Seal Stone of Victoria Island.");
                } else if (status == 3) {
                        cm.sendNext("I may have gotten attacked this time when I was preoccupied waiting for the items from Lilin, but I won't let that happen again. Never underestimate the power of the information dealer! I always find a way to escape wherever i go. I know! Since you helped me out I'll do the same for you!");
                } else if (status == 4) {
                        cm.sendOk("Give stuff here");
                        cm.dispose();
                }
        }
}*/