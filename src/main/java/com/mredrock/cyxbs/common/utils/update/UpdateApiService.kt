package com.mredrock.cyxbs.common.utils.update

import com.mredrock.cyxbs.common.bean.UpdateInfo
import io.reactivex.Observable
import retrofit2.http.GET

/**
 * Create By Hosigus at 2019/5/11
 */
interface UpdateApiService {
    //todo
    @GET("/app/cyxbsAppUpdate.xml.bk")
    fun getUpdateInfo(): Observable<UpdateInfo>
}