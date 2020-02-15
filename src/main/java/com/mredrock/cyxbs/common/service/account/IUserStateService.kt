package com.mredrock.cyxbs.common.service.account

import android.content.Context
import androidx.annotation.WorkerThread

interface IUserStateService {
    enum class UserState {
        LOGIN, NOT_LOGIN, EXPIRED
    }

    @WorkerThread
    @Throws(Exception::class)
    fun login(context: Context, uid: String, passwd: String)

    fun logout(context: Context)

    fun refresh(onError: () -> Unit = {}, action: (token: String, refreshToken: String) -> Unit = { s: String, s1: String -> })

    fun isLogin(): Boolean

    fun isExpired(): Boolean

    fun addOnStateChangedListener(listener: (state: UserState) -> Unit)

    fun removeStateChangedListener(listener: (state: UserState) -> Unit)

    fun removeAllStateListeners()
}