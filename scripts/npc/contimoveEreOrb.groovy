/*
    Npc Name: Kiru
    Npc ID: 1100004
    Location Name: Sky Ferry 
    Location ID: 130000210
            
    @Author: Connor
    @Created: 2023-04-20 
*/

def ret = script.askYesNo("Hmm... The winds seem favorable. Are you thinking of leaving #bEreve#k and going elsewhere? " +
        "This ferry sails to the #bOssyria Continent#k! I hope you've taken care of everything you needed to in " +
        "#bEreve#k. What do you say, would you like to go to #bOrbis#k?\r\n\r\n The trip costs #b1000 Mesos#k")

if (ret == 1) {
    if (user.getMeso() < 1000) {
        script.say("Hmm.. Are you sure that you have #b1000 Mesos#k? Check your Inventory and make sure you have " +
                "enough. You must pay the fee or I can't let you get on...")
    } else {
        user.gainMeso(-1000)
        user.changeMap(200000161, 1)
    }
} else {
    script.say("If you're not interested, then oh well...")
}