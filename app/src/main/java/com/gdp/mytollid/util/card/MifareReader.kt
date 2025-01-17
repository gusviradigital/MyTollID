package com.gdp.mytollid.util.card

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.util.Log
import java.io.IOException

class MifareReader : CardReader {
    private val TAG = "MifareReader"

    override fun readCardNumber(tag: Tag): String? {
        val mifare = MifareClassic.get(tag) ?: return null
        
        try {
            mifare.connect()
            
            // Baca UID langsung dari MifareClassic
            val uid = mifare.tag.id
            val uidHex = bytesToHex(uid)
            Log.d(TAG, "UID kartu (MifareClassic): $uidHex (${uid.size} bytes)")
            return uidHex
        } catch (e: Exception) {
            Log.e(TAG, "Error membaca UID kartu: ${e.message}")
            return null
        } finally {
            try {
                mifare.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error menutup koneksi: ${e.message}")
            }
        }
    }

    override fun readBalance(tag: Tag): Double? {
        val mifare = MifareClassic.get(tag) ?: return null
        
        try {
            mifare.connect()
            
            // Key untuk E-Money
            val keyA = byteArrayOf(
                0xA0.toByte(), 0xA1.toByte(), 0xA2.toByte(),
                0xA3.toByte(), 0xA4.toByte(), 0xA5.toByte()
            )
            
            // Coba baca sektor yang berisi saldo
            val sector = 2 // Sektor 2 untuk E-Money
            val block = mifare.sectorToBlock(sector) // Blok pertama di sektor 2
            
            if (mifare.authenticateSectorWithKeyA(sector, keyA)) {
                val data = mifare.readBlock(block)
                Log.d(TAG, "Data blok $block: ${bytesToHex(data)}")
                
                // Parse saldo E-Money (4 byte, big endian)
                val balance = data.take(4).fold(0L) { acc, byte ->
                    (acc shl 8) or (byte.toLong() and 0xFF)
                }
                Log.d(TAG, "Saldo mentah: $balance")
                return balance / 100.0 // Konversi ke format rupiah
            } else {
                Log.e(TAG, "Autentikasi gagal untuk sektor $sector")
            }
            
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error membaca saldo: ${e.message}")
            return null
        } finally {
            try {
                mifare.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error menutup koneksi: ${e.message}")
            }
        }
    }

    override fun isCardSupported(tag: Tag): Boolean {
        return MifareClassic.get(tag) != null
    }

    override fun getCardType(): CardType = CardType.EMONEY

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02X".format(it) }
    }
} 