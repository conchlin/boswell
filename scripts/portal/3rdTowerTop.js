function enter(pi) {
    if (pi.hasItem(4032834)) {
        var spellBreaker3 = pi.getEventManager("3rdSpellBreaker");

        spellBreaker3.setProperty("player", pi.getPlayer().getName());
        spellBreaker3.startInstance(pi.getPlayer());

        pi.playPortalSound();
        pi.warp(211060601, 0);
    } else {
        pi.message("The tower door is locked.");
        return false;
    }
}