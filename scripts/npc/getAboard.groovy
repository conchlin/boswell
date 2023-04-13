/*
    Npc Name: Isa the Station Guide
    Npc ID: 2012006
    Location Name: Orbis Station Entrance
    Location ID: 200000100
            
    @Author: Connor
    @Created: 2023-04-12 
*/

def destinations = ["Ellinia", "Ludibrium", "Leafre", "Mu Lung", "Ariant", "Ereve"]
def boatType = ["the ship", "the train", "the bird", "Hak", "Genie", "the ship"]
def message = "Orbis Station has lots of platforms available to choose from. You need to choose the one " +
        "that'll take you to the destination of your choice. Which platform will you take?\\r\\n"

for (var i = 0; i < destinations.size(); i++) {
    message += "\r\n#L" + i + "##bThe platform to " + boatType[i] + " that heads to " + destinations[i] + ".#l";
}

def sel = npc.askMenu(message)
npc.sayNext("Ok #h #, I will send you to the platform for #b#m" + (200000110 + (sel * 10)) + "##k.")
user.changeMap(200000110 + (sel * 10), "west00")

