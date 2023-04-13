/*
    Npc Name: NLC Taxi
    Npc ID: 9201056
    Location Name: Haunted House
    Location ID: 682000000
            
    @Author: Connor
    @Created: 2023-04-12
*/

def currentMap = user.getMapId()
def destination = currentMap == 600000000 ? "the Haunted Mansion" : "New Leaf City"
def warpDestination = -1
def fee = 1500

def ret = npc.askYesNo("Would you like to go to #b" + destination + "#k? The fee is " + fee + " mesos.")

if (ret == 1) {
    warpDestination = destination == "New Leaf City" ? 600000000 : 682000000
    if (user.getMeso() < fee) {
        npc.say("Hey, what are you trying to pull on? You don't have enough mesos to pay the fee.")
    } else {
        user.gainMeso(-fee)
        user.changeMap(warpDestination, 0)
    }
} else {
    npc.say("Alright, see you next time.")
}