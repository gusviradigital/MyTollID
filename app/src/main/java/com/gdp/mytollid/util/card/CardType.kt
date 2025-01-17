package com.gdp.mytollid.util.card

enum class CardType(val displayName: String) {
    EMONEY("E-Money Mandiri"),
    FLAZZ("Flazz BCA"),
    BRIZZI("Brizzi BRI"),
    TAPCASH("TapCash BNI"),
    JAKCARD("JakCard Bank DKI"),
    MIFARE("MIFARE Plus"),
    UNKNOWN("Kartu Tidak Dikenal");

    companion object {
        fun fromAtr(atr: ByteArray): CardType {
            return when {
                // ATR patterns untuk masing-masing kartu
                // E-Money Mandiri pattern
                atr.contentEquals(byteArrayOf(0x3B, 0x67, 0x00, 0x00, 0x00, 0x73, 0x66, 0x74, 0x00)) -> EMONEY
                // Flazz pattern
                atr.contentEquals(byteArrayOf(0x3B, 0x67, 0x00, 0x00, 0x00, 0x66, 0x6C, 0x7A, 0x7A)) -> FLAZZ
                // Brizzi pattern
                atr.contentEquals(byteArrayOf(0x3B, 0x67, 0x00, 0x00, 0x00, 0x62, 0x72, 0x7A, 0x7A)) -> BRIZZI
                // TapCash pattern
                atr.contentEquals(byteArrayOf(0x3B, 0x67, 0x00, 0x00, 0x00, 0x74, 0x61, 0x70, 0x63)) -> TAPCASH
                // JakCard pattern
                atr.contentEquals(byteArrayOf(0x3B, 0x67, 0x00, 0x00, 0x00, 0x6A, 0x61, 0x6B, 0x63)) -> JAKCARD
                // MIFARE Plus pattern
                bytesToHex(atr) == "4D49464152452B" -> MIFARE
                else -> UNKNOWN
            }
        }

        private fun bytesToHex(bytes: ByteArray): String {
            return bytes.joinToString("") { "%02X".format(it) }
        }
    }
} 