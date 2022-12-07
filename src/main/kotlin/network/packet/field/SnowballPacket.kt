package network.packet.field

import network.opcode.SendOpcode
import server.events.gm.MapleSnowball
import tools.data.output.MaplePacketLittleEndianWriter

class SnowballPacket {

    companion object Packet {

        /**
         * onSnowballState
         *
         * @param enterMap
         * @param state 0 = move, 1 = roll, 2 is down disappear, 3 is up disappear
         * @param ball0
         * @param ball1
         */
        fun onState(enterMap: Boolean, state: Int, ball0: MapleSnowball, ball1: MapleSnowball): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SnowBallState.value)
            if (enterMap) {
                mplew.skip(21)
            } else {
                mplew.write(state)
                mplew.writeInt(ball0.snowmanHP / 75)
                mplew.writeInt(ball1.snowmanHP / 75)
                mplew.writeShort(ball0.position)
                mplew.write(-1)
                mplew.writeShort(ball1.position)
                mplew.write(-1)
            }
            return mplew.packet
        }

        /**
         * onHit
         *
         * @param what
         * @param damage
         */
        fun onHit(what: Int, damage: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(7)
            mplew.writeShort(SendOpcode.SnowBallHit.value)
            mplew.write(what)
            mplew.writeInt(damage)

            return mplew.packet
        }

        /**
         * Sends a Snowball Message<br></br>
         *
         * Possible values for `message`:
         * <br></br> 1: ... Team's snowball has passed the stage 1.
         * <br></br> 2: ... Team's snowball has passed the stage 2.
         * <br></br> 3: ... Team's snowball has passed the stage 3.
         * <br></br> 4: ... Team is attacking the snowman, stopping the progress.
         * <br></br> 5: ... Team is moving again.
         * <br></br>
         *
         * @param 0 is down, 1 is up
         * @param message see above
         */
        fun onMessage(team: Int, message: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(7)
            mplew.writeShort(SendOpcode.SnowBallMsg.value)
            mplew.write(team)
            mplew.writeInt(message)

            return mplew.packet
        }

        fun onTouch(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(2)
            mplew.writeShort(SendOpcode.SnowBallTouch.value)
            return mplew.packet
        }
    }
}