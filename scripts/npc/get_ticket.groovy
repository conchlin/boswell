/*
    This file handles a lot of the main travel NPCs and is part of the instant travel system
    that boswell has implemented.

    Most of the NPCs associated with the Orbis Station Entrance are handled by this script.
            
    @Author: Connor
    @Created: 2023-04-13
*/

static int getTravelLocation(int currentMap) {
    // map of all valid travel locations
    def travel = [
            200000111: 101000300, // orbis -> ellinia
            200000121: 220000100, // orbis -> ludi
            200000131: 240000110, // orbis -> leafre
            200000151: 260000100, // orbis -> ariant
            101000300: 200000100, // ellinia -> orbis
            220000110: 200000100, // ludi -> orbis
            240000110: 200000131, // leafre -> orbis
            260000100: 200000100  // ariant -> orbis
    ]

    return travel.get(currentMap)
}

def currentMap = user.getMapId()
def sel = script.askMenu("We are ready for takeoff! Do you want to travel to #b#m" + getTravelLocation(currentMap) + "##k? " +
        " For the low cost of 10,000 meso I can take you there! But please do hurry and decide before the ship leaves!" +
        "\r\n#L0##bI would like to travel to #m" + getTravelLocation(currentMap) +"##k \r\n#L1# #bI would like to ride the ship#k")

if (sel == 0) {
    if (user.getMeso() < 10000) {
        script.say("Hmm.. Are you sure that you have #b1000 Mesos#k? Check your Inventory and make sure you have enough. " +
                "You must pay the fee or I can't let you get on...")
    } else {
        user.gainMeso(-10000)
        user.changeMap(getTravelLocation(currentMap), 0)
    }

} else if (sel == 1) {
    // this option is needed only to gain access to the balrog on the orbis flight
    script.say("This feature is not complete")
}