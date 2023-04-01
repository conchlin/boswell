package script.binding

import client.MapleClient
import enums.UserEffectType
import network.packet.UserLocal
import server.MaplePortal

class ScriptPortal(p: MaplePortal, c: MapleClient) {

    val portal: MaplePortal = p
    val client: MapleClient = c

    /**
     * The portal will move the user to the specified field
     *
     * @param fieldId
     */
    fun fieldTransfer(fieldId: Int) {
        client.player.changeMap(fieldId, 0)
    }

    /**
     * display a specific user effect within the UI wz file
     *
     * @param pathway
     */
    fun showInfoEffect(pathway: String) {
        client.announce(UserLocal.onEffect(UserEffectType.SHOW_INFO.effect, pathway));
    }
}