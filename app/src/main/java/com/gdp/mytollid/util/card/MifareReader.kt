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
            
            // Coba baca sektor 1 blok 4 (lokasi umum untuk saldo)
            val sector = 1
            val block = 4
            
            if (mifare.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT)) {
                val data = mifare.readBlock(block)
                Log.d(TAG, "Data blok $block: ${bytesToHex(data)}")
                return parseBalance(data)
            } else {
                Log.e(TAG, "Autentikasi gagal untuk sektor $sector")
                return null
            }
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

    override fun getCardType(): CardType = CardType.MIFARE

    private fun parseBalance(data: ByteArray): Double {
        try {
            // Ambil 4 byte pertama sebagai saldo
            val balance = data.take(4).fold(0L) { acc, byte ->
                (acc shl 8) or (byte.toLong() and 0xFF)
            }
            Log.d(TAG, "Saldo mentah: $balance")
            return balance / 100.0 // Konversi ke format rupiah (2 desimal)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing saldo: ${e.message}")
            return 0.0
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02X".format(it) }
    }
} 