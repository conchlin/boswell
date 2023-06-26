package game.field

import client.MapleCharacter
import net.server.world.MapleParty
import server.maps.MapleMap


class FieldInstance(private var user: MapleCharacter, private var fieldId: Int) {

    /**
     * Field instances provide the ability for there to be multiple
     * occurrences of the same field/event running at the same time
     * on any given channel. These instances can either be for an
     * individual player or party.
     */

    private var party: MapleParty? = user.party
    private var forcedReturnField = 0
    private var chars: ArrayList<MapleCharacter> = ArrayList()
    private var fields: MutableMap<Int, MapleMap> = mutableMapOf()

    /**
     * Create the field instance. Compiles list of valid instance
     * members, and warps them to field.
     */
    fun init() {
        val map = user.client?.channelServer?.mapFactory?.makeDisposableMap(fieldId)

        if (map != null) {
            forcedReturnField = map.forcedReturnId
            fields[map.id] = map
        }

        if (party != null) {
            chars = ArrayList(user.partyMembersOnSameMap)
            party!!.instance = this
        } else {
            chars = ArrayList()
            chars.add(user)
        }

        for (users in chars) {
            user.instance = this
            user.changeMap(fieldId)
        }
    }

    fun getInstanceFields(fieldId: Int): MapleMap? {
        return if (fields.containsKey(fieldId)) {
            fields[fieldId]
        } else {
            null
        }
    }
}