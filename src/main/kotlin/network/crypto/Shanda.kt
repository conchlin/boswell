package network.encryption

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.xor

class Shanda {

    /**
     *  Cryptography originally from a company called Shanda. Was used in
     *  Global MapleStory up until version 149.2
     */

    companion object {

        fun encrypt(data: ByteArray): ByteArray {
            for (j in 0 until 6) {
                var remember: Byte = 0
                var dataLength: Byte = (data.size and 0xFF).toByte()
                if (j % 2 == 0) {
                    for (i in data.indices) {
                        var cur: Byte = data[i]
                        cur = rollLeft(cur, 3)
                        cur = (cur + dataLength).toByte()
                        cur = cur xor remember
                        remember = cur
                        cur = rollRight(cur, dataLength.toInt() and 0xFF)
                        cur = ((cur.inv() and 0xFF.toByte()))
                        cur = (cur + 0x48).toByte()
                        dataLength--
                        data[i] = cur
                    }
                } else {
                    for (i in data.size - 1 downTo 0) {
                        var cur: Byte = data[i]
                        cur = rollLeft(cur, 4)
                        cur = (cur + dataLength).toByte()
                        cur = cur xor remember
                        remember = cur
                        cur = cur xor 0x13
                        cur = rollRight(cur, 3)
                        dataLength--
                        data[i] = cur
                    }
                }
            }
            return data
        }

        fun decrypt(data: ByteArray): ByteArray {
            var nextRemember: Byte
            for (j in 1..6) {
                var remember: Byte = 0
                var dataLength: Byte = (data.size and 0xFF).toByte()
        
                if (j % 2 == 0) {
                    for (i in data.indices) {
                        var cur: Byte = data[i]
                        cur = (cur - 0x48).toByte()
                        cur = (cur.inv() and 0xFF.toByte())
                        cur = rollLeft(cur, dataLength.toInt() and 0xFF)
                        nextRemember = cur
                        cur = cur xor remember
                        remember = nextRemember
                        cur = (cur - dataLength).toByte()
                        cur = rollRight(cur, 3)
                        data[i] = cur
                        dataLength--
                    }
                } else {
                    for (i in data.size - 1 downTo 0) {
                        var cur: Byte = data[i]
                        cur = rollLeft(cur, 3)
                        cur = cur xor 0x13
                        nextRemember = cur
                        cur = cur xor remember
                        remember = nextRemember
                        cur = (cur - dataLength).toByte()
                        cur = rollRight(cur, 4)
                        data[i] = cur
                        dataLength--
                    }
                }
            }
            return data
        }

        private fun rollLeft(input: Byte, count: Int): Byte {
            return (((input.toInt() and 0xFF) shl (count % 8) and 0xFF) or ((input.toInt() and 0xFF) shl (count % 8) shr 8)).toByte()
        }

        private fun rollRight(input: Byte, count: Int): Byte {
            var tmp = (input.toInt() and 0xFF)
            tmp = (tmp shl 8) ushr (count % 8)
            return ((tmp and 0xFF) or (tmp ushr 8)).toByte()
        }
    }
}