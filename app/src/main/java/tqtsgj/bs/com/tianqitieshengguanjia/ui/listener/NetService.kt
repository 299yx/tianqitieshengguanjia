package tqtsgj.bs.com.tianqitieshengguanjia.ui.listener

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import tqtsgj.bs.com.tianqitieshengguanjia.entity.Weather

/**
 * Created by Administrator on 2018/3/7 0007.
 */
interface NetService {

    @GET("weather_mini")
    fun getWeather(@Query("city") city: String): Call<Weather> //获得天气
}