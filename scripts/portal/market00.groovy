/*
    This is an auto-generated portal script for Boswell!
    
    Portal Script Name: market00            
    Portal Name: out00
    Portal Position: java.awt.Point[x=-538,y=30]
    Location Name: Free Market Entrance
    Location ID: 910000000
            
    @Author: shalevbs
    @Created: 2023-05-09 
    */
    returnMap = user.getSavedLocation("FREE_MARKET");
    if (returnMap < 0) {
        returnMap = 100000000; // to fix people who entered the fm trough an unconventional way
    }
    var target = map_factory.getMap(returnMap);
    var portal;

    if (returnMap == 230000000) { // aquaroad has a different fm portal
        portal = target.getPortal("market01");
    } else {
        portal = target.getPortal("market00");
    }
    if (portal == null) {
        portal = target.getPortal(0);
    }
    if (user.getMapId() != target) {
        user.changeMap(target, portal);
    }
    