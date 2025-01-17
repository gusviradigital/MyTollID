package com.gdp.mytollid.util.card

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.NfcA
import java.io.IOException

class DefaultReader : CardReader {
    override fun readCardNumber(tag: Tag): String? {
        // Coba baca dengan berbagai metode
        val isoDep = IsoDep.get(tag)
        val mifareClassic = MifareClassic.get(tag)
        val nfcA = NfcA.get(tag)

        return when {
            isoDep != null -> readWithIsoDep(isoDep)
            mifareClassic != null -> readWithMifare(mifareClassic)
            nfcA != null -> readWithNfcA(nfcA)
            else -> null
        }
    }

    override fun readBalance(tag: Tag): Double? {
        // Coba baca dengan berbagai metode
        val isoDep = IsoDep.get(tag)
        val mifareClassic = MifareClassic.get(tag)
        val nfcA = NfcA.get(tag)

        return when {
            isoDep != null -> readBalanceWithIsoDep(isoDep)
            mifareClassic != null -> readBalanceWithMifare(mifareClassic)
            nfcA != null -> readBalanceWithNfcA(nfcA)
            else -> null
        }
    }

    override fun isCardSupported(tag: Tag): Boolean {
        return IsoDep.get(tag) != null || 
               MifareClassic.get(tag) != null ||
               NfcA.get(tag) != null
    }

    override fun getCardType(): CardType = CardType.UNKNOWN

    private fun readWithIsoDep(isoDep: IsoDep): String? {
        try {
            isoDep.connect()
            isoDep.timeout = CardReader.DEFAULT_TIMEOUT

            // Coba beberapa command APDU umum
            val commands = arrayOf(
                byteArrayOf(0x00.toByte(), 0xB2.toByte(), 0x01.toByte(), 0x0C.toByte(), 0x00.toByte()),
                byteArrayOf(0x90.toByte(), 0x4C.toByte(), 0x00.toByte(), 0x00.toByte(), 0x08.toByte()),
                byteArrayOf(0xFF.toByte(), 0xCA.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
            )

            for (command in commands) {
                try {
                    val response = isoDep.transceive(command)
                    if (response.size >= 8) {
                        return formatCardNumber(response.copyOfRange(0, 8))
                    }
                } catch (e: IOException) {
                    continue
                }
            }
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            try {
                isoDep.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun readWithMifare(mifare: MifareClassic): String? {
        try {
            mifare.connect()
            // Coba baca sektor 0 yang biasanya berisi informasi kartu
            if (mifare.authenticateSectorWithKeyA(0, MifareClassic.KEY_DEFAULT)) {
                val block = mifare.readBlock(1)
                return formatCardNumber(block)
            }
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            try {
                mifare.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun readWithNfcA(nfcA: NfcA): String? {
        try {
            nfcA.connect()
            val response = nfcA.transceive(byteArrayOf(0x30.toByte(), 0x00.toByte()))
            return if (response.size >= 8) {
                formatCardNumber(response.copyOfRange(0, 8))
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            try {
                nfcA.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun readBalanceWithIsoDep(isoDep: IsoDep): Double? {
        try {
            isoDep.connect()
            isoDep.timeout = CardReader.DEFAULT_TIMEOUT

            // Coba beberapa command APDU umum untuk membaca saldo
            val commands = arrayOf(
                byteArrayOf(0x00.toByte(), 0xB2.toByte(), 0x01.toByte(), 0x0D.toByte(), 0x00.toByte()),
                byteArrayOf(0x90.toByte(), 0x5C.toByte(), 0x00.toByte(), 0x00.toByte(), 0x04.toByte()),
                byteArrayOf(0xFF.toByte(), 0xCB.toByte(), 0x00.toByte(), 0x00.toByte(), 0x04.toByte())
            )

            for (command in commands) {
                try {
                    val response = isoDep.transceive(command)
                    if (response.size >= 4) {
                        return parseBalance(response.copyOfRange(0, 4))
                    }
                } catch (e: IOException) {
                    continue
                }
            }
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            try {
                isoDep.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun readBalanceWithMifare(mifare: MifareClassic): Double? {
        try {
            mifare.connect()
            // Coba baca sektor yang biasanya berisi saldo
            if (mifare.authenticateSectorWithKeyA(1, MifareClassic.KEY_DEFAULT)) {
                val block = mifare.readBlock(4)
                return parseBalance(block.copyOfRange(0, 4))
            }
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            try {
                mifare.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun readBalanceWithNfcA(nfcA: NfcA): Double? {
        try {
            nfcA.connect()
            val response = nfcA.transceive(byteArrayOf(0x30.toByte(), 0x01.toByte()))
            return if (response.size >= 4) {
                parseBalance(response.copyOfRange(0, 4))
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            try {
                nfcA.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun formatCardNumber(data: ByteArray): String {
        return data.joinToString("") { "%02X".format(it) }
    }

    private fun parseBalance(data: ByteArray): Double {
        var balance = 0L
        for (i in data.indices) {
            balance = balance or ((data[i].toLong() and 0xFF) shl (8 * (data.size - 1 - i)))
        }
        return balance / 100.0
    }
} 