// NPC: Yokora
// Map: 800040200

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    cm.openShopNPC(9110100);
    cm.dispose();
}
