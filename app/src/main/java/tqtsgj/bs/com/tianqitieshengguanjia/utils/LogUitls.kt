package tqtsgj.bs.com.tianqitieshengguanjia.utils

import android.util.Log
import tqtsgj.bs.com.tianqitieshengguanjia.BuildConfig

/**
 * Created by Administrator on 2018/3/7 0007.
 */
object LogUitls {
    val TAG = "天气贴身管家"

    fun e(msg: String) {
        if (BuildConfig.isDebug) {
            Log.e(TAG, msg)
        }
    }

    fun v(msg: String) {
        if (BuildConfig.isDebug) {
            Log.v(TAG, msg)
        }
    }

    fun w(msg: String) {
        if (BuildConfig.isDebug) {
            Log.w(TAG, msg)
        }
    }

    fun d(msg: String) {
        if (BuildConfig.isDebug) {
            Log.d(TAG, msg)
        }
    }
}