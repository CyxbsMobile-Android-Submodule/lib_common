package com.mredrock.cyxbs.common.service.manager

import com.alibaba.android.arouter.launcher.ARouter

/**
 * 对服务获取的封装，便于以后修改为其他依赖注入的框架（ARouter这方面性能稍弱），
 * 建议都通过该文件提供的方法获取服务，不采用@Autowired的方式
 * Created By jay68 on 2019/10/12.
 */

object ServiceManager {
    /**
     * 通过类型搜索对应服务，建议优先使用此方式
     */
    fun <T> getService(serviceClass: Class<T>) = ARouter.getInstance().navigation(serviceClass)

    /**
     * 通过服务名搜索服务，当同一种类型的服务有多个实现时建议使用该方式
     */
    fun <T> getService(serviceName: String) = ARouter.getInstance().build(serviceName).navigation() as T
}