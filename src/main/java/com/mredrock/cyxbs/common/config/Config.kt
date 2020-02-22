package com.mredrock.cyxbs.common.config

import java.io.File

/**
 * Created By jay68 on 2018/8/10.
 */

const val DIR = "/cyxbs"
const val DIR_PHOTO = "/cyxbs/photo"
const val DIR_FILE = "/cyxbs/file"
const val PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned"
const val PREF_USER_LOGIN_ALREADY = "login_already"
const val STATE_SELECTED_POSITION = "selected_navigation_drawer_position"

val dataFilePath = android.os.Environment.getExternalStorageDirectory().toString() + "/" + "Android/data/com.mredrock.cyxbs/"
val updateFilePath = android.os.Environment.getExternalStorageDirectory().toString() + "/" + "Download/"
const val updateFilename = "com.mredrock.cyxbs.apk"
val updateFile get() = File(updateFilePath + updateFilename)

const val APP_WIDGET_CACHE_FILE_NAME = "AppWidgetCache.json"

//SharedPreferences key for encrypt version of user
const val SP_KEY_ENCRYPT_VERSION_USER = "encrypt_version_user"

//SharedPreferences value for encrypt version of user
const val USER_INFO_ENCRYPT_VERSION = 1

//User信息存储key
const val SP_KEY_USER_V2 = "cyxbsmobile_user_v2"

@Deprecated(message = "user类结构变动较大，放弃使用该key", replaceWith = ReplaceWith("SP_KEY_USER_V2", "com.mredrock.cyxbs.common.config.SP_KEY_USER_V2"))
const val SP_KEY_USER = "cyxbsmobile_user"

const val DEFAULT_PREFERENCE_FILENAME = "share_data"

//在课表上没课的地方显示备忘录
const val SP_SHOW_MODE = "showMode"

//连续签到每日提醒
const val SP_SIGN_REMIND = "signRemind"

const val APP_WEBSITE = "https://wx.idsbllp.cn/app/"

const val ABOUT_US_WEBSITE = "https://redrock.team/aboutus/"

//小控件课表
const val WIDGET_COURSE = "course_widget"
const val SP_WIDGET_NEED_FRESH = "sharepreference_widget_need_fresh"

// uri跳转打开应用
// QA
const val URI_HOST_QA = "/qa"
// question
const val URI_PATH_QA_QUESTION = "/question"
// answer
const val URI_PATH_QA_ANSWER = "/answer"

//课表辨别是查同学课表的key
const val OTHERS_STU_NUM = "others_stu_num"

//课表辨别是查老师课表的key
const val OTHERS_TEA_NUM = "others_tea_num"
const val OTHERS_TEA_NAME = "others_tea_name"

//没课约传递信息的key
const val STU_NUM_LIST = "stuNumList"
const val STU_NAME_LIST = "stuNameList"

//传递给CourseFragment页数的key
const val WEEK_NUM = "week_num"

//启动App时最先显示课表界面的Key
const val COURSE_SHOW_STATE = "course_show_state"

//通知课表是否直接加载的key
const val COURSE_DIRECT_LOAD = "direct_load"

//用来替代boolean类型的两种选择（Bundle传递bool类型的值会有默认的false，如果不想要false可以使用这个替代）
const val TRUE = "true"
const val FALSE = "false"

//邮问问题和回答
//从跳转到具体页面时需要以此为key
const val QUESTION_ID = "question_id"
const val ANSWER_ID = "answer_id"
//跳转邮问时，需要RequestBody的string，以此为key
const val QUESTION_REQUEST_BODY_DATA = "question_request_body"
const val ANSWER_REQUEST_BODY_DATA = "answer_request_body"

//课表版本
const val COURSE_VERSION = "course_version"