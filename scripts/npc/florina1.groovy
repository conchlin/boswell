/*
    Npc Name: Pison            
    Npc ID: 1081001
    Location Name: Florina Beach
    Location ID: 110000000
            
    @Author: Connor
    @Created: 2023-04-09
*/

def returnMap = user.peekSavedLocation("FLORINA")

script.sayNext("So you want to leave #b#m110000000##k? If you want, I can take you back to #b#m" + returnMap + "##k.")

def ret = script.askYesNo("Are you sure you want to return to #b#m" + returnMap + "##k? Alright, we'll have to get " +
        "going fast. Do you want to head back to #m" + returnMap + "# now?")
if (ret == 1) {
    user.getSavedLocation("FLORINA")
    user.changeMap(returnMap)
} else {
    script.say("You must have some business to take care of here. It's not a bad idea to take some rest at #m"+ returnMap +
            "# Look at me; I love it here so much that I wound up living here. Hahaha anyway, talk to me when you feel like going back.")
}