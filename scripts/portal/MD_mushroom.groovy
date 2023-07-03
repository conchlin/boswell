/*
    This is an auto-generated portal script for Boswell!
    
    Portal Script Name: MD_mushroom            
    Portal Name: MD00
    Portal Position: java.awt.Point[x=1287,y=968]
    Location Name: Ant Tunnel II
    Location ID: 105050100
            
    @Author: Connor
    @Created: 2023-07-03 
*/
    
int mushroomDungeon = 105050101

if (user.getMapId() == 105050100) {
    if (user.getParty() != null) {
        if (user.getId() == user.getParty().getLeaderId()) {
            script.transferNewFieldInstance(mushroomDungeon)
        } else {
            user.message("Please have your party leader enter, or leave your party.")
        }
    } else {
        script.transferNewFieldInstance(mushroomDungeon)
    }
} else {
    // when trying to leave
    script.transferOutFieldInstance(user.getInstance())
}
    