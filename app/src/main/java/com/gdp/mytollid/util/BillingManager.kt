package com.gdp.mytollid.util

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingManager private constructor(
    private val context: Context
) : PurchasesUpdatedListener {

    private val _purchaseStatus = MutableLiveData<PurchaseStatus>()
    val purchaseStatus: LiveData<PurchaseStatus> = _purchaseStatus

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    // Koneksi berhasil
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Coba hubungkan kembali
                billingClient.startConnection(this)
            }
        })
    }

    private fun queryPurchases() {
        CoroutineScope(Dispatchers.IO).launch {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            
            val result = billingClient.queryPurchasesAsync(params)
            for (purchase in result.purchasesList) {
                if (purchase.products.contains(PREMIUM_PRODUCT_ID)) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        _purchaseStatus.postValue(PurchaseStatus.PURCHASED)
                        return@launch
                    }
                }
            }
            _purchaseStatus.postValue(PurchaseStatus.NOT_PURCHASED)
        }
    }

    fun launchBillingFlow(activity: Activity) {
        CoroutineScope(Dispatchers.IO).launch {
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PREMIUM_PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            val productDetailsResult = withContext(Dispatchers.IO) {
                billingClient.queryProductDetails(params)
            }

            if (productDetailsResult.billingResult.responseCode == BillingResponseCode.OK) {
                productDetailsResult.productDetailsList?.firstOrNull()?.let { productDetails ->
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(
                            listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .build()
                            )
                        )
                        .build()

                    withContext(Dispatchers.Main) {
                        billingClient.launchBillingFlow(activity, billingFlowParams)
                    }
                }
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: List<Purchase>?
    ) {
        if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    _purchaseStatus.value = PurchaseStatus.PURCHASED
                    // Konfirmasi pembelian ke Google Play
                    acknowledgePurchase(purchase)
                }
            }
        } else if (billingResult.responseCode == BillingResponseCode.USER_CANCELED) {
            _purchaseStatus.value = PurchaseStatus.CANCELED
        } else {
            _purchaseStatus.value = PurchaseStatus.ERROR
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.acknowledgePurchase(params) { billingResult ->
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    // Pembelian berhasil dikonfirmasi
                }
            }
        }
    }

    companion object {
        private const val PREMIUM_PRODUCT_ID = "premium_upgrade"
        private var instance: BillingManager? = null

        fun getInstance(context: Context): BillingManager {
            return instance ?: synchronized(this) {
                instance ?: BillingManager(context).also { instance = it }
            }
        }
    }

    enum class PurchaseStatus {
        NOT_PURCHASED,
        PURCHASED,
        CANCELED,
        ERROR
    }
} 