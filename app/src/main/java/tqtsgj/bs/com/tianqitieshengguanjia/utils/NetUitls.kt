package tqtsgj.bs.com.tianqitieshengguanjia.utils

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Administrator on 2018/3/7 0007.
 */
object NetUitls {
    fun <T> getRetrofit(url: String, service: Class<T>): T {
        val retrofit = Retrofit.Builder().baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        return retrofit.create(service)
    }
}