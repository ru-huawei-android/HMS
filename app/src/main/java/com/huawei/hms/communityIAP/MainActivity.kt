/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.huawei.hms.communityIAP

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.hms.communityIAP.Key.ANNUAL_PRO
import com.huawei.hms.communityIAP.Key.BEGINNER_PACK
import com.huawei.hms.communityIAP.Key.MONTHLY_PRO
import com.huawei.hms.communityIAP.Key.REQ_CODE_BUY
import com.huawei.hms.communityIAP.Key.REQ_CODE_LOGIN
import com.huawei.hms.communityIAP.Key.SEASON_PRO
import com.huawei.hms.communityIAP.Key.SEED
import com.huawei.hms.communityIAP.Key.SKILLED_PACK
import com.huawei.hms.communityIAP.Key.SOIL_PIECE
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


const val TAG = "v4-IAP-Demo"

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private var products = arrayListOf<ProductModel>()
    private var isSandbox = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // проверяем доступность сервиса в регионе
        isEnvironmentReady()
        // проверяем доступность тестового окружения при необходимости
        checkSandboxing()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = RecyclerViewAdapter(products, ::gotoPay)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    /**
     Обновляем список товаров и подписок
     */
    private fun initControls() {
        val deferred = listOf(
                Pair(IapClient.PriceType.IN_APP_CONSUMABLE, listOf(BEGINNER_PACK, SKILLED_PACK)),
                Pair(IapClient.PriceType.IN_APP_SUBSCRIPTION, listOf(MONTHLY_PRO, SEASON_PRO, ANNUAL_PRO)),
                Pair(IapClient.PriceType.IN_APP_NONCONSUMABLE, listOf(SEED, SOIL_PIECE))
        ).map { pair ->
            CoroutineScope(Dispatchers.IO).async {
                Log.d(TAG, "${pair.first} load started")
                loadProducts(
                        pair.first,
                        pair.second).also { Log.d(TAG, "${pair.first} load finished") }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
                val results = deferred.map { it.await() }.flatten()
                Log.d(TAG, "show products")
                showProduct(results)
        }
    }

    /**
     * Загружаем информацию о продуктах
     */
    private suspend fun loadProducts(type: Int, products: List<String>): List<ProductInfo> =
            suspendCoroutine { continuation ->
                Iap.getIapClient(this)
                        .obtainProductInfo(
                                ProductInfoReq().apply {
                                    priceType = type     // запрос продуктов происходит в соответствии с типом
                                    productIds = products   // Здесь требуется добавить сконфигурированные на странице продуктов идентификаторы
                                }
                        )
                        .also {
                            it.addOnSuccessListener { result ->
                                continuation.resume(result?.productInfoList.orEmpty())
                            }.addOnFailureListener { e ->
                                Log.e(TAG, "Load products error: $e!")
                                Toast.makeText(this, "Load products error", Toast.LENGTH_SHORT).show()
                                continuation.resume(emptyList())
                            }
                        }
            }

    private fun showProduct(list: List<ProductInfo>) {
        products.clear()
        products.addAll(list.map {
            ProductModel(it.productName, it.productDesc, it.price, it.productId, it.priceType)
        } as ArrayList<ProductModel>)
        recyclerView.adapter?.notifyDataSetChanged()
    }

    /**
     * Отправка запроса на покупку в приложении
     * @param productId Идентификатор продукта, сконфигурированный на странице продуктов приложения
     * @param type  Тип продукта(consumable, non-consumable, subscription)
     */
    private fun gotoPay(productId: String, type: Int) {
        Log.i(TAG, "call createPurchaseIntent")
        Iap.getIapClient(this)
                .createPurchaseIntent(
                        PurchaseIntentReq().apply { this.productId = productId; priceType = type }
                )
                .addOnSuccessListener { result ->
                    Log.i(TAG, "createPurchaseIntent, onSuccess")
                    if (result?.status?.hasResolution() == true) {
                        try {
                            result.status.startResolutionForResult(this, REQ_CODE_BUY)
                        } catch (exp: SendIntentException) {
                            Log.e(TAG, exp.message ?: "error")
                        }
                    } else {
                        Log.e(TAG, "intent is null")
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, e.message ?: "")
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    if (e is IapApiException) {
                        val returnCode = e.statusCode
                        Log.e(TAG, "createPurchaseIntent, returnCode: $returnCode")
                        // handle error scenarios
                    }
                }
    }

    // проверка результатов запросов на покупку
    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        // код возврата совпадает с кодом запроса
        if (requestCode == REQ_CODE_BUY) {
            if (data == null) {
                Toast.makeText(this,
                        "Buy product error",
                        Toast.LENGTH_SHORT).show()
                return
            }
            val purchaseResultInfo =
                    Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data)
            when (purchaseResultInfo.returnCode) {
                OrderStatusCode.ORDER_STATE_SUCCESS -> {
                    // проверка подписи результата
                    CipherUtil.doCheck(
                            purchaseResultInfo.inAppPurchaseData,
                            purchaseResultInfo.inAppDataSignature,
                            Key.publicKey
                    ).also {
                        Toast.makeText(this,
                                "Pay successful${if (it) ", sign failed" else ""}",
                                Toast.LENGTH_SHORT).show()
                    }
                }
                OrderStatusCode.ORDER_STATE_CANCEL -> {
                    // Пользователь отменил покупку
                    Toast.makeText(this, "user cancel", Toast.LENGTH_SHORT).show()
                }
                OrderStatusCode.ORDER_PRODUCT_OWNED -> {
                    // Пользователь уже приобрёл продукт
                    Toast.makeText(this, "you have owned the product", Toast.LENGTH_SHORT).show()
                    // тут можно опрелить действия по доставке функционала продукта пользователю
                }
                else -> Toast.makeText(this, "Pay failed", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQ_CODE_LOGIN) {
            if (data != null) {
                // Obtain the execution result.
                val returnCode: Int = data.getIntExtra("returnCode", 1)
                if (returnCode == 0) initControls()
            }
        }
    }

    // Общая проверка окружения на доступность сервиса покупок в приложении
    private fun isEnvironmentReady() {
        Iap.getIapClient(this)
                .isEnvReady
                .addOnSuccessListener {
                    // Сервис доступен
                    // загружаем доступные продукты
                    initControls()
                }.addOnFailureListener { e ->
                    if (e is IapApiException) {
                        when (e.status.statusCode) {
                            OrderStatusCode.ORDER_HWID_NOT_LOGIN ->
                                // Пользователь не залогинен
                                if (e.status.hasResolution()) {
                                    try {
                                        // Отправляем запрос на логин
                                        e.status.startResolutionForResult(this@MainActivity, REQ_CODE_LOGIN)
                                    } catch (exp: SendIntentException) {
                                    }
                                }
                            OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED ->
                                // Текущее расположение не поддерживается
                                Log.e(TAG, "Current region is not supported by IAP")
                            else ->
                                Log.e(TAG, "Unknown error")
                        }
                    }
                    Log.e(TAG, "Unknown error")
                }
    }

    // проверка доступности изолированного окружения для совершения тестовых платежей
    // подробнее здесь: https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/iap-sandbox-testing-v4
    // на начало мая 2020 недоступно
    private fun checkSandboxing() {
        Iap.getIapClient(this)
                .isSandboxActivated(IsSandboxActivatedReq())
                .addOnSuccessListener { result ->
                    isSandbox = result.isSandboxApk && result.isSandboxUser
                    Log.i(TAG, "isSandboxActivated: $isSandbox")
                }.addOnFailureListener { e ->
                    Log.e(TAG, "isSandboxActivated call fail: ${e.message}")
                    isSandbox = false
                }
    }


}