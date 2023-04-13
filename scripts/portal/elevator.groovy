/*
    Portal Script Name: elevator
    Portal Name: in00
    Portal Position: java.awt.Point[x=-133,y=1963]
    Location Name: Helios Tower <99th Floor> and <2nd Floor>
    Location ID: 222020200/222020100

    Part of the instant travel system of Boswell
            
    @Author: Connor
    @Created: 2023-04-13 
    */

def location = user.getMapId() == 222020100 ? 222020200 : 222020100
user.changeMap(location, 0)
    