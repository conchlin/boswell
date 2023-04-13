/*
    Npc Name: Bell
    Npc ID: 9201057
    Location Name: NLC Subway Station
    Location ID: 600010001
            
    @Author: Connor
    @Created: 2023-04-12
    */

def currentMap = user.getMapId()
def destination = currentMap == 103000100 ? "New Leaf City" : "Kerning City"
def warpDestination = -1


def ret = npc.askYesNo("This subway is ready for takeoff, next stop #b" + destination + "#k! " +
        "Are you done with everything here, would you like to go to #b" + destination + "#k?\r\n\r\n The trip costs #b10000 Mesos#k")
if (ret == 1) {
    warpDestination = destination == "New Leaf City" ? 600010001 : 103000100
    if (user.getMeso() < 10000) {
        npc.say("Hmm.. Are you sure that you have #b10000 Mesos#k? Check your Inventory and make sure you have enough. " +
                "You must pay the fee or I can't let you aboard.")
    } else {
        user.gainMeso(-10000)
        user.changeMap(warpDestination, 0)
    }
}