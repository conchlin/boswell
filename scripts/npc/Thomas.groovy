/*
    Npc Name: Thomas Swift            
    Npc ID: 9201022
    Location Name: Henesys
    Location ID: 100000000
            
    @Author: Connor
    @Created: 2023-04-09
*/

def currentMap = user.getMapId()
def location = currentMap == 100000000 ? "Amoria" : "Henesys"

def ret = npc.askYesNo("I can take you to the " + location +". Are you ready to go?")

if (ret == 1) {
    if (currentMap == 100000000) {
        user.changeMap(680000000, 0)
    } else {
        user.changeMap(100000000, 5)
    }
}
//empty no return


