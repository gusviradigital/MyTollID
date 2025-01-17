package com.gdp.mytollid.util

import android.nfc.Tag
import com.gdp.mytollid.util.card.CardReader
import com.gdp.mytollid.util.card.CardType

object NfcUtils {
    fun readCardNumber(tag: Tag): String? {
        return CardReader.getReader(tag).readCardNumber(tag)
    }

    fun readBalance(tag: Tag): Double? {
        return CardReader.getReader(tag).readBalance(tag)
    }

    fun isEMoneyCard(tag: Tag): Boolean {
        return CardReader.getReader(tag).isCardSupported(tag)
    }

    fun getCardType(tag: Tag): CardType {
        return CardReader.getReader(tag).getCardType()
    }
} 