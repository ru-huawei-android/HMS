package com.huawei.hms.ads6

import com.huawei.hms.ads.AdParam

class Utils {
    companion object {
        /*
            Details: https://developer.huawei.com/consumer/en/doc/development/HMS-References/ads-api-adparam-errorcode
        */
        @JvmStatic
        fun getErrorMessage(errorCode: Int): String {
            return when (errorCode) {
                AdParam.ErrorCode.INNER -> "Internal error"
                AdParam.ErrorCode.INVALID_REQUEST -> "The ad request is invalid due to causes, such as not setting the ad slot ID or invalid banner ad size."
                AdParam.ErrorCode.NETWORK_ERROR -> "The ad request fails due to a network connection error."
                AdParam.ErrorCode.NO_AD -> "The ad request is successful, but the server does not return any available ad material."
                AdParam.ErrorCode.AD_LOADING -> "The ad is being requested and cannot be requested again."
                AdParam.ErrorCode.LOW_API -> "The API version is not supported by the HUAWEI Ads SDK."
                AdParam.ErrorCode.BANNER_AD_EXPIRE -> "The banner ad has expired."
                AdParam.ErrorCode.BANNER_AD_CANCEL -> "The banner ad task is removed."
                else -> "Unknown error"
            }
        }
    }
}