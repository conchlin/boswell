package script

import java.util.*


class ScriptHistory(t: Int) {

    var type = 0
    var packet: ByteArray? = null
    var memory: ArrayList<Any>? = null
    var speakerTemplateID = 0
    var couponItemID = 0

    init {
        this.type = t
        memory = ArrayList()
    }
}