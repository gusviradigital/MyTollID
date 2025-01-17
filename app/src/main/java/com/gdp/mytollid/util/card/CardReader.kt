package com.gdp.mytollid.util.card

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log

interface CardReader {
    fun readCardNumber(tag: Tag): String?
    fun readBalance(tag: Tag): Double?
    fun isCardSupported(tag: Tag): Boolean
    fun getCardType(): CardType
    
    companion object {
        const val DEFAULT_TIMEOUT = 2000 // 2 seconds
        private const val TAG = "CardReader"
        
        fun getReader(tag: Tag): CardReader {
            // Cek apakah kartu mendukung IsoDep
            val isoDep = IsoDep.get(tag)
            
            // Jika tidak mendukung IsoDep, gunakan MifareClassic
            if (isoDep == null) {
                Log.d(TAG, "IsoDep tidak tersedia, mencoba MifareClassic")
                return EMoneyReader() // Karena kita tahu ini E-Money
            }
            
            val atr = isoDep.historicalBytes ?: run {
                Log.d(TAG, "Historical bytes tidak tersedia, mencoba MifareClassic")
                return EMoneyReader() // Karena kita tahu ini E-Money
            }
            
            Log.d(TAG, "ATR: ${bytesToHex(atr)}")
            
            val cardType = CardType.fromAtr(atr)
            Log.d(TAG, "Tipe kartu terdeteksi: ${cardType.displayName}")
            
            return when (cardType) {
                CardType.EMONEY -> EMoneyReader()
                CardType.FLAZZ -> FlazzReader()
                CardType.BRIZZI -> BrizziReader()
                CardType.TAPCASH -> TapCashReader()
                CardType.JAKCARD -> JakCardReader()
                CardType.UNKNOWN -> DefaultReader()
            }
        }

        private fun bytesToHex(bytes: ByteArray): String {
            return bytes.joinToString("") { "%02X".format(it) }
        }
    }
} 