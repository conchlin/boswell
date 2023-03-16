package script.binding

import client.MapleClient
import server.MapleItemInformationProvider

class ScriptItem(si: MapleItemInformationProvider.ScriptedItem, c: MapleClient) {

    var item = si
    var client = c

}