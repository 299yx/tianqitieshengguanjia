package tqtsgj.bs.com.tianqitieshengguanjia.base

import android.app.Application
import android.content.Context
import android.util.Log
import com.umeng.commonsdk.UMConfigure
import com.umeng.message.IUmengRegisterCallback
import com.umeng.message.PushAgent
import tqtsgj.bs.com.tianqitieshengguanjia.utils.LogUitls
import android.widget.Toast
import com.umeng.message.entity.UMessage
import com.umeng.message.UmengNotificationClickHandler



/**
 * Created by Administrator on 2018/3/7 0007.
 */
class BaseApp : Application() {

    public val appName = "天气贴身管家"
    private var deviceToken: String? = null
    lateinit var mPushAgent: PushAgent

    override fun onCreate() {
        super.onCreate()
        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, "4eaf74d8db38b36712a9736e21376a96")

        mPushAgent = PushAgent.getInstance(this)
        mPushAgent.register(object : IUmengRegisterCallback {
            override fun onSuccess(p0: String?) {
                deviceToken = p0
                LogUitls.e("放回推送token$deviceToken")
                Log.d("天气管家","$deviceToken")
            }

            override fun onFailure(p0: String?, p1: String?) {
                LogUitls.e("友盟注册失败$p0 。。$p1")
            }
        })
        val notificationClickHandler = object : UmengNotificationClickHandler() {

            override fun dealWithCustomAction(context: Context, msg: UMessage) {
                Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show()
            }
        }
        mPushAgent.notificationClickHandler = notificationClickHandler
    }

    public fun getDeviceToken(): String {
        return deviceToken ?: ""
    }

    public fun getPushAgent(): PushAgent {
        return mPushAgent
    }
}