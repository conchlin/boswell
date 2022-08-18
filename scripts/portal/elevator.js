function enter(pi) {
    pi.playPortalSound(); 
    pi.warp(pi.getMapId() == 222020100 ? 222020200 : 222020100, 0);
    return true;
}