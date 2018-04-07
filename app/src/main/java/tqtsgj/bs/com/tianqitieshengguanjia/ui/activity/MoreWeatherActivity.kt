package tqtsgj.bs.com.tianqitieshengguanjia.ui.activity

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.layout_more_weather.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tqtsgj.bs.com.tianqitieshengguanjia.Constant
import tqtsgj.bs.com.tianqitieshengguanjia.R
import tqtsgj.bs.com.tianqitieshengguanjia.base.BaseActivity
import tqtsgj.bs.com.tianqitieshengguanjia.entity.EventBus.EvbGetAllWeather
import tqtsgj.bs.com.tianqitieshengguanjia.entity.ItemWeather
import tqtsgj.bs.com.tianqitieshengguanjia.entity.Weather
import tqtsgj.bs.com.tianqitieshengguanjia.ui.listener.NetService
import tqtsgj.bs.com.tianqitieshengguanjia.utils.NetUitls
import tqtsgj.bs.com.tianqitieshengguanjia.utils.RegexUitls

/**
 * Created by Administrator on 2018/3/8 0008.
 *
 */
class MoreWeatherActivity : BaseActivity() {

    var mDates = ArrayList<ItemWeather>()
    private var mAdapat = RecycleViewAdapter()

    override fun setContentView(): Int {
        return R.layout.layout_more_weather
    }

    override fun initView() {
        EventBus.getDefault().register(this)
        tv_back.setOnClickListener {
            finish()
        }
        rv_all_weather.layoutManager = LinearLayoutManager(this)
        rv_all_weather.adapter = mAdapat
        rl_close.setOnClickListener {
            getWeather()
        }
    }

    override fun initData() {
        getWeather()
    }

    override fun initOthers() {

    }

    override fun beforeClosing() {
        EventBus.getDefault().unregister(this)
    }

    /**
     * 获得当前温度
     */
    private fun getWeather() {
        NetUitls.getRetrofit(Constant.WeatherURl, NetService::class.java).getWeather(intent.getStringExtra(Constant.CITY)).enqueue(object : Callback<Weather> {
            override fun onResponse(call: Call<Weather>?, response: Response<Weather>?) {
                mDates.clear()
                response?.body()?.data?.forecast?.forEach { i ->
                    mDates.add(ItemWeather(i.date, i.type, i.high, i.low, i.fengli, i.fengxiang))
                }
                EventBus.getDefault().post(EvbGetAllWeather(true))
            }

            override fun onFailure(call: Call<Weather>?, t: Throwable?) {
                EventBus.getDefault().post(EvbGetAllWeather(false))
            }
        })
    }

    /**
     * 包装类
     */
    inner class RecycleViewAdapter : RecyclerView.Adapter<RecycleViewAdapter.MyHolder>() {
        override fun onBindViewHolder(holder: MyHolder?, position: Int) {
            holder?.mTvData?.text = mDates[position].data
            holder?.mTvYQ?.text = mDates[position].yq
            holder?.mTvHot?.text = mDates[position].hot
            holder?.mTvCold?.text = mDates[position].cold
            holder?.mTvLevel?.text = RegexUitls.compile(mDates[position].level, RegexUitls.CDATA)
            holder?.mTvFX?.text = mDates[position].xq
        }

        override fun getItemCount(): Int {
            return mDates.size
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyHolder {
            return MyHolder(LayoutInflater.from(mBaseContext).inflate(R.layout.item_weather, parent, false))
        }

        inner class MyHolder(view: View) : RecyclerView.ViewHolder(view) {
            var mTvData = view.findViewById<TextView>(R.id.tv_data)
            var mTvYQ = view.findViewById<TextView>(R.id.tv_yq)
            var mTvHot = view.findViewById<TextView>(R.id.tv_hot)
            var mTvCold = view.findViewById<TextView>(R.id.tv_cold)
            var mTvLevel = view.findViewById<TextView>(R.id.tv_level)
            var mTvFX = view.findViewById<TextView>(R.id.tv_fx)
        }
    }

    /***************EventBus***************************/
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun setRecycle(evbGetAllWeather: EvbGetAllWeather) {
        if (evbGetAllWeather.isSuccess) {
            rl_close.visibility = View.GONE
            rv_all_weather.visibility = View.VISIBLE
            mAdapat.notifyDataSetChanged()
        } else {
            rl_close.visibility = View.VISIBLE
            rv_all_weather.visibility = View.GONE
            Toast.makeText(this, "网络异常，请检查网络", Toast.LENGTH_LONG).show()
            //todo 显示错误界面
        }
    }
}