package tqtsgj.bs.com.tianqitieshengguanjia.ui.activity

import com.umeng.message.inapp.InAppMessageManager
import com.umeng.message.inapp.UmengSplashMessageActivity

/**
 * Created by Administrator on 2018/3/17 0017.
 * 欢迎页面
 */
class SplashActivity : UmengSplashMessageActivity() {

    lateinit var mInAppMessageManager: InAppMessageManager

    override fun onCustomPretreatment(): Boolean {
        mInAppMessageManager = InAppMessageManager.getInstance(this)
        mInAppMessageManager.setInAppMsgDebugMode(false)
        mInAppMessageManager.setMainActivityPath("tqtsgj.bs.com.tianqitieshengguanjia.ui.activity.MainActivity")
        return super.onCustomPretreatment()
    }
}