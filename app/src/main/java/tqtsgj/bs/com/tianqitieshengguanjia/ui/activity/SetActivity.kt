package tqtsgj.bs.com.tianqitieshengguanjia.ui.activity

import kotlinx.android.synthetic.main.layout_set.*
import tqtsgj.bs.com.tianqitieshengguanjia.R
import tqtsgj.bs.com.tianqitieshengguanjia.base.BaseActivity

/**
 * Created by Administrator on 2018/3/9 0009.
 */
class SetActivity : BaseActivity() {

    override fun setContentView(): Int {
        return R.layout.layout_set
    }

    override fun initView() {
        tv_back.setOnClickListener {
            finish()
        }
        rl_about.setOnClickListener {
            gotoActivity(AboutActivity::class.java)
        }
    }

    override fun initData() {

    }

    override fun initOthers() {

    }

    override fun beforeClosing() {

    }
}