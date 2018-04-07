package tqtsgj.bs.com.tianqitieshengguanjia.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import anet.channel.util.Utils.context
import com.umeng.message.PushAgent

/**
 * Created by Administrator on 2018/3/7 0007.
 */
abstract class BaseActivity : AppCompatActivity() {

    abstract fun setContentView(): Int
    abstract fun initView()
    abstract fun initData()
    abstract fun initOthers()
    abstract fun beforeClosing()

    public val mBaseContext: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PushAgent.getInstance(context).onAppStart()
        setContentView(setContentView())
        initView()
        initData()
        initOthers()
    }

    override fun onDestroy() {
        super.onDestroy()
        beforeClosing()
    }


    open fun <T> gotoActivity(clazz: Class<T>) {
        startActivity(Intent(this, clazz))
    }

    open fun <T> gotoActivityForResult(clazz: Class<T>, ID: Int) {
        startActivityForResult(Intent(this, clazz), ID)
    }
}