/*
    This is an auto-generated portal script for Boswell!
    
    Portal Script Name: MD_sand            
    Portal Name: MD00
    Portal Position: java.awt.Point[x=-180,y=-178]
    Location Name: Sahel 2
    Location ID: 260020600
            
    @Author: Connor
    @Created: 2023-07-03 
*/
    
int sandDungeon = 260020630

if (user.getMapId() == 260020600) {
    if (user.getParty() != null) {
        if (user.getId() == user.getParty().getLeaderId()) {
            script.transferNewFieldInstance(sandDungeon)
        } else {
            user.message("Please have your party leader enter, or leave your party.")
        }
    } else {
        script.transferNewFieldInstance(sandDungeon)
    }
} else {
    // when trying to leave
    script.transferOutFieldInstance(user.getInstance())
}
    