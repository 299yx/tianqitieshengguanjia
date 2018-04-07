package tqtsgj.bs.com.tianqitieshengguanjia.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Administrator on 2018/3/9 0009.
 */
object SharePreferenesUitls {
    fun getSharePreferenes(context: Context, filename: String): SharedPreferences {
        return context.getSharedPreferences(filename, Context.MODE_PRIVATE)
    }
}