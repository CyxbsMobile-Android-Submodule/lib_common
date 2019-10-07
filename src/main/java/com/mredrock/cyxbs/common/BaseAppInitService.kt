package com.mredrock.cyxbs.common

import android.app.IntentService
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Build
import com.meituan.android.walle.WalleChannelReader
import com.mredrock.cyxbs.common.utils.CrashHandler
import com.mredrock.cyxbs.common.utils.LogUtils
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.umeng.message.IUmengRegisterCallback
import com.umeng.message.PushAgent
import com.umeng.message.inapp.InAppMessageManager
import com.umeng.socialize.PlatformConfig

/**
 * author: Fxymine4ever
 * time: 2019/9/28
 */

class BaseAppInitService : IntentService("BaseAppInitService") {

    companion object{
        const val ACTION_INIT_WHEN_APP_CREATE = "service.action.BASE_INIT"

        fun init(context: Context){
            val intent = Intent(context, BaseAppInitService::class.java)
            intent.action = ACTION_INIT_WHEN_APP_CREATE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(Int.MAX_VALUE, Notification())
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        intent?.let {
            val action = intent.action
            if(action == ACTION_INIT_WHEN_APP_CREATE){
                startInit()
            }
        }
    }

    private fun startInit(){
        CrashHandler.init(applicationContext)
        initUMeng()
    }

    private fun initUMeng() {
        val channel = WalleChannelReader.getChannel(applicationContext, "debug")
        UMConfigure.init(applicationContext, BuildConfig.UM_APP_KEY, channel, UMConfigure.DEVICE_TYPE_PHONE,
                BuildConfig.UM_PUSH_SECRET)
        MobclickAgent.setScenarioType(applicationContext, MobclickAgent.EScenarioType.E_UM_NORMAL)
        MobclickAgent.openActivityDurationTrack(false)
        //调试模式（推荐到umeng注册测试机，避免数据污染）
        UMConfigure.setLogEnabled(BuildConfig.DEBUG)
        //友盟推送服务的接入
        PushAgent.getInstance(BaseApp.context).onAppStart()
        val mPushAgent = PushAgent.getInstance(this)
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(object : IUmengRegisterCallback {
            override fun onSuccess(deviceToken: String) {
                //注册成功会返回deviceToken deviceToken是推送消息的唯一标志
                LogUtils.i("友盟注册", "注册成功：deviceToken：-------->  $deviceToken")
            }
            override fun onFailure(s: String, s1: String) {
                LogUtils.e("友盟注册", "注册失败：-------->  s:$s,s1:$s1")
            }
        })

        InAppMessageManager.getInstance(BaseApp.context).setInAppMsgDebugMode(true)
        initShare()
    }

    private fun initShare() {
        PlatformConfig.setSinaWeibo(BuildConfig.UM_SHARE_SINA_APP_KEY, BuildConfig.UM_SHARE_SINA_APP_SECRET, "http://hongyan.cqupt.edu.cn/app/")
        PlatformConfig.setQQZone(BuildConfig.UM_SHARE_QQ_ZONE_APP_ID, BuildConfig.UM_SHARE_QQ_ZONE_APP_SECRET)
    }

}