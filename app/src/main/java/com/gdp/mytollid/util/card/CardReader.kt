package com.gdp.mytollid.util.card

import android.nfc.Tag
import android.nfc.tech.IsoDep

interface CardReader {
    fun readCardNumber(tag: Tag): String?
    fun readBalance(tag: Tag): Double?
    fun isCardSupported(tag: Tag): Boolean
    fun getCardType(): CardType
    
    companion object {
        const val DEFAULT_TIMEOUT = 2000 // 2 seconds
        
        fun getReader(tag: Tag): CardReader {
            val isoDep = IsoDep.get(tag) ?: return DefaultReader()
            val atr = isoDep.historicalBytes ?: return DefaultReader()
            
            return when (CardType.fromAtr(atr)) {
                CardType.EMONEY -> EMoneyReader()
                CardType.FLAZZ -> FlazzReader()
                CardType.BRIZZI -> BrizziReader()
                CardType.TAPCASH -> TapCashReader()
                CardType.JAKCARD -> JakCardReader()
                CardType.UNKNOWN -> DefaultReader()
            }
        }
    }
} 