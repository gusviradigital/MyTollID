package com.gdp.mytollid.util

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.NfcA
import java.io.IOException

object NfcUtils {
    private const val DEFAULT_TIMEOUT = 2000 // 2 seconds

    fun readCardNumber(tag: Tag): String? {
        val isoDep = IsoDep.get(tag) ?: return null
        
        try {
            isoDep.connect()
            isoDep.timeout = DEFAULT_TIMEOUT

            // Command APDU untuk membaca nomor kartu
            val command = byteArrayOf(
                0x00.toByte(), // CLA
                0xB2.toByte(), // INS
                0x01.toByte(), // P1
                0x0C.toByte(), // P2
                0x00.toByte()  // Le
            )

            val response = isoDep.transceive(command)
            return if (response.size >= 16) {
                // Format nomor kartu sesuai dengan format E-Toll
                formatCardNumber(response.copyOfRange(0, 16))
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

    fun readBalance(tag: Tag): Double? {
        val isoDep = IsoDep.get(tag) ?: return null
        
        try {
            isoDep.connect()
            isoDep.timeout = DEFAULT_TIMEOUT

            // Command APDU untuk membaca saldo
            val command = byteArrayOf(
                0x00.toByte(), // CLA
                0xB2.toByte(), // INS
                0x01.toByte(), // P1
                0x0D.toByte(), // P2
                0x00.toByte()  // Le
            )

            val response = isoDep.transceive(command)
            return if (response.size >= 4) {
                // Konversi 4 byte saldo ke Double
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

    fun isEMoneyCard(tag: Tag): Boolean {
        // Cek apakah kartu mendukung teknologi yang diperlukan
        return IsoDep.get(tag) != null || 
               MifareClassic.get(tag) != null ||
               NfcA.get(tag) != null
    }
} 