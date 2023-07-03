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
    private var clock: Clock? = null

    /**
     * Create the field instance. Compiles list of valid instance
     * members, and warps them to field.
     *
     * This will also search for the timeLimit map node and add a
     * clock to the map based on that number. If the timeLimit node
     * does not exist it will need to be manually added.
     */
    fun init() {
        val map = user.client?.channelServer?.mapFactory?.makeDisposableMap(fieldId)
        val timeLimit = map?.timeLimit

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

        if (timeLimit != null) {
            map.setClock(true)
            clock = fields[map.id]?.let { Clock(it, timeLimit) }!!
            clock!!.create()
        }
    }

    fun clear() {
        if (party != null) {
            party!!.instance = null
        }

        for (user in chars) {
            user.instance = null
            user.changeMap(forcedReturnField)
        }

        fields.clear()
        chars.clear()
        clock?.destroy()
    }

    fun getInstanceFields(fieldId: Int): MapleMap? {
        return if (fields.containsKey(fieldId)) {
            fields[fieldId]
        } else {
            null
        }
    }
}