package script.binding

import client.MapleClient
import server.MaplePortal

class ScriptPortal(p: MaplePortal, c: MapleClient) {

    val portal: MaplePortal = p
    val client: MapleClient = c

    fun message(msg: String?) {
        client.player.message(msg)
    }

}