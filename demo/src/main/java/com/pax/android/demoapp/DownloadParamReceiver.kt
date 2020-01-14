package com.pax.android.demoapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.pax.android.demoapp.DownloadParamService

/**
 * Created by zcy on 2016/12/2 0002.
 */
class DownloadParamReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) { //todo add log to see if the broadcast is received, if not, please check whether the bradcast config is correct
        Log.i("DownloadParamReceiver", "broadcast received")
        //todo receive the broadcast from paxstore, start a service to download parameter files
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //
            context.startForegroundService(Intent(context, DownloadParamService::class.java))
        } else {
            context.startService(Intent(context, DownloadParamService::class.java))
        }
    }
}