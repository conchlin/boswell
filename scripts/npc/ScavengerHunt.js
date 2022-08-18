/**
 * Scavenger Hunt Exchange
 * Accessed through Nina in the FM
 * 
 * @author Saffron
 */

var options = ["How do I participate?", "What is today's category?",  "I'd like to turn in my scavenger pieces!"];
var pieceA = 4006994;
var pieceB = 4006995;
var pieceC = 4006996;
var pieceD = 4006997;
var pieceE = 4006998;
var PIECE_COUNT = 200;
var TROPHY = 4008002;
var TROPHY_COUNT = 5;
var status;

function handleDescription(version) {

    function descriptionString(piece) {
        cm.sendOk("It's quite simple really, every day I provide the players with a challenge. \r\n"
            + "I've scattered #i" + piece + "# scavenger pieces throughout the maple world. If you can bring me 200 pieces "
            + "before the end of each day I'll give you a reward!");
    }

    if (version === 0) {
        descriptionString(pieceA)
    } else if (version === 1) {
        descriptionString(pieceB);
    } else if (version === 2) {
        descriptionString(pieceC);
    } else if (version === 3) {
        descriptionString(pieceD);
    } else if (version === 4) {
        descriptionString(pieceE);
    }
}

function handleHint(version) {
    if (version === 0) { // TODO: hints for each version
        cm.sendOk("Today's category is: #r#eField Work#n#k\r\n\r\n" +
            "#eHint:#n A true adventurer conducts their research out in the field! It is the best place to learn about your "
            + "surroundings. \r\n\r\nToday is all about learning how to be a better adventurer with some field work! "
            + "Today's scavenger hunt mobs would definitely agree!");
    } else if (version === 1) {
        cm.sendOk("Today's category is: #r#eField Work#n#k\r\n\r\n" +
            "#eHint:#n A true adventurer conducts their research out in the field! It is the best place to learn about your "
            + "surroundings. \r\n\r\nToday is all about learning how to be a better adventurer with some field work! "
            + "Today's scavenger hunt mobs would definitely agree!");
    } else if (version === 2) {
        cm.sendOk("Today's category is: #r#eField Work#n#k\r\n\r\n" +
            "#eHint:#n A true adventurer conducts their research out in the field! It is the best place to learn about your "
            + "surroundings. \r\n\r\nToday is all about learning how to be a better adventurer with some field work! "
            + "Today's scavenger hunt mobs would definitely agree!");
    } else if (version === 3) {
        cm.sendOk("Today's category is: #r#eField Work#n#k\r\n\r\n" +
            "#eHint:#n A true adventurer conducts their research out in the field! It is the best place to learn about your "
            + "surroundings. \r\n\r\nToday is all about learning how to be a better adventurer with some field work! "
            + "Today's scavenger hunt mobs would definitely agree!");
    } else if (version === 4) {
        cm.sendOk("Today's category is: #r#eField Work#n#k\r\n\r\n" +
            "#eHint:#n A true adventurer conducts their research out in the field! It is the best place to learn about your "
            + "surroundings. \r\n\r\nToday is all about learning how to be a better adventurer with some field work! "
            + "Today's scavenger hunt mobs would definitely agree!");
    }

}

function handleReward(version) {

    function handleRewardHelper(piece) {
        if (cm.canHold(2430000, 1) && cm.canHold(TROPHY, TROPHY_COUNT)) {
            cm.gainItem(2430000, 1);
            cm.gainItem(TROPHY, TROPHY_COUNT);
            cm.gainItem(piece, -PIECE_COUNT);
            cm.setDailyScavenger(true);
            print(cm.getPlayer() + " has completed their daily scavenger hunt");
        } else {
            cm.sendOk("Please free up some space in your USE and ETC slot");
            cm.dispose();
        }
    }

    if (version === 0 && cm.haveItem(pieceA, PIECE_COUNT)) {
        handleRewardHelper(pieceA);
    } else if (version === 1 && cm.haveItem(pieceB, PIECE_COUNT)) {
        handleRewardHelper(pieceB);
    } else if (version === 2 && cm.haveItem(pieceC, PIECE_COUNT)) {
        handleRewardHelper(pieceC);
    } else if (version === 3 && cm.haveItem(pieceD, PIECE_COUNT)) {
        handleRewardHelper(pieceD);
    } else if (version === 4 && cm.haveItem(pieceE, PIECE_COUNT)) {
        handleRewardHelper(pieceE);
    } else {
        cm.sendOk("Just as I thought, you do not have all the pieces! Only a true adventurer would be able to complete a task like this.");
    }
}

function start() {
    status = -2
    action(1, 0, 0);
}

function action(mode, type, selection) {
                                
    if (mode === -1) {
        cm.dispose();
    } else {
        if (mode === 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode === 1)
            status++;
        else
            status--;
                                        
        if(status === -1) {
            var text = "Nobody knows this world better than I, John Barricade, the Scavenger King! What?! " 
                + "You think you know more about Maple? Hmmm.. let's put that to the test.\r\n";
            for(var i = 0; i < options.length; i++) {
                text += "#L" + i + "##b" + options[i] + "#k#l\r\n";
            }
            
            if (cm.getDailyScavenger() === true) {
                cm.sendOk("Impressive! It seems are are quite the adventurer. You should come back tomorrow for another Scavenger Hunt!");
                cm.dispose();
            } else {
                cm.sendSimple(text);
            }

        } else if (status === 0) {
            var version = parseInt(cm.getPlayer().getId()) % 5;

            if (selection === 0) {
                handleDescription(version);

            } else if (selection === 1) {
                handleHint(version);

            } else if (selection === 2) {
                handleReward(version);
            }
            cm.dispose();
        } 
    }
}