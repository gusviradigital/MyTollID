package com.gdp.mytollid.util.card

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.util.Log
import java.io.IOException

class EMoneyReader : CardReader {
    private val TAG = "EMoneyReader"

    override fun readCardNumber(tag: Tag): String? {
        // Coba baca dengan MifareClassic dulu
        val mifare = MifareClassic.get(tag)
        if (mifare != null) {
            try {
                mifare.connect()
                val uid = mifare.tag.id
                val uidHex = bytesToHex(uid)
                Log.d(TAG, "UID kartu (MifareClassic): $uidHex (${uid.size} bytes)")
                return uidHex
            } catch (e: Exception) {
                Log.e(TAG, "Error membaca UID dengan MifareClassic: ${e.message}")
            } finally {
                try {
                    mifare.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error menutup koneksi MifareClassic: ${e.message}")
                }
            }
        }

        // Jika MifareClassic gagal, coba dengan IsoDep
        val isoDep = IsoDep.get(tag) ?: return null
        try {
            isoDep.connect()
            isoDep.timeout = CardReader.DEFAULT_TIMEOUT

            val command = byteArrayOf(
                0x90.toByte(), // CLA
                0x4C.toByte(), // INS
                0x00.toByte(), // P1
                0x00.toByte(), // P2
                0x08.toByte()  // Le
            )

            val response = isoDep.transceive(command)
            return formatCardNumber(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error membaca nomor kartu dengan IsoDep: ${e.message}")
            return null
        } finally {
            try {
                isoDep.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error menutup koneksi IsoDep: ${e.message}")
            }
        }
    }

    override fun readBalance(tag: Tag): Double? {
        // Coba baca dengan MifareClassic dulu
        val mifare = MifareClassic.get(tag)
        if (mifare != null) {
            try {
                mifare.connect()
                
                // Daftar key yang umum digunakan
                val keys = listOf(
                    // Key A standar untuk E-Money
                    byteArrayOf(0xA0.toByte(), 0xA1.toByte(), 0xA2.toByte(), 0xA3.toByte(), 0xA4.toByte(), 0xA5.toByte()),
                    // Key B standar untuk E-Money
                    byteArrayOf(0xB0.toByte(), 0xB1.toByte(), 0xB2.toByte(), 0xB3.toByte(), 0xB4.toByte(), 0xB5.toByte()),
                    // Key default MIFARE
                    MifareClassic.KEY_DEFAULT,
                    // Key factory default
                    byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
                )
                
                // Coba beberapa sektor yang mungkin berisi saldo
                val sectors = listOf(1, 2, 3, 4, 8)
                
                for (sector in sectors) {
                    for (key in keys) {
                        try {
                            // Coba autentikasi dengan Key A
                            if (mifare.authenticateSectorWithKeyA(sector, key)) {
                                val block = mifare.sectorToBlock(sector)
                                val data = mifare.readBlock(block)
                                Log.d(TAG, "Data sektor $sector blok $block dengan Key A: ${bytesToHex(data)}")
                                
                                // Coba parse saldo dengan beberapa format
                                val balance = try {
                                    // Format 1: 4 byte big endian
                                    val bal1 = data.take(4).fold(0L) { acc, byte ->
                                        (acc shl 8) or (byte.toLong() and 0xFF)
                                    }
                                    Log.d(TAG, "Saldo format 1: $bal1")
                                    
                                    // Format 2: 4 byte little endian
                                    val bal2 = data.take(4).reversed().fold(0L) { acc, byte ->
                                        (acc shl 8) or (byte.toLong() and 0xFF)
                                    }
                                    Log.d(TAG, "Saldo format 2: $bal2")
                                    
                                    // Format 3: BCD format (2 byte)
                                    val bcdString = data.take(2).joinToString("") { byte ->
                                        String.format("%02X", byte)
                                    }
                                    val bal3 = bcdString.toLongOrNull() ?: 0L
                                    Log.d(TAG, "Saldo format 3: $bal3")
                                    
                                    // Pilih saldo yang masuk akal (antara 1000 dan 1000000)
                                    when {
                                        bal1 in 1000..1000000 -> bal1
                                        bal2 in 1000..1000000 -> bal2
                                        bal3 in 1000..1000000 -> bal3
                                        else -> null
                                    }
                                } catch (e: Exception) {
                                    null
                                }
                                
                                if (balance != null) {
                                    return balance / 100.0
                                }
                            }
                            
                            // Coba autentikasi dengan Key B
                            if (mifare.authenticateSectorWithKeyB(sector, key)) {
                                val block = mifare.sectorToBlock(sector)
                                val data = mifare.readBlock(block)
                                Log.d(TAG, "Data sektor $sector blok $block dengan Key B: ${bytesToHex(data)}")
                                
                                // Coba parse saldo dengan beberapa format
                                val balance = try {
                                    // Format 1: 4 byte big endian
                                    val bal1 = data.take(4).fold(0L) { acc, byte ->
                                        (acc shl 8) or (byte.toLong() and 0xFF)
                                    }
                                    Log.d(TAG, "Saldo format 1: $bal1")
                                    
                                    // Format 2: 4 byte little endian
                                    val bal2 = data.take(4).reversed().fold(0L) { acc, byte ->
                                        (acc shl 8) or (byte.toLong() and 0xFF)
                                    }
                                    Log.d(TAG, "Saldo format 2: $bal2")
                                    
                                    // Format 3: BCD format (2 byte)
                                    val bcdString = data.take(2).joinToString("") { byte ->
                                        String.format("%02X", byte)
                                    }
                                    val bal3 = bcdString.toLongOrNull() ?: 0L
                                    Log.d(TAG, "Saldo format 3: $bal3")
                                    
                                    // Pilih saldo yang masuk akal (antara 1000 dan 1000000)
                                    when {
                                        bal1 in 1000..1000000 -> bal1
                                        bal2 in 1000..1000000 -> bal2
                                        bal3 in 1000..1000000 -> bal3
                                        else -> null
                                    }
                                } catch (e: Exception) {
                                    null
                                }
                                
                                if (balance != null) {
                                    return balance / 100.0
                                }
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "Gagal membaca sektor $sector: ${e.message}")
                            continue
                        }
                    }
                }
                Log.e(TAG, "Tidak dapat menemukan saldo yang valid di semua sektor")
            } catch (e: Exception) {
                Log.e(TAG, "Error membaca saldo dengan MifareClassic: ${e.message}")
            } finally {
                try {
                    mifare.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error menutup koneksi MifareClassic: ${e.message}")
                }
            }
        }

        // Jika MifareClassic gagal, coba dengan IsoDep
        val isoDep = IsoDep.get(tag) ?: return null
        try {
            isoDep.connect()
            isoDep.timeout = CardReader.DEFAULT_TIMEOUT

            // Command APDU untuk membaca saldo E-Money
            val commands = listOf(
                // Command 1: Standard
                byteArrayOf(0x90.toByte(), 0x5C.toByte(), 0x00.toByte(), 0x00.toByte(), 0x04.toByte()),
                // Command 2: Get Balance
                byteArrayOf(0x80.toByte(), 0x5C.toByte(), 0x00.toByte(), 0x00.toByte(), 0x04.toByte()),
                // Command 3: Read Record
                byteArrayOf(0x00.toByte(), 0xB2.toByte(), 0x01.toByte(), 0x0C.toByte(), 0x00.toByte())
            )

            for (command in commands) {
                try {
                    Log.d(TAG, "Mencoba command APDU: ${bytesToHex(command)}")
                    val response = isoDep.transceive(command)
                    Log.d(TAG, "Response APDU: ${bytesToHex(response)}")
                    
                    if (response.size >= 4) {
                        val balance = parseBalance(response)
                        if (balance > 10.0 && balance < 10000.0) { // Validasi saldo masuk akal
                            return balance
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Command gagal: ${e.message}")
                    continue
                }
            }
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error membaca saldo dengan IsoDep: ${e.message}")
            return null
        } finally {
            try {
                isoDep.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error menutup koneksi IsoDep: ${e.message}")
            }
        }
    }

    override fun isCardSupported(tag: Tag): Boolean {
        return IsoDep.get(tag) != null || MifareClassic.get(tag) != null
    }

    override fun getCardType(): CardType = CardType.EMONEY

    private fun formatCardNumber(data: ByteArray): String {
        return try {
            data.take(8).joinToString("") { "%02d".format(it.toInt() and 0xFF) }
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting card number: ${e.message}")
            ""
        }
    }

    private fun parseBalance(data: ByteArray): Double {
        try {
            // Parse saldo (4 byte, big endian)
            val balance = data.take(4).fold(0L) { acc, byte ->
                (acc shl 8) or (byte.toLong() and 0xFF)
            }
            Log.d(TAG, "Saldo mentah: $balance")
            return balance / 100.0 // Konversi ke format rupiah
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing saldo: ${e.message}")
            return 0.0
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02X".format(it) }
    }
} 