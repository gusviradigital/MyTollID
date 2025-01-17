package com.gdp.mytollid

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.gdp.mytollid.databinding.ActivityMainBinding
import com.gdp.mytollid.ui.scan.ScanFragment
import com.gdp.mytollid.util.NfcUtils

class MainActivity : AppCompatActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private lateinit var binding: ActivityMainBinding
    private var navHostFragment: NavHostFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Navigation
        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment!!.navController
        binding.bottomNavigation.setupWithNavController(navController)

        // Initialize NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "Perangkat tidak mendukung NFC", Toast.LENGTH_LONG).show()
        } else if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "NFC tidak aktif", Toast.LENGTH_LONG).show()
        }

        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            tag?.let { processTag(it) }
        }
    }

    private fun processTag(tag: Tag) {
        if (!NfcUtils.isEMoneyCard(tag)) {
            Toast.makeText(this, "Bukan kartu E-Money yang valid", Toast.LENGTH_SHORT).show()
            return
        }

        val cardNumber = NfcUtils.readCardNumber(tag)
        val balance = NfcUtils.readBalance(tag)

        if (cardNumber != null && balance != null) {
            // Kirim data ke ScanFragment jika sedang aktif
            val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()
            if (currentFragment is ScanFragment) {
                currentFragment.onCardScanned(cardNumber, balance)
            } else {
                // Jika tidak di ScanFragment, navigasi ke sana dengan data
                navHostFragment?.navController?.navigate(
                    R.id.navigation_scan,
                    Bundle().apply {
                        putString("cardNumber", cardNumber)
                        putDouble("balance", balance)
                    }
                )
            }
        } else {
            Toast.makeText(this, "Gagal membaca kartu", Toast.LENGTH_SHORT).show()
        }
    }
} 