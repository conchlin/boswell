/*
    This is an auto-generated portal script for Boswell!
    
    Portal Script Name: MD_drakeroom            
    Portal Name: MD00
    Portal Position: java.awt.Point[x=161,y=-359]
    Location Name: Cold Cradle
    Location ID: 105090311
            
    @Author: Connor
    @Created: 2023-07-03 
*/
    
int drakeDungeon = 105090320

if (user.getMapId() == 105090311) {
    if (user.getParty() != null) {
        if (user.getId() == user.getParty().getLeaderId()) {
            script.transferNewFieldInstance(drakeDungeon)
        } else {
            user.message("Please have your party leader enter, or leave your party.")
        }
    } else {
        script.transferNewFieldInstance(drakeDungeon)
    }
} else {
    // when trying to leave
    script.transferOutFieldInstance(user.getInstance())
}
    