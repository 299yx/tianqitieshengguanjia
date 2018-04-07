package tqtsgj.bs.com.tianqitieshengguanjia.ui.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import kotlinx.android.synthetic.main.layout_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tqtsgj.bs.com.tianqitieshengguanjia.Constant
import tqtsgj.bs.com.tianqitieshengguanjia.R
import tqtsgj.bs.com.tianqitieshengguanjia.base.BaseActivity
import tqtsgj.bs.com.tianqitieshengguanjia.entity.EventBus.*
import tqtsgj.bs.com.tianqitieshengguanjia.entity.Weather
import tqtsgj.bs.com.tianqitieshengguanjia.ui.listener.NetService
import tqtsgj.bs.com.tianqitieshengguanjia.utils.LogUitls
import tqtsgj.bs.com.tianqitieshengguanjia.utils.NetUitls
import tqtsgj.bs.com.tianqitieshengguanjia.utils.SharePreferenesUitls
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

/**
 * Created by Administrator on 2018/3/7 0007.
 * 主界面
 */
class MainActivity : BaseActivity() {

    private val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_PERMISSION_ACCESS_LOCATION = 2
    lateinit var mMAC: String
    lateinit var mID: String
    private var mIsConnection = false
    var mOpen = true
    var IsOpen = false
    /**百度本地服务器*/
    var mLocationClient: LocationClient? = null
    var myListener = MyLocationListener()
    /*获取的当前城市*/
    var city: String? = null


    override fun setContentView(): Int {
        return R.layout.layout_main
    }

    override fun initView() {
        EventBus.getDefault().register(this)
        //因为数据需在控件之前初始化
        mMAC = SharePreferenesUitls.getSharePreferenes(mBaseContext, Constant.Constant).getString(Constant.MAC, "")
        LogUitls.v("获取的Mac：" + mMAC)
        mID = SharePreferenesUitls.getSharePreferenes(mBaseContext, Constant.Constant).getString(Constant.ID, "")
        LogUitls.v("获取的id：" + mID)

        val echo = mID + '\n' + mMAC
        tv_device_id.text = echo

        /**更改跳转文字*/
        if (mMAC != "") {
            tv_to_add_device.text = "更改设备>>"
        }
        /**查看更多*/
        tv_watch_over.setOnClickListener {
            if (city == null) {
                Toast.makeText(mBaseContext, "地址初始化失败!!", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(mBaseContext, MoreWeatherActivity::class.java)
                intent.putExtra(Constant.CITY, city)
                startActivity(intent)
            }
        }
        /**设置*/
        tv_set.setOnClickListener {
            gotoActivity(SetActivity::class.java)
        }
        /**刷新天气*/
        ib_refresh.setOnClickListener {
            ib_refresh.visibility = View.GONE
            pb_refresh.visibility = View.VISIBLE
            if (city != null) {
                getWeather(city!!)
            } else {
                getJingWei()
                Toast.makeText(this, "初始化地址失败", Toast.LENGTH_SHORT).show()
                ib_refresh.visibility = View.VISIBLE
                pb_refresh.visibility = View.GONE
            }
        }
        /**绑定设备*/
        tv_to_add_device.setOnClickListener {
            if (IsOpen) {
                showDialog()
                return@setOnClickListener
            }
            gotoActivity(SetDeviceActivity::class.java)
        }
        /**连接设备和断开设备*/
        bn_control.setOnClickListener {
            if (mIsConnection) {
                LogUitls.v("断开连接")
                mOpen = false

            } else {
                if (mMAC == "") {
                    Toast.makeText(mBaseContext, "请先绑定设备", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else if (!mBluetoothAdapter.isEnabled) {
                    Toast.makeText(mBaseContext, "蓝牙开启中，稍后再试", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                LogUitls.v("尝试连接")
                bn_control.text = "连接中..."
                bn_control.isClickable = false
                MainThread().start()
            }
        }
    }

    override fun initData() {
    }

    override fun initOthers() {
        setUp()
        getJingWei()

    }

    override fun beforeClosing() {
        mOpen = false
        closeBluetooth()
        EventBus.getDefault().unregister(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            showDialogBeforeClose()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 百度地图相关初始化
     * */
    fun initOption() {
        val mOption = LocationClientOption()
        mOption.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        mOption.setCoorType("bd09ll")
        mOption.setScanSpan(1000)
        mOption.isOpenGps = true
        mOption.isLocationNotify = true
        mOption.setIgnoreKillProcess(false)
        mOption.setIsNeedAddress(true)
        mOption.setWifiCacheTimeOut(5 * 60 * 1000)
        mOption.setEnableSimulateGps(false)
        mLocationClient?.locOption = mOption
    }

    /**
     * 如果更改设备时，外设正在运行，对话框提示
     */
    private fun showDialog() {
        AlertDialog.Builder(mBaseContext)
                .setTitle("提示")
                .setMessage("该操作需要停止当前设备连接,是否断开？")
                .setPositiveButton("是") { dialog, which ->
                    mOpen = false
                    gotoActivity(SetDeviceActivity::class.java)
                }
                .setNegativeButton("算了", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                    }

                }).create()
                .show()
    }

    /**
     * 关闭前对话框
     */
    private fun showDialogBeforeClose() {
        AlertDialog.Builder(mBaseContext)
                .setTitle("提示")
                .setMessage("确定退出应用?")
                .setPositiveButton("是") { dialog, which ->
                    finish()
                }
                .setNegativeButton("再玩再玩", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                    }

                }).create()
                .show()
    }

    /**
     * 开启权限的回调
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                when (resultCode) {
                    RESULT_OK -> {
                    }
                    RESULT_CANCELED -> {
                        Toast.makeText(mBaseContext, "没有权限,自动退出程序", Toast.LENGTH_LONG).show()
                        finish()
                    }

                }
            }
        }
    }

    /**
     *查看手机是否支持蓝牙，并开启蓝牙
     */
    private fun setUp() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "未知异常", Toast.LENGTH_SHORT).show()
            finish()
        }
//请求开启蓝牙
        if (!mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            LogUitls.v("正在请求打开蓝牙")
            return
        }
    }

    /**
     * 关闭蓝牙设备
     */
    private fun closeBluetooth() {
        LogUitls.v("关闭蓝牙")
        mBluetoothAdapter?.disable()
    }

    /**
     * 获得当前温度
     */
    private fun getWeather(city: String) {
        NetUitls.getRetrofit(Constant.WeatherURl, NetService::class.java).getWeather(city).enqueue(object : Callback<Weather> {
            override fun onResponse(call: Call<Weather>?, response: Response<Weather>?) {
                EventBus.getDefault().post(EvbInitWeather(response?.body()?.data?.wendu.toString(), response?.body()?.data?.ganmao.toString(), true))
            }

            override fun onFailure(call: Call<Weather>?, t: Throwable?) {
                EventBus.getDefault().post(EvbInitWeather("", "", false))
            }
        })
    }

    /**
     * 连接进程
     */
    inner class MainThread : Thread() {
        lateinit var createCommSocketToServiceRecord: BluetoothSocket
        override fun run() {
            super.run()
            try {
                LogUitls.w("目标设备：$mID $mMAC")
                val remoteDevice = mBluetoothAdapter.getRemoteDevice(mMAC)
                createCommSocketToServiceRecord = remoteDevice.createRfcommSocketToServiceRecord(UUID.fromString(Constant.mUUID))
                createCommSocketToServiceRecord.connect()
            } catch (e: Exception) {
                e.stackTrace
                EventBus.getDefault().post(EvbCommCallBack(false))//失败回调
                LogUitls.e("设备连接失败")
                return
            }
            LogUitls.v("设备连接成功")
            EventBus.getDefault().post(EvbCommCallBack(true))
            try {
                val bufferedReader = BufferedReader(InputStreamReader(createCommSocketToServiceRecord.inputStream))
                while (mOpen) {
                    IsOpen = true
                    var s = bufferedReader.readLine()
                    if (s != null) {
                        var string: String = ""
                        for (byte in s.toByteArray()) {
                            var echo = string + byte.toString() + "."
                            string = echo
                            LogUitls.e(byte.toString())
                        }
                        EventBus.getDefault().post(EvbRefrashData(string))
                    }
                }
            } catch (e: Exception) {

            } finally {
                IsOpen = false
                createCommSocketToServiceRecord.close()
                EventBus.getDefault().post(EvbCommOpen())
            }
        }
    }

    /**
     * 获得城市
     **/
    fun getJingWei() {
        mLocationClient = LocationClient(application)
        initOption()
        mLocationClient?.registerLocationListener(myListener)
        if (Build.VERSION.SDK_INT >= 23) {
            val checkAccessFinePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            if (checkAccessFinePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_ACCESS_LOCATION)
                LogUitls.e("没有权限需要请求权限")
                return
            }
            LogUitls.e("已经有权限")
            mLocationClient?.start()
        } else {
            mLocationClient?.start()
        }
    }

    /**
     * 6.0动态权限的处理
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_ACCESS_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LogUitls.d("开启权限permission granted!")
                    mLocationClient?.start()
                } else {
                    LogUitls.e("没有定位权限，再次请求！！!")
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_ACCESS_LOCATION)
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * 百度获取回调
     */
    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(p0: BDLocation?) {
            city = p0?.city
            LogUitls.e(city ?: "地址获取失败")
            getWeather(city!!)
        }
    }

    /**************************EventBus*******************************/
    /**
     * 刷新天气
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun InitWeather(evbInitWeather: EvbInitWeather) {
        if (evbInitWeather.isSuccess) {
            tv_inter_wd.text = evbInitWeather.wd
            tv_inter_tip.text = evbInitWeather.tip
            tv_city.text = city
        } else {
            Toast.makeText(mBaseContext, "连接异常，请检查网络", Toast.LENGTH_SHORT).show()
        }
        pb_refresh.visibility = View.GONE
        ib_refresh.visibility = View.VISIBLE
        mLocationClient?.stop()
        mLocationClient = null
    }

    /**
     * 从绑定设备界面回调主界面的Mac
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun RefreshDevice(evbNewDevice: EvbNewDevice) {
        val string = evbNewDevice.id + '\n' + evbNewDevice.mac
        tv_device_id.text = string
        mMAC = evbNewDevice.mac
        mID = evbNewDevice.id
        val edit = SharePreferenesUitls.getSharePreferenes(mBaseContext, Constant.Constant).edit()
        edit.putString(Constant.ID, mID)
        edit.putString(Constant.MAC, mMAC)
        edit.apply()
        tv_to_add_device.text = "更改设备>>"
    }

    /**
     * 连接设备的回调
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun CommCallBack(evbCommCallBack: EvbCommCallBack) {
        bn_control.isClickable = true
        if (evbCommCallBack.callBack) {
            LogUitls.w("连接成功回调")
            mIsConnection = true
            bn_control.text = "设备连接成功，点击断开连接"
        } else {
            LogUitls.e("连接失败回调")
            Toast.makeText(mBaseContext, "检查目标设备是否开启", Toast.LENGTH_SHORT).show()
            bn_control.text = "连接设备"
        }
    }

    /**
     * 连接断开
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun commOpenCallBack(evbCommOpen: EvbCommOpen) {
        Toast.makeText(mBaseContext, "已与目标设备断开", Toast.LENGTH_SHORT).show()
        bn_control.text = "连接设备"
        tv_now_wd.text = "0.0°C"
        mIsConnection = false
        mOpen = true
    }

    /**
     * 更新温度
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refrash(evbRefreshData: EvbRefrashData) {
        var echo = stringHelp(evbRefreshData.s) + "°C"
        tv_now_wd.text = echo

    }

    private fun stringHelp(s: String): String {
        return s.substring(0, s.length - 1)
    }
}