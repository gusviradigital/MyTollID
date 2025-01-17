package com.gdp.mytollid

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.gdp.mytollid.databinding.ActivityMainBinding
import com.gdp.mytollid.ui.scan.ScanFragment
import com.gdp.mytollid.ui.settings.SettingsViewModel
import com.gdp.mytollid.util.NfcUtils
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class MainActivity : AppCompatActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private lateinit var binding: ActivityMainBinding
    private var navHostFragment: NavHostFragment? = null
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize MobileAds
        MobileAds.initialize(this)
        setupAds()

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

        // Handle NFC intent if activity was started from NFC
        if (intent != null) {
            onNewIntent(intent)
        }
    }

    private fun setupAds() {
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        
        if (intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            
            val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }

            if (tag != null) {
                processTag(tag)
            }
        }
    }

    private fun processTag(tag: Tag) {
        if (!NfcUtils.isEMoneyCard(tag)) {
            Toast.makeText(this, "Kartu tidak didukung", Toast.LENGTH_SHORT).show()
            return
        }

        val cardNumber = NfcUtils.readCardNumber(tag)
        val balance = NfcUtils.readBalance(tag)
        val cardType = NfcUtils.getCardType(tag)

        if (cardNumber != null && balance != null) {
            val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()
            
            if (currentFragment is ScanFragment) {
                // Jika ScanFragment aktif, langsung kirim data
                currentFragment.onCardScanned(cardNumber, balance, cardType)
            } else {
                // Jika tidak, navigasi ke ScanFragment dengan data
                val bundle = Bundle().apply {
                    putString("cardNumber", cardNumber)
                    putDouble("balance", balance)
                    putString("cardType", cardType.name)
                }
                navHostFragment?.navController?.navigate(R.id.navigation_scan, bundle)
            }
        } else {
            Toast.makeText(this, "Gagal membaca kartu", Toast.LENGTH_SHORT).show()
        }
    }
} 