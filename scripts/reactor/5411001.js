/**
 * Spawns Krexel
 * 
 * @author Saffron
 * @map 541020800 - Ruins of Krexel II
 */

function act(){
    var map = rm.getReactor().getMap();
    if (map.countMonsters() == 0) { // idk if this would ever be a problem but...
        rm.spawnMonster(9420521);
        rm.mapMessage(5, "Krexel has been awoken.");
    }
}