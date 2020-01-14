package com.pax.android.demoapp

import android.app.Application
import android.graphics.BitmapFactory
import android.os.Build
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import com.blankj.utilcode.util.LogUtils
import com.pax.market.android.app.sdk.BaseApiService
import com.pax.market.android.app.sdk.Notifications
import com.pax.market.android.app.sdk.StoreSdk
import com.pax.market.api.sdk.java.base.exception.NotInitException
import net.grandcentrix.tray.AppPreferences
import java.util.*

/**
 * Created by fojut on 2017/8/24.
 */
class BaseApplication : Application() {
    private var isReadyToUpdate = true
    override fun onCreate() {
        super.onCreate()
        //initial the SDK
        initPaxStoreSdk()
        appPreferences = AppPreferences(applicationContext) // this Preference comes for free from the library
    }

    private fun initPaxStoreSdk() { //todo 1. Init AppKey，AppSecret and SN, make sure the appkey and appSecret is corret.
        StoreSdk.getInstance().init(applicationContext, appkey, appSecret, SN, object : BaseApiService.Callback {
            override fun initSuccess() {
                Log.i(TAG, "initSuccess.")
                initInquirer()
                try {
                    val list = ArrayList<Int?>()
                    for (i in 0..9) {
                        list.add(i)
                    }
                    val result = StoreSdk.getInstance().syncApi().syncTerminalBizData(list)
                    LogUtils.d(result)
                } catch (e: NotInitException) {
                    e.printStackTrace()
                }
            }

            override fun initFailed(e: RemoteException) {
                Log.i(TAG, "initFailed: " + e.message)
                Toast.makeText(applicationContext, "Cannot get API URL from PAXSTORE, Please install PAXSTORE first.", Toast.LENGTH_LONG).show()
            }
        })
        //if you want the sdk to show notifications for you, initialize the Notifications
        Notifications.I.init(applicationContext)
                .setSmallIcon(R.drawable.logo_demo_white)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.logo_demo))
        //if you want to customize the notification, disable the Notifications we provided through below code.
// Notifications.I.setEnabled(false);
    }

    private fun initInquirer() { //todo 2. Init checking of whether app can be updated
        StoreSdk.getInstance().initInquirer {
            Log.i(TAG, "call business function....isReadyUpdate = $isReadyToUpdate")
            //todo call your business function here while is ready to update or not
            isReadyToUpdate
        }
    }

    fun isReadyToUpdate(): Boolean {
        return isReadyToUpdate
    }

    fun setReadyToUpdate(readyToUpdate: Boolean) {
        isReadyToUpdate = readyToUpdate
        if (isReadyToUpdate) {
            Toast.makeText(applicationContext, getString(R.string.label_ready_to_update), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(applicationContext, getString(R.string.label_not_ready_to_update), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private val TAG = BaseApplication::class.java.simpleName
        //todo make sure to replace with your own app's appkey and appsecret
        private const val appkey = "BY3FRXA1BZV0VYFQ99M8"
        private const val appSecret = "QAZQXMMOHDK43SR903RIDJAI059UWYQ0X8ZO8RT7"
        //todo please make sure get the correct SN here, for pax device you can integrate NeptuneLite SDK to get the correct SN
        private val SN = Build.SERIAL
        @JvmField
        var appPreferences: AppPreferences? = null
    }
}