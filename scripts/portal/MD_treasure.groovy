/*
    This is an auto-generated portal script for Boswell!
    
    Portal Script Name: MD_treasure            
    Portal Name: MD00
    Portal Position: java.awt.Point[x=234,y=-272]
    Location Name: Red-Nose Pirate Den 2
    Location ID: 251010402
            
    @Author: Connor
    @Created: 2023-07-03 
*/
    
int treasureDungeon = 251010410

if (user.getMapId() == 251010402) {
    if (user.getParty() != null) {
        if (user.getId() == user.getParty().getLeaderId()) {
            script.transferNewFieldInstance(treasureDungeon)
            script.appendFieldClock(60)
        } else {
            user.message("Please have your party leader enter, or leave your party.")
        }
    } else {
        script.transferNewFieldInstance(treasureDungeon)
        script.appendFieldClock(60)
    }
} else {
    // when trying to leave
    script.transferOutFieldInstance(user.getInstance())
}
    