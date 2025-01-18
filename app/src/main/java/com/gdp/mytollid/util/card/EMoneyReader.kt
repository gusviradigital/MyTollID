package com.gdp.mytollid.util.card

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.util.Log
import java.io.IOException

class EMoneyReader : CardReader {
    private val TAG = "EMoneyReader"

    override fun readCardNumber(tag: Tag): String? {
        // Coba baca dengan MifareClassic
        val mifare = MifareClassic.get(tag)
        if (mifare != null) {
            try {
                mifare.connect()
                val uid = mifare.tag.id
                val uidHex = bytesToHex(uid)
                Log.d(TAG, "UID kartu: $uidHex (${uid.size} bytes)")
                return uidHex
            } catch (e: Exception) {
                Log.e(TAG, "Error membaca UID: ${e.message}")
            } finally {
                try {
                    mifare.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error menutup koneksi: ${e.message}")
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
        // Coba baca dengan MifareClassic
        val mifare = MifareClassic.get(tag)
        if (mifare != null) {
            try {
                mifare.connect()
                
                // Key untuk E-Money
                val keys = listOf(
                    // Default factory key
                    byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()),
                    // Default MIFARE key
                    MifareClassic.KEY_DEFAULT,
                    // E-Money key A
                    byteArrayOf(0xA0.toByte(), 0xA1.toByte(), 0xA2.toByte(), 0xA3.toByte(), 0xA4.toByte(), 0xA5.toByte()),
                    // E-Money key B
                    byteArrayOf(0xB0.toByte(), 0xB1.toByte(), 0xB2.toByte(), 0xB3.toByte(), 0xB4.toByte(), 0xB5.toByte())
                )
                
                // Coba baca sektor yang berisi saldo
                val sectors = listOf(1, 2, 3, 4)
                
                for (sector in sectors) {
                    for (key in keys) {
                        try {
                            if (mifare.authenticateSectorWithKeyA(sector, key)) {
                                val firstBlock = mifare.sectorToBlock(sector)
                                val lastBlock = firstBlock + mifare.getBlockCountInSector(sector) - 1
                                
                                // Baca semua blok dalam sektor
                                for (block in firstBlock..lastBlock) {
                                    try {
                                        val data = mifare.readBlock(block)
                                        Log.d(TAG, "Data sektor $sector blok $block: ${bytesToHex(data)}")
                                        
                                        // Coba parse saldo
                                        val balance = try {
                                            val value = data.take(4).fold(0L) { acc, byte ->
                                                (acc shl 8) or (byte.toLong() and 0xFF)
                                            }
                                            if (value in 1000..1000000) value else null
                                        } catch (e: Exception) {
                                            null
                                        }
                                        
                                        if (balance != null) {
                                            return balance / 100.0
                                        }
                                    } catch (e: Exception) {
                                        Log.d(TAG, "Gagal membaca blok $block: ${e.message}")
                                        continue
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "Gagal autentikasi sektor $sector: ${e.message}")
                            continue
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error membaca saldo: ${e.message}")
            } finally {
                try {
                    mifare.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error menutup koneksi: ${e.message}")
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
                0x5C.toByte(), // INS
                0x00.toByte(), // P1
                0x00.toByte(), // P2
                0x04.toByte()  // Le
            )

            val response = isoDep.transceive(command)
            if (response.size >= 4) {
                val balance = parseBalance(response)
                if (balance > 10.0 && balance < 10000.0) {
                    return balance
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
        return MifareClassic.get(tag) != null || IsoDep.get(tag) != null
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
            val balance = data.take(4).fold(0L) { acc, byte ->
                (acc shl 8) or (byte.toLong() and 0xFF)
            }
            Log.d(TAG, "Saldo mentah: $balance")
            return balance / 100.0
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing saldo: ${e.message}")
            return 0.0
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02X".format(it) }
    }
} 