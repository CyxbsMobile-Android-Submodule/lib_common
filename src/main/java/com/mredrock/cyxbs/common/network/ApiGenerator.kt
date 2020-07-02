package com.mredrock.cyxbs.common.network

import com.mredrock.cyxbs.common.BuildConfig
import com.mredrock.cyxbs.common.config.END_POINT_REDROCK
import com.mredrock.cyxbs.common.service.ServiceManager
import com.mredrock.cyxbs.common.service.account.IAccountService
import com.mredrock.cyxbs.common.service.account.IUserStateService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by AceMurder on 2018/1/24.
 */
object ApiGenerator {
    private const val DEFAULT_TIME_OUT = 30

    private var retrofit: Retrofit
    private var commonRetrofit: Retrofit
    private var okHttpClient: OkHttpClient
    private var token = ""
    private var refreshToken = ""
    private val retrofitMap by lazy { HashMap<Int, Retrofit>() }

    init {
        val accountService = ServiceManager.getService(IAccountService::class.java)
        accountService.getVerifyService().addOnStateChangedListener {
            when (it) {
                IUserStateService.UserState.LOGIN -> {
                    token = accountService.getUserTokenService().getToken()
                    refreshToken = accountService.getUserTokenService().getRefreshToken()
                }
                else -> {
                    //不用操作
                }
            }
        }
        token = accountService.getUserTokenService().getToken()
        refreshToken = accountService.getUserTokenService().getRefreshToken()
        okHttpClient = configureOkHttp(OkHttpClient.Builder())
        retrofit = Retrofit.Builder()
                .baseUrl(END_POINT_REDROCK)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        commonRetrofit = commonApiService()
    }

    private fun configureOkHttp(builder: OkHttpClient.Builder): OkHttpClient = configureOkHttp(builder = builder, function = null)

    private fun configureOkHttp(builder: OkHttpClient.Builder, function: ((builder: OkHttpClient.Builder) -> OkHttpClient.Builder)?): OkHttpClient {
        builder.apply {
            connectTimeout(DEFAULT_TIME_OUT.toLong(), TimeUnit.SECONDS)
            interceptors().add(Interceptor {
                /**
                 * 所有请求添加token到header
                 * 在外面加一层判断，用于token未过期时，能够异步请求，不用阻塞在checkRefresh()
                 * 如果有更好方式再改改
                 */
                when {
                    refreshToken.isEmpty() || token.isEmpty() -> {
                        token = ServiceManager.getService(IAccountService::class.java).getUserTokenService()?.getToken()
                        refreshToken = ServiceManager.getService(IAccountService::class.java).getUserTokenService()?.getRefreshToken()
                        it.proceed(it.request().newBuilder().header("Authorization", "Bearer $token").build())
                    }
                    isTokenExpired() -> {
                        checkRefresh(it)
                    }
                    else -> {
                        it.proceed(it.request().newBuilder().header("Authorization", "Bearer $token").build())
                    }
                } as Response
            })
        }
        function?.invoke(builder)
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(logging)
        }
        return builder.build()
    }

    @Synchronized
    private fun checkRefresh(chain: Interceptor.Chain): Response? {
        var response = chain.proceed(chain.request().newBuilder().header("Authorization", "Bearer $token").build())
        /**
         * 刷新token条件设置为，已有refreshToken，并且已经过期，也可以后端返回特定到token失效code
         * 当第一个过期token请求接口后，改变token和refreshToken，防止同步refreshToken失效
         * 之后进入该方法的请求，token已经刷新
         */
        if (refreshToken.isNotEmpty() && isTokenExpired()) {
            ServiceManager.getService(IAccountService::class.java).getVerifyService().refresh(
                    onError = {
                        response.close()
                    },
                    action = { s: String ->
                        response.close()
                        response = chain.run { proceed(chain.request().newBuilder().header("Authorization", "Bearer $s").build()) }
                    }
            )
        }
        return response
    }

    private fun commonApiService(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(END_POINT_REDROCK)
                .client(OkHttpClient().newBuilder().apply {
                    connectTimeout(DEFAULT_TIME_OUT.toLong(), TimeUnit.SECONDS)
                    if (BuildConfig.DEBUG) {
                        val logging = HttpLoggingInterceptor()
                        logging.level = HttpLoggingInterceptor.Level.BODY
                        addInterceptor(logging)
                    }
                }.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    private fun isTokenExpired() = ServiceManager.getService(IAccountService::class.java).getVerifyService().isExpired()
    fun <T> getApiService(clazz: Class<T>) = retrofit.create(clazz)

    fun <T> getApiService(retrofit: Retrofit, clazz: Class<T>) = retrofit.create(clazz)

    /**
     *这个方法提供对OkHttp和Retrofit进行自定义的操作，通过uniqueNum可以实现不同子模块中的复用，而不需要在通用模块中添加。
     *默认会完成部分基础设置，此处传入的两个lambda在基础设置之后执行，可以覆盖基础设置。
     * 需要先进行注册才能使用。
     *@throws IllegalAccessException
     */
    fun <T> getApiService(uniqueNum: Int, clazz: Class<T>): T {

        if (retrofitMap[uniqueNum] == null) {
            throw IllegalArgumentException()
        }
        return retrofitMap[uniqueNum]!!.create(clazz)
    }
    /*
     *通过此方法对配置进行注册，之后即可使用uniqueNum获取service。
     */
    fun registerNetSettings(uniqueNum:Int,retrofitConfig: ((Retrofit.Builder) -> Retrofit.Builder)? = null, okHttpClientConfig: ((OkHttpClient.Builder) -> OkHttpClient.Builder)? = null){
        retrofitMap[uniqueNum] = Retrofit.Builder()
                .baseUrl(END_POINT_REDROCK)
                .apply {
                    retrofitConfig?.invoke(this)
                }
                .client(configureOkHttp(OkHttpClient.Builder(), okHttpClientConfig))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    fun <T> getCommonApiService(clazz: Class<T>) = commonRetrofit.create(clazz)

}