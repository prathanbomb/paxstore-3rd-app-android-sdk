package com.pax.android.demoapp

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

//to store result cross process, replace shared preferences.
class SPUtil {
    fun setString(tag: String?, value: String?) {
        BaseApplication.appPreferences!!.put(tag!!, value)
    }

    fun getString(tag: String?): String? {
        return BaseApplication.appPreferences!!.getString(tag!!, null)
    }

    /**
     * save List
     *
     * @param tag
     * @param datalist
     */
    fun <T> setDataList(tag: String?, datalist: List<T>?) {
        if (datalist.isNullOrEmpty()) return
        val gson = Gson()
        //转换成json数据，再保存
        val strJson = gson.toJson(datalist)
        BaseApplication.appPreferences!!.put(tag!!, strJson)
    }

    /**
     * get List
     *
     * @param tag
     * @return
     */
    fun <T> getDataList(tag: String?): List<T> {
        var datalist: List<T> = ArrayList()
        val strJson = BaseApplication.appPreferences!!.getString(tag!!, null) ?: return datalist
        val gson = Gson()
        datalist = gson.fromJson(strJson, object : TypeToken<List<T>?>() {}.type)
        return datalist
    }
}