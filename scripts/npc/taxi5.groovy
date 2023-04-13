/*
    Npc Name: Nautilus' Mid-Sized Taxi
    Npc ID: 1092014
    Location Name: Nautilus Harbor
    Location ID: 120000000
            
    @Author: Connor
    @Created: 2023-04-12
*/

def beginner = false
def selStr = ""
def selectedMap = -1
def maps = [104000000, 102000000, 100000000, 101000000, 103000000]
def cost = [1000, 1000, 1000, 800, 1000]

if (user.isBeginnerJob()) {
    beginner = true
}

npc.sayNext("Hello, I drive the Regular Cab. If you want to go from town to town safely and fast, " +
        "then ride our cab. We'll gladly take you to your destination with an affordable price.")

if (beginner) {
    selStr += "We have a special 90% discount for beginners."
}
selStr += "Choose your destination, for fees will change from place to place.#b"

for (var i = 0; i < maps.size(); i++) {
    selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + (beginner ? cost[i] / 10 : cost[i]) + " mesos)#l"
}

def sel = npc.askMenu(selStr)
def ret = npc.askYesNo("You don't have anything else to do here, huh? Do you really want to go to #b#m" +
        maps[sel] + "##k? It'll cost you #b" + (beginner ? cost[sel] / 10 : cost[sel]) + " mesos#k.")
selectedMap = sel
if (ret == 1) { // yes
    if (user.getMeso() < cost[selectedMap]) {
        npc.say("You don't have enough mesos. Sorry to say this, " +
                "but without them, you won't be able to ride the cab.")
    } else {
        user.gainMeso(cost[selectedMap])
        user.changeMap(maps[selectedMap], 0)
    }
}