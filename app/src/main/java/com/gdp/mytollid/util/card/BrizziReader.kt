package com.gdp.mytollid.util.card

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import java.io.IOException

class BrizziReader : CardReader {
    override fun readCardNumber(tag: Tag): String? {
        val isoDep = IsoDep.get(tag) ?: return null
        
        try {
            isoDep.connect()
            isoDep.timeout = CardReader.DEFAULT_TIMEOUT

            // Command APDU untuk Brizzi
            val command = byteArrayOf(
                0x90.toByte(), // CLA
                0x50.toByte(), // INS
                0x00.toByte(), // P1
                0x00.toByte(), // P2
                0x08.toByte()  // Le
            )

            val response = isoDep.transceive(command)
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
                isoDep.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun readBalance(tag: Tag): Double? {
        val isoDep = IsoDep.get(tag) ?: return null
        
        try {
            isoDep.connect()
            isoDep.timeout = CardReader.DEFAULT_TIMEOUT

            // Command APDU untuk membaca saldo Brizzi
            val command = byteArrayOf(
                0x90.toByte(), // CLA
                0x51.toByte(), // INS
                0x00.toByte(), // P1
                0x00.toByte(), // P2
                0x04.toByte()  // Le
            )

            val response = isoDep.transceive(command)
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
                isoDep.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun isCardSupported(tag: Tag): Boolean {
        return IsoDep.get(tag) != null && MifareClassic.get(tag) != null
    }

    override fun getCardType(): CardType = CardType.BRIZZI

    private fun formatCardNumber(data: ByteArray): String {
        return data.joinToString("") { "%02X".format(it) }
    }

    private fun parseBalance(data: ByteArray): Double {
        var balance = 0L
        for (i in data.indices) {
            balance = balance or ((data[i].toLong() and 0xFF) shl (8 * (data.size - 1 - i)))
        }
        return balance / 100.0 // Konversi ke format rupiah (2 desimal)
    }
} 