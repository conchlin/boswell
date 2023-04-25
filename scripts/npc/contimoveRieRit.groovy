/*
    Npc Name: Puro
    Npc ID: 1200003
    Location Name: Dangerous Forest
    Location ID: 140020300
            
    @Author: Connor
    @Created: 2023-04-20 
*/

def ret = npc.askYesNo("Are you thinking about leaving Rien and heading back? If you board this ship, I can " +
        "take you from #bLith Harbor#k to #bRien#k and back. Would you like to go to #bVinctoria Island#k?" +
        "\r\n\r\n The trip costs #b1000 Mesos#k")

if (ret == 1) {
    if (user.getMeso() < 1000) {
        npc.say("Hmm.. Are you sure that you have #b1000 Mesos#k? Check your Inventory and make sure you have " +
                "enough. You must pay the fee or I can't let you get on...")
    } else {
        user.gainMeso(-1000)
        user.changeMap(104000000, 26)
    }
} else {
    npc.say("If you're not interested, then oh well...")
}