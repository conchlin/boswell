package network.crypto

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

class ClientEncryption(private var iv: ByteArray, private var mapleVersion: Short) {
    private var cipher: Cipher? = null

    init {
        // initialize the cipher object using the aesKey reverse engineered from the client.
        // aesKey changes depending on version
        cipher = Cipher.getInstance("AES")
        cipher?.init(Cipher.ENCRYPT_MODE, aesKey) ?: throw IllegalStateException("Cipher initialization failed")
        mapleVersion = (mapleVersion.toInt() shr 8 and 0xFF or (mapleVersion.toInt() shl 8 and 0xFF00)).toShort()
    }

    @Synchronized
    fun aesCrypt(data: ByteArray): ByteArray {
        //Maple's OFB is done in piecemeal every 1460 bytes (I'm guessing it has
        //to deal with maximum segment size). First piece is only 1456 bytes
        //because the header, although not encrypted, adds 4 blocks to the first
        //segment.
        var remaining = data.size
        var pieceSize = 1456
        var offset = 0
        while (remaining > 0) {
            val myIv = multiplyBytes(iv, 4, 4)
            if (remaining < pieceSize) {
                pieceSize = remaining
            }
            //loops through each 1460 byte piece (with first piece only 1456 bytes)
            for (x in offset until offset + pieceSize) {
                if ((x - offset) % myIv.size == 0) {
                    val newIv = cipher!!.doFinal(myIv)
                    for (j in myIv.indices) {
                        myIv[j] = newIv[j]
                    }
                }
                data[x] = (data[x].toInt() xor myIv[(x - offset) % myIv.size].toInt()).toByte()
            }
            offset += pieceSize
            remaining -= pieceSize
            pieceSize = 1460
        }
        iv = shiftIv(iv)
        return data
    }

    /**
     * Generates a packet header for the specified packet length.
     */
    fun getPacketHeader(length: Int): ByteArray {
        var iiv = iv[3].toInt() and 0xFF
        iiv = iiv or (iv[2].toInt() shl 8 and 0xFF00)
        iiv = iiv xor mapleVersion.toInt()
        val mlength = length shl 8 and 0xFF00 or (length ushr 8)
        val xoredIv = iiv xor mlength
        val ret = ByteArray(4)
        ret[0] = (iiv ushr 8 and 0xFF).toByte()
        ret[1] = (iiv and 0xFF).toByte()
        ret[2] = (xoredIv ushr 8 and 0xFF).toByte()
        ret[3] = (xoredIv and 0xFF).toByte()
        return ret
    }

    private fun checkPacket(packet: ByteArray): Boolean {
        return packet[0].toInt() xor iv[2].toInt() and 0xFF == mapleVersion.toInt() shr 8 and 0xFF 
        && packet[1].toInt() xor iv[3].toInt() and 0xFF == mapleVersion.toInt() and 0xFF
    }

    /**
     * Checks the validity of a packet using its header.
     */
    fun checkPacket(packetHeader: Int): Boolean {
        val packetHeaderBuf = ByteArray(2)
        packetHeaderBuf[0] = (packetHeader shr 24 and 0xFF).toByte()
        packetHeaderBuf[1] = (packetHeader shr 16 and 0xFF).toByte()
        return checkPacket(packetHeaderBuf)
    }

    companion object {
        private val aesKey = SecretKeySpec(
            byteArrayOf(
                0x13,
                0x00,
                0x00,
                0x00,
                0x08,
                0x00,
                0x00,
                0x00,
                0x06,
                0x00,
                0x00,
                0x00,
                0xB4.toByte(),
                0x00,
                0x00,
                0x00,
                0x1B,
                0x00,
                0x00,
                0x00,
                0x0F,
                0x00,
                0x00,
                0x00,
                0x33,
                0x00,
                0x00,
                0x00,
                0x52,
                0x00,
                0x00,
                0x00
            ), "AES"
        )
        private val ivShiftKey = byteArrayOf(
            0xEC.toByte(),
            0x3F.toByte(),
            0x77.toByte(),
            0xA4.toByte(),
            0x45.toByte(),
            0xD0.toByte(),
            0x71.toByte(),
            0xBF.toByte(),
            0xB7.toByte(),
            0x98.toByte(),
            0x20.toByte(),
            0xFC.toByte(),
            0x4B.toByte(),
            0xE9.toByte(),
            0xB3.toByte(),
            0xE1.toByte(),
            0x5C.toByte(),
            0x22.toByte(),
            0xF7.toByte(),
            0x0C.toByte(),
            0x44.toByte(),
            0x1B.toByte(),
            0x81.toByte(),
            0xBD.toByte(),
            0x63.toByte(),
            0x8D.toByte(),
            0xD4.toByte(),
            0xC3.toByte(),
            0xF2.toByte(),
            0x10.toByte(),
            0x19.toByte(),
            0xE0.toByte(),
            0xFB.toByte(),
            0xA1.toByte(),
            0x6E.toByte(),
            0x66.toByte(),
            0xEA.toByte(),
            0xAE.toByte(),
            0xD6.toByte(),
            0xCE.toByte(),
            0x06.toByte(),
            0x18.toByte(),
            0x4E.toByte(),
            0xEB.toByte(),
            0x78.toByte(),
            0x95.toByte(),
            0xDB.toByte(),
            0xBA.toByte(),
            0xB6.toByte(),
            0x42.toByte(),
            0x7A.toByte(),
            0x2A.toByte(),
            0x83.toByte(),
            0x0B.toByte(),
            0x54.toByte(),
            0x67.toByte(),
            0x6D.toByte(),
            0xE8.toByte(),
            0x65.toByte(),
            0xE7.toByte(),
            0x2F.toByte(),
            0x07.toByte(),
            0xF3.toByte(),
            0xAA.toByte(),
            0x27.toByte(),
            0x7B.toByte(),
            0x85.toByte(),
            0xB0.toByte(),
            0x26.toByte(),
            0xFD.toByte(),
            0x8B.toByte(),
            0xA9.toByte(),
            0xFA.toByte(),
            0xBE.toByte(),
            0xA8.toByte(),
            0xD7.toByte(),
            0xCB.toByte(),
            0xCC.toByte(),
            0x92.toByte(),
            0xDA.toByte(),
            0xF9.toByte(),
            0x93.toByte(),
            0x60.toByte(),
            0x2D.toByte(),
            0xDD.toByte(),
            0xD2.toByte(),
            0xA2.toByte(),
            0x9B.toByte(),
            0x39.toByte(),
            0x5F.toByte(),
            0x82.toByte(),
            0x21.toByte(),
            0x4C.toByte(),
            0x69.toByte(),
            0xF8.toByte(),
            0x31.toByte(),
            0x87.toByte(),
            0xEE.toByte(),
            0x8E.toByte(),
            0xAD.toByte(),
            0x8C.toByte(),
            0x6A.toByte(),
            0xBC.toByte(),
            0xB5.toByte(),
            0x6B.toByte(),
            0x59.toByte(),
            0x13.toByte(),
            0xF1.toByte(),
            0x04.toByte(),
            0x00.toByte(),
            0xF6.toByte(),
            0x5A.toByte(),
            0x35.toByte(),
            0x79.toByte(),
            0x48.toByte(),
            0x8F.toByte(),
            0x15.toByte(),
            0xCD.toByte(),
            0x97.toByte(),
            0x57.toByte(),
            0x12.toByte(),
            0x3E.toByte(),
            0x37.toByte(),
            0xFF.toByte(),
            0x9D.toByte(),
            0x4F.toByte(),
            0x51.toByte(),
            0xF5.toByte(),
            0xA3.toByte(),
            0x70.toByte(),
            0xBB.toByte(),
            0x14.toByte(),
            0x75.toByte(),
            0xC2.toByte(),
            0xB8.toByte(),
            0x72.toByte(),
            0xC0.toByte(),
            0xED.toByte(),
            0x7D.toByte(),
            0x68.toByte(),
            0xC9.toByte(),
            0x2E.toByte(),
            0x0D.toByte(),
            0x62.toByte(),
            0x46.toByte(),
            0x17.toByte(),
            0x11.toByte(),
            0x4D.toByte(),
            0x6C.toByte(),
            0xC4.toByte(),
            0x7E.toByte(),
            0x53.toByte(),
            0xC1.toByte(),
            0x25.toByte(),
            0xC7.toByte(),
            0x9A.toByte(),
            0x1C.toByte(),
            0x88.toByte(),
            0x58.toByte(),
            0x2C.toByte(),
            0x89.toByte(),
            0xDC.toByte(),
            0x02.toByte(),
            0x64.toByte(),
            0x40.toByte(),
            0x01.toByte(),
            0x5D.toByte(),
            0x38.toByte(),
            0xA5.toByte(),
            0xE2.toByte(),
            0xAF.toByte(),
            0x55.toByte(),
            0xD5.toByte(),
            0xEF.toByte(),
            0x1A.toByte(),
            0x7C.toByte(),
            0xA7.toByte(),
            0x5B.toByte(),
            0xA6.toByte(),
            0x6F.toByte(),
            0x86.toByte(),
            0x9F.toByte(),
            0x73.toByte(),
            0xE6.toByte(),
            0x0A.toByte(),
            0xDE.toByte(),
            0x2B.toByte(),
            0x99.toByte(),
            0x4A.toByte(),
            0x47.toByte(),
            0x9C.toByte(),
            0xDF.toByte(),
            0x09.toByte(),
            0x76.toByte(),
            0x9E.toByte(),
            0x30.toByte(),
            0x0E.toByte(),
            0xE4.toByte(),
            0xB2.toByte(),
            0x94.toByte(),
            0xA0.toByte(),
            0x3B.toByte(),
            0x34.toByte(),
            0x1D.toByte(),
            0x28.toByte(),
            0x0F.toByte(),
            0x36.toByte(),
            0xE3.toByte(),
            0x23.toByte(),
            0xB4.toByte(),
            0x03.toByte(),
            0xD8.toByte(),
            0x90.toByte(),
            0xC8.toByte(),
            0x3C.toByte(),
            0xFE.toByte(),
            0x5E.toByte(),
            0x32.toByte(),
            0x24.toByte(),
            0x50.toByte(),
            0x1F.toByte(),
            0x3A.toByte(),
            0x43.toByte(),
            0x8A.toByte(),
            0x96.toByte(),
            0x41.toByte(),
            0x74.toByte(),
            0xAC.toByte(),
            0x52.toByte(),
            0x33.toByte(),
            0xF0.toByte(),
            0xD9.toByte(),
            0x29.toByte(),
            0x80.toByte(),
            0xB1.toByte(),
            0x16.toByte(),
            0xD3.toByte(),
            0xAB.toByte(),
            0x91.toByte(),
            0xB9.toByte(),
            0x84.toByte(),
            0x7F.toByte(),
            0x61.toByte(),
            0x1E.toByte(),
            0xCF.toByte(),
            0xC5.toByte(),
            0xD1.toByte(),
            0x56.toByte(),
            0x3D.toByte(),
            0xCA.toByte(),
            0xF4.toByte(),
            0x05.toByte(),
            0xC6.toByte(),
            0xE5.toByte(),
            0x08.toByte(),
            0x49.toByte()
        )

        /**
         * Multiplies the input ByteArray by repeating its elements mul 
         * times to create a new ByteArray.
         */
        private fun multiplyBytes(`in`: ByteArray, count: Int, mul: Int): ByteArray {
            val ret = ByteArray(count * mul)
            for (x in 0 until count * mul) {
                ret[x] = `in`[x % count]
            }
            return ret
        }

        /**
         * Extracts the packet length from the given packet header.
         */
        fun getPacketLength(packetHeader: Int): Int {
            var packetLength = packetHeader ushr 16 xor (packetHeader and 0xFFFF)
            packetLength = packetLength shl 8 and 0xFF00 or (packetLength ushr 8 and 0xFF)
            return packetLength
        }

        /**
         * Shifts and transforms the input IV using the maple's 
         * custom protocol as defined in the client
         * 
         * @param oldIv The current IV to be shifted and transformed.
         * @return A new ByteArray containing the shifted and transformed IV.
         */
        fun shiftIv(oldIv: ByteArray): ByteArray {
            val newIv = byteArrayOf(0xf2.toByte(), 0x53, 0x50.toByte(), 0xc6.toByte())
            for (element in oldIv) {
                var temp1 = newIv[1]
                var temp3: Byte = ivShiftKey[temp1.toInt() and 0xFF]
                temp3 = (temp3 - element).toByte()
                newIv[0] = (newIv[0] + temp3).toByte()
                temp3 = newIv[2]
                temp3 = temp3 xor ivShiftKey[element.toInt() and 0xFF]
                temp1 = (temp1 - temp3).toByte()
                newIv[1] = temp1
                temp1 = newIv[3]
                temp3 = temp1
                temp1 = (temp1 - newIv[0]).toByte()
                temp3 = ivShiftKey[temp3.toInt() and 0xFF]
                temp3 = (temp3 + element).toByte()
                temp3 = temp3 xor newIv[2]
                newIv[2] = temp3
                temp1 = (temp1 + ivShiftKey[element.toInt() and 0xFF]).toByte()
                newIv[3] = temp1

                //essentially reverses the byte order of newIv, rotates all bits
                //3 to the left, then reverses the byte order again
                temp1 = ((newIv[3].toInt() and 0xFF ushr 5)).toByte() //the "carry"
                newIv[3] = ((newIv[3].toInt() shl 3 or (newIv[2].toInt() and 0xFF ushr 5))).toByte()
                newIv[2] = ((newIv[2].toInt() shl 3 or (newIv[1].toInt() and 0xFF ushr 5))).toByte()
                newIv[1] = ((newIv[1].toInt() shl 3 or (newIv[0].toInt() and 0xFF ushr 5))).toByte()
                newIv[0] = ((newIv[0].toInt() shl 3 or temp1.toInt())).toByte()
            }
            return newIv
        }
    }
}