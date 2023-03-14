package script.binding

import client.MapleClient
import server.maps.MapleReactor

class ScriptReactor(r: MapleReactor, c: MapleClient) {

    val reactor: MapleReactor = r
    val client: MapleClient = c

}
