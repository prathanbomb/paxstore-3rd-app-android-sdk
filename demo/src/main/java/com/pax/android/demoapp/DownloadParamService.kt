package com.pax.android.demoapp

import android.app.IntentService
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.pax.market.android.app.sdk.NotificationUtils
import com.pax.market.android.app.sdk.StoreSdk
import com.pax.market.api.sdk.java.base.constant.ResultCode
import com.pax.market.api.sdk.java.base.dto.DownloadResultObject
import com.pax.market.api.sdk.java.base.exception.NotInitException
import com.pax.market.api.sdk.java.base.exception.ParseXMLException
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by zcy on 2016/12/2 0002.
 */
class DownloadParamService : IntentService("DownloadParamService") {
    private var spUtil: SPUtil? = null
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onHandleIntent(intent: Intent?) {
        spUtil = SPUtil()
        //todo Specifies the download path for the parameter file, you can replace the path to your app's internal storage for security.
        saveFilePath = "$filesDir/YourPath/"
        //show downloading info in main page
        updateUI(DemoConstants.DOWNLOAD_STATUS_START)
        //todo Call SDK API to download parameter files into your specific directory,
        var downloadResult: DownloadResultObject? = null
        try {
            Log.i(TAG, "call sdk API to download parameter")
            downloadResult = StoreSdk.getInstance().paramApi().downloadParamToPath(application.packageName, BuildConfig.VERSION_CODE, saveFilePath)
            Log.i(TAG, downloadResult.toString())
        } catch (e: NotInitException) {
            Log.e(TAG, "e:$e")
        }
        //                businesscode==0, means download successful, if not equal to 0, please check the return message when need.
        if (downloadResult != null && downloadResult.businessCode == ResultCode.SUCCESS.code) {
            Log.i(TAG, "download successful.")
            //todo start to add your own logic.
//below is only for demo
            readDataToDisplay()
        } else { //todo check the Error Code and Error Message for fail reason
            Log.e(TAG, "ErrorCode: " + downloadResult!!.businessCode + "ErrorMessage: " + downloadResult.message)
            //update download fail info in main page for Demo
            spUtil!!.setString(DemoConstants.PUSH_RESULT_BANNER_TITLE, DemoConstants.DOWNLOAD_FAILED)
            spUtil!!.setString(DemoConstants.PUSH_RESULT_BANNER_TEXT, "Your push parameters file task failed at " + sdf.format(Date()) + ", please check error log.")
            updateUI(DemoConstants.DOWNLOAD_STATUS_FAILED)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        NotificationUtils.showForeGround(this, "Downloading params")
        return super.onStartCommand(intent, flags, startId)
    }

    private fun readDataToDisplay() {
        spUtil!!.setString(DemoConstants.PUSH_RESULT_BANNER_TITLE, DemoConstants.DOWNLOAD_SUCCESS)
        // get specific display data resource <File>sys_cap.p</File>
        val parameterFile = displayFile
        //save data for display in main page for demo
        saveDisplayFileDataToSp(parameterFile)
        //update successful info in main page for Demo
        updateUI(DemoConstants.DOWNLOAD_STATUS_SUCCESS)
    }

    /**
     * save data for display in main page for demo
     *
     * @param parameterFile data resource
     */
    private fun saveDisplayFileDataToSp(parameterFile: File?) {
        if (parameterFile != null) {
            val bannerTextValue = ("Your push parameters  - " + parameterFile.name
                    + " have been successfully pushed at " + sdf.format(Date()) + ".")
            val bannerSubTextValue = "Files are stored in " + parameterFile.path
            Log.i(TAG, "run=====: $bannerTextValue")
            //save result for demo display
            spUtil!!.setString(DemoConstants.PUSH_RESULT_BANNER_TEXT, bannerTextValue)
            spUtil!!.setString(DemoConstants.PUSH_RESULT_BANNER_SUBTEXT, bannerSubTextValue)
            val datalist = getParameters(parameterFile)
            //save result for demo display
            spUtil!!.setDataList(DemoConstants.PUSH_RESULT_DETAIL, datalist)
        } else {
            Log.i(TAG, "parameterFile is null ")
            spUtil!!.setString(DemoConstants.PUSH_RESULT_BANNER_TEXT, "Download file not found. This demo only accept parameter file with name 'sys_cap.p'")
        }
    }//todo Noted. this is for demo only, here hard code the xml name to "sys_cap.p". this demo will only parse with the specified file name

    /**
     * get specific display data resource <File>sys_cap.p</File>
     *
     * @return specific file, return null if not exists
     */
    private val displayFile: File?
        private get() {
            var parameterFile: File? = null
            val filelist = File(saveFilePath).listFiles()
            if (filelist != null && filelist.size > 0) {
                for (f in filelist) { //todo Noted. this is for demo only, here hard code the xml name to "sys_cap.p". this demo will only parse with the specified file name
                    if (DemoConstants.DOWNLOAD_PARAM_FILE_NAME == f.name) {
                        parameterFile = f
                    }
                }
            }
            return parameterFile
        }

    private fun getParameters(parameterFile: File): List<Map<String, Any>>? {
        try { //parse the download parameter xml file for display.
            val datalist: MutableList<Map<String, Any>> = ArrayList()
            //todo call API to parse xml
            var resultMap: LinkedHashMap<String, String>? = null
            resultMap = if (isJsonFile(parameterFile)) {
                StoreSdk.getInstance().paramApi().parseDownloadParamJsonWithOrder(parameterFile)
            } else {
                StoreSdk.getInstance().paramApi().parseDownloadParamXmlWithOrder(parameterFile)
            }
            if (resultMap != null && resultMap.size > 0) { //convert result map to list for ListView display.
                for ((key, value) in resultMap) {
                    val map = HashMap<String, Any>()
                    map["label"] = key
                    map["value"] = value
                    datalist.add(map)
                }
            }
            return datalist
        } catch (e: JsonParseException) {
            e.printStackTrace()
        } catch (e: NotInitException) {
            e.printStackTrace()
        } catch (e: ParseXMLException) {
            e.printStackTrace()
        }
        return null
    }

    private fun isJsonFile(parameterFile: File?): Boolean {
        return if (parameterFile == null) {
            false
        } else try {
            val jsonStr = FileUtils.readFileToString(parameterFile)
            val jsonElement = JsonParser().parse(jsonStr)
            true
        } catch (e: JsonSyntaxException) {
            false
        } catch (e1: IOException) {
            false
        }
    }

    /**
     * notify MainActivity to display downloaded files, just for demo display
     */
    private fun updateUI(stateCode: Int) {
        val intent = Intent(DemoConstants.UPDATE_VIEW_ACTION)
        intent.putExtra(DemoConstants.DOWNLOAD_RESULT_CODE, stateCode)
        sendBroadcast(intent)
    }

    companion object {
        private val TAG = DownloadParamService::class.java.simpleName
        var saveFilePath: String? = null
        private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
    }
}