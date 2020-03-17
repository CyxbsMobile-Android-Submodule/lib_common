package com.mredrock.cyxbs.common.ui

//import com.jude.swipbackhelper.SwipeBackHelper
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.View
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.alibaba.android.arouter.launcher.ARouter
import com.mredrock.cyxbs.common.BaseApp
import com.mredrock.cyxbs.common.R
import com.mredrock.cyxbs.common.component.JToolbar
import com.mredrock.cyxbs.common.config.MAIN_SPLASH
import com.mredrock.cyxbs.common.event.AskLoginEvent
import com.mredrock.cyxbs.common.event.LoginEvent
import com.mredrock.cyxbs.common.event.LoginStateChangeEvent
import com.mredrock.cyxbs.common.utils.LogUtils
import com.mredrock.cyxbs.common.utils.extensions.getDarkModeStatus
import com.umeng.analytics.MobclickAgent
import kotlinx.android.synthetic.main.common_toolbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 * Created By jay68 on 2018/8/9.
 */
abstract class BaseActivity : AppCompatActivity() {
    companion object {
        @JvmField
        val TAG: String = BaseActivity::class.java.simpleName
    }

    /**
     * service for umeng
     * set true if this activity consist of fragments.
     */
    abstract val isFragmentActivity: Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // todo 8.0 系统Bug 窗口透明导致限制竖屏闪退
//        SwipeBackHelper.onCreate(this)
//        SwipeBackHelper.getCurrentPage(this).setSwipeRelateEnable(true)
        initFlag()
        LogUtils.v(TAG, javaClass.name)
    }

    private fun initFlag() {

        when {
            Build.VERSION.SDK_INT >= 23 -> {
                if (BaseApp.isNightMode) {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                }else{
                    window.decorView.systemUiVisibility =
                            //亮色模式状态栏
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                                    //设置decorView的布局设置为全屏
                                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                    //维持布局稳定，不会因为statusBar和虚拟按键的消失而移动view位置
                                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                }
            }
            Build.VERSION.SDK_INT >= 21 -> {
                //设置decorView的布局设置为全屏，并维持布局稳定
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                window.statusBarColor = Color.TRANSPARENT
            }
        }
    }

    inline fun <reified T : Activity> startActivity(finish: Boolean = false, vararg params: Pair<String, Any?>) {
        if (finish) finish()
        startActivity<T>(*params)
    }

    val common_toolbar get() = toolbar

    var menu: Menu? = null
        private set

    @Deprecated(message = "废弃，请使用initWithSplitLine()", replaceWith = ReplaceWith("JToolbar.initWithSplitLine()", "com.mredrock.cyxbs.common.ui"))
    protected fun JToolbar.init(title: String,
                                @DrawableRes icon: Int = R.drawable.common_ic_back,
                                listener: View.OnClickListener? = View.OnClickListener { finish() }) {
        withSplitLine(true)
        initInternal(title, icon, listener)
    }

    private fun JToolbar.initInternal(title: String,
                                      @DrawableRes icon: Int = R.drawable.common_ic_back,
                                      listener: View.OnClickListener? = View.OnClickListener { finish() },
                                      titleOnLeft: Boolean = true) {
        this.title = title
        setSupportActionBar(this)
        setTitleLocationAtLeft(titleOnLeft)
        if (listener == null) {
            navigationIcon = null
        } else {
            setNavigationIcon(icon)
            setNavigationOnClickListener(listener)
        }
    }

    protected fun JToolbar.initWithSplitLine(title: String,
                                             withSplitLine: Boolean = true,
                                             @DrawableRes icon: Int = R.drawable.common_ic_back,
                                             listener: View.OnClickListener? = View.OnClickListener { finish() },
                                             titleOnLeft: Boolean = true) {
        setTitleLocationAtLeft(false)
        withSplitLine(withSplitLine)
        initInternal(title, icon, listener, titleOnLeft)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onLoginEvent(event: LoginEvent) {
        ARouter.getInstance().build("/main/login").navigation()
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    open fun onAskLoginEvent(event: AskLoginEvent) {
        val handler = Handler(mainLooper)
        handler.post {
            MaterialDialog.Builder(this)
                    .title("是否登录?")
                    .content(event.msg)
                    .positiveText("马上去登录")
                    .negativeText("我再看看")
                    .callback(object : MaterialDialog.ButtonCallback() {
                        override fun onPositive(dialog: MaterialDialog?) {
                            super.onPositive(dialog)
                            onLoginEvent(LoginEvent())
                        }

                        override fun onNegative(dialog: MaterialDialog?) {
                            super.onNegative(dialog)
                            dialog!!.dismiss()
                        }
                    }).show()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val r = super.onPrepareOptionsMenu(menu)
        this.menu = menu
        return r
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onLoginStateChangeEvent(event: LoginStateChangeEvent) {
        LogUtils.d("LoginStateChangeEvent", "in" + localClassName + "login state: " + event.loginState + "")
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (getDarkModeStatus() != BaseApp.isNightMode) {
            BaseApp.isNightMode = getDarkModeStatus()
            MaterialDialog.Builder(this)
                    .title("是否重启应用？")
                    .content("检测到你切换了显示模式，需要重启app才能完全正常显示")
                    .positiveText("立即重启")
                    .negativeText("稍后自己重启")
                    .autoDismiss(false)
                    .onPositive { _, _ ->
                        finishAffinity();
                        ARouter.getInstance().build(MAIN_SPLASH).navigation();
                        finish()
                    }.onNegative{ materialDialog, _ ->
                        materialDialog.dismiss()
                    }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
        if (!isFragmentActivity) {
            MobclickAgent.onPageStart(javaClass.name)
            LogUtils.d("UMStat", javaClass.name + " started")
        }
    }

    override fun onPause() {
        super.onPause()
        MobclickAgent.onPause(this)
        if (!isFragmentActivity) {
            MobclickAgent.onPageEnd(javaClass.name)
            LogUtils.d("UMStat", javaClass.name + " paused")
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

}
