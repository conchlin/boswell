function enter(pi) {
    if (pi.hasItem(4032833)) {
        var spellBreaker2 = pi.getEventManager("2ndSpellBreaker");

        spellBreaker2.setProperty("player", pi.getPlayer().getName());
        spellBreaker2.startInstance(pi.getPlayer());

        pi.playPortalSound();
        pi.warp(211060401, 0);
    } else {
        pi.message("The tower door is locked.");
        return false;
    }
}