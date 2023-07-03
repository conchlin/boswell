/*
    This is an auto-generated portal script for Boswell!
    
    Portal Script Name: MD_high            
    Portal Name: MD00
    Portal Position: java.awt.Point[x=-160,y=638]
    Location Name: Fantasy Theme Park 3
    Location ID: 551030000
            
    @Author: Connor
    @Created: 2023-07-03 
*/
    
int highDungeon = 551030001

if (user.getMapId() == 551030000) {
    if (user.getParty() != null) {
        if (user.getId() == user.getParty().getLeaderId()) {
            script.transferNewFieldInstance(highDungeon)
        } else {
            user.message("Please have your party leader enter, or leave your party.")
        }
    } else {
        script.transferNewFieldInstance(highDungeon)
    }
} else {
    // when trying to leave
    script.transferOutFieldInstance(user.getInstance())
}
    