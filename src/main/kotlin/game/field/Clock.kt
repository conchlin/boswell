package game.field


import game.EventScheduler
import network.packet.field.CField
import server.maps.MapleMap

class Clock(private val field: MapleMap, private val seconds: Int) {

    private val timeInMillis = (seconds * 1000) + System.currentTimeMillis()

    fun create() {
        field.setClock(true)
        field.broadcastMessage(CField.onClock(true, seconds))
        field.fieldClock = this
        EventScheduler.schedule(this::destroy, (seconds * 1000).toLong())
    }

    fun show() {
        field.broadcastMessage(CField.onClock(false, getRemainingTime()))
    }

    fun destroy() {
        if (field.hasClock()) {
            field.broadcastMessage(CField.onDestroyClock())
        }
    }

    private fun getRemainingTime(): Int {
        return ((timeInMillis - System.currentTimeMillis()) / 1000).toInt()
    }
}