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

object Key {
    // !!! в реальном приложении ключ не хранить в открытом виде !!!
    const val publicKey = BuildConfig.IAPKEY // здесь нужен ключ , полученный после включения IAP
    //(Мои приложения->[Имя приложения]->Разработка->In-App Purchases)

    // Коды запросов
    const val REQ_CODE_BUY = 1003
    const val REQ_CODE_LOGIN = 1004

    // Коды продуктов, сконфигурированных в консоли разработчика

    // подписки
    const val MONTHLY_PRO = "monthly_pro"
    const val SEASON_PRO = "seasonal_pro"
    const val ANNUAL_PRO = "annual_pro"

    const val PLATINUM_VER = "platinum_version"

    // consumable
    const val SEED = "seed"
    const val SOIL_PIECE = "soil_piece"

    // non-consumable
    const val BEGINNER_PACK = "beginner_pack"
    const val SKILLED_PACK = "skilled_pack"
}