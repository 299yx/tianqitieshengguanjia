package tqtsgj.bs.com.tianqitieshengguanjia.ui.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.layout_set_device.*
import org.greenrobot.eventbus.EventBus
import tqtsgj.bs.com.tianqitieshengguanjia.R
import tqtsgj.bs.com.tianqitieshengguanjia.base.BaseActivity
import tqtsgj.bs.com.tianqitieshengguanjia.entity.Device
import tqtsgj.bs.com.tianqitieshengguanjia.entity.EventBus.EvbNewDevice
import tqtsgj.bs.com.tianqitieshengguanjia.utils.LogUitls
import java.util.ArrayList

/**
 * Created by Administrator on 2018/3/9 0009
 * 搜索设备界面.
 */
class SetDeviceActivity : BaseActivity() {

    val mData = ArrayList<Device>()
    private var mAdapter = MyAdapter()
    var IsFiding: Boolean = true
    private val REQUEST_PERMISSION_ACCESS_LOCATION = 1
    private val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val filter = IntentFilter()
    var IsSecondFind = false
    /**
     * 接受蓝牙状态变化广播的类
     * 已处理（寻找到可用设备、搜索结束）
     */
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            LogUitls.v(action.toString())
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    LogUitls.v(device.name + "\n" + device.address)
                    mData.add(Device(device.name ?: "未知设备", device.address))

                    mAdapter.notifyDataSetChanged()//刷新数据列表
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    pb_finding.visibility = View.GONE//隐藏进度条
                    ib_control.background = resources.getDrawable(R.mipmap.find, null)
                    IsFiding = false
                    LogUitls.v("搜索结束")
                }
            }
        }
    }

    override fun setContentView(): Int {
        return R.layout.layout_set_device
    }

    override fun initView() {
        tv_back.setOnClickListener {
            finish()
        }
        rv_devices.layoutManager = LinearLayoutManager(this)
        rv_devices.adapter = mAdapter
        ib_control.setOnClickListener { i ->
            if (IsFiding) {
                //正在寻找
                mBluetoothAdapter.cancelDiscovery()
                pb_finding.visibility = View.GONE
                i.background = resources.getDrawable(R.mipmap.find, null)
                IsFiding = false
            } else {
                //停止寻找
                startFind()
                pb_finding.visibility = View.VISIBLE
                i.background = resources.getDrawable(R.mipmap.close, null)
                IsFiding = true
            }
        }
    }

    override fun initData() {
        startFind()
    }

    override fun initOthers() {

    }

    override fun beforeClosing() {
        if (mBluetoothAdapter.isDiscovering) {
            mBluetoothAdapter.cancelDiscovery()
        }
        LogUitls.v("取消广播")
        unregisterReceiver(mReceiver)
    }

    /**
     * 开始寻找蓝牙设备
     */
    private fun startFind() {
        if (Build.VERSION.SDK_INT >= 23) {
            val checkAccessFinePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            if (checkAccessFinePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_ACCESS_LOCATION)
                LogUitls.e("没有权限需要请求权限")
                return
            }
            LogUitls.e("已经有权限")
            findDevice()
        } else {
            findDevice()
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
                    findDevice()
                } else {
                    LogUitls.e("没有定位权限，再次请求！！!")
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_ACCESS_LOCATION)
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * 搜索可用蓝牙(不需要权限验证部分部分)
     */
    private fun findDevice() {
        mData.clear()
        LogUitls.d("开始搜索")
        if (!IsSecondFind) {
            LogUitls.e("第一次寻找，注册广播")
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            filter.addAction(BluetoothDevice.ACTION_FOUND)
            registerReceiver(mReceiver, filter)
            IsSecondFind = true
        }
        mBluetoothAdapter.startDiscovery()
    }

    /**
     * 包装类
     */
    inner class MyAdapter : RecyclerView.Adapter<MyAdapter.MyHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyHolder {
            return MyHolder(LayoutInflater.from(mBaseContext).inflate(R.layout.item_mac, parent, false))
        }

        override fun getItemCount(): Int {
            return mData.size
        }

        override fun onBindViewHolder(holder: MyHolder?, position: Int) {
            holder?.mTvId?.text = mData[position].id
            holder?.mTvMac?.text = mData[position].mac
            holder?.mLlItem?.setOnClickListener {
                EventBus.getDefault().post(EvbNewDevice(mData[position].id,mData[position].mac))
                finish()
            }
        }

        inner class MyHolder(view: View) : RecyclerView.ViewHolder(view) {
            var mTvId = view.findViewById<TextView>(R.id.tv_device_id)
            var mTvMac = view.findViewById<TextView>(R.id.tv_device_mac)
            var mLlItem = view.findViewById<LinearLayout>(R.id.ll_item)
        }
    }
}