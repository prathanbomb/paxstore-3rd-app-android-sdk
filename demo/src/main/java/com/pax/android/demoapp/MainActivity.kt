package com.pax.android.demoapp

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.LogUtils
import com.pax.market.android.app.sdk.BaseApiService.ICallBack
import com.pax.market.android.app.sdk.StoreSdk
import com.pax.market.android.app.sdk.dto.TerminalInfo
import com.pax.market.api.sdk.java.base.constant.ResultCode
import com.pax.market.api.sdk.java.base.exception.NotInitException
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var bannerTitleTV: TextView? = null
    private var bannerTextTV: TextView? = null
    private var bannerSubTextTV: TextView? = null
    private var versionTV: TextView? = null
    private var openClientlayout: LinearLayout? = null
    private var checkUpdate: LinearLayout? = null
    private var msgReceiver: MsgReceiver? = null
    //    private var tradingStateSwitch: Switch? = null
    private var getTerminalInfoBtn: Button? = null
    private var detailListView: ListViewForScrollView? = null
    private var scrollView: ScrollView? = null
    private var demoListViewAdapter: DemoListViewAdapter? = null
    private var spUtil: SPUtil? = null
    private var nodataLayout: LinearLayout? = null
    private var datalist: List<Map<String, Any>>? = null
    private var lvRetrieveData: LinearLayout? = null
    private var openDownloadList: LinearLayout? = null
    private var mImgArrow: ImageView? = null
    private var lvChildRetrieve: LinearLayout? = null
    private var getTerminalLocation: Button? = null
    private var getOnlineStatus // todo remove
            : Button? = null
    private var isExpanded = false
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        verifyStoragePermissions(this)
        spUtil = SPUtil()
        bannerTitleTV = findViewById(R.id.banner_title)
        bannerTextTV = findViewById(R.id.banner_text)
        bannerSubTextTV = findViewById(R.id.banner_sub_text)
//        tradingStateSwitch = findViewById(R.id.tradingStateSwitch)
        openClientlayout = findViewById(R.id.openAppDetail)
        checkUpdate = findViewById(R.id.check_update)
        versionTV = findViewById(R.id.versionText)
        openDownloadList = findViewById(R.id.open_downloadlist_page)
        lvRetrieveData = findViewById(R.id.lv_retrieve_data)
        lvChildRetrieve = findViewById(R.id.lv_childs_retrieve)
        mImgArrow = findViewById(R.id.img_retrieve_data)
        getTerminalLocation = findViewById(R.id.get_location)
        versionText.text = resources.getString(R.string.label_version_text) + " " + BuildConfig.VERSION_NAME
        //receiver to get UI update.
        msgReceiver = MsgReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(DemoConstants.UPDATE_VIEW_ACTION)
        registerReceiver(msgReceiver, intentFilter)
        //switch to set trading status.
        tradingStateSwitch.isChecked = (applicationContext as BaseApplication).isReadyToUpdate()
        tradingStateSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                (applicationContext as BaseApplication).setReadyToUpdate(true)
            } else {
                (applicationContext as BaseApplication).setReadyToUpdate(false)
            }
        }
        check_update.setOnClickListener {
            // check if update available from PAXSTORE.
            val thread = Thread(Runnable {
                try {
                    val updateObject = StoreSdk.getInstance().updateApi().checkUpdate(BuildConfig.VERSION_CODE, packageName)
                    handler.post {
                        if (updateObject.businessCode == ResultCode.SUCCESS.code) {
                            if (updateObject.isUpdateAvailable) {
                                Toast.makeText(this@MainActivity, "Update is available", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this@MainActivity, "No Update available", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "errmsg:>>" + updateObject.message, Toast.LENGTH_SHORT).show()
                            Log.w("MessagerActivity", "updateObject.getBusinessCode():"
                                    + updateObject.businessCode + "\n msg:" + updateObject.message)
                        }
                    }
                } catch (e: NotInitException) {
                    e.printStackTrace()
                }
            })
            thread.start()
        }
        //open paxtore client
        openAppDetail.setOnClickListener {
            //put app 'NeptuneService' package name here for demo.
//if the market don't have this app, it will show app not found, else will go to detail page in PAXSTORE market
            StoreSdk.getInstance().openAppDetailPage(packageName, applicationContext)
        }
        open_downloadlist_page.setOnClickListener { StoreSdk.getInstance().openDownloadListPage(packageName, applicationContext) }
        detailListView = findViewById(R.id.parameter_detail_list)
        nodataLayout = findViewById(R.id.nodata)
        val pushResultBannerTitle = spUtil!!.getString(DemoConstants.PUSH_RESULT_BANNER_TITLE)
        if (DemoConstants.DOWNLOAD_SUCCESS == pushResultBannerTitle) {
            banner_title.text = spUtil!!.getString(DemoConstants.PUSH_RESULT_BANNER_TITLE)
            banner_text.text = spUtil!!.getString(DemoConstants.PUSH_RESULT_BANNER_TEXT)
            banner_sub_text.text = spUtil!!.getString(DemoConstants.PUSH_RESULT_BANNER_SUBTEXT)
            datalist = spUtil!!.getDataList(DemoConstants.PUSH_RESULT_DETAIL)
            //if have push history, display it. the demo will only store the latest push record.
            if (!datalist!!.isNullOrEmpty()) { //display push history detail
                parameter_detail_list.visibility = View.VISIBLE
                nodata.visibility = View.GONE
                demoListViewAdapter = DemoListViewAdapter(this, datalist!!, R.layout.param_detail_list_item)
                parameter_detail_list.adapter = demoListViewAdapter
            } else { //no data. check log for is a correct xml downloaded.
                parameter_detail_list.visibility = View.GONE
                nodata.visibility = View.VISIBLE
                Toast.makeText(this, "File parse error.Please check the downloaded file.", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (DemoConstants.DOWNLOAD_FAILED == pushResultBannerTitle) {
                banner_title.text = spUtil!!.getString(DemoConstants.PUSH_RESULT_BANNER_TITLE)
                banner_text.text = spUtil!!.getString(DemoConstants.PUSH_RESULT_BANNER_TEXT)
            }
            //display as no data
            parameter_detail_list.visibility = View.GONE
            nodata.visibility = View.VISIBLE
        }
        GetTerminalInfo.setOnClickListener {
            StoreSdk.getInstance().getBaseTerminalInfo(applicationContext, object : ICallBack {
                override fun onSuccess(obj: Any) {
                    val terminalInfo = obj as TerminalInfo
                    LogUtils.d(terminalInfo)
                    Toast.makeText(applicationContext, terminalInfo.toString(), Toast.LENGTH_SHORT).show()
                }

                override fun onError(e: Exception) {
                    LogUtils.e(e)
                    Toast.makeText(applicationContext, "getTerminalInfo Error:$e", Toast.LENGTH_SHORT).show()
                }
            })
        }
        lv_retrieve_data.setOnClickListener {
            if (isExpanded) {
                isExpanded = false
                img_retrieve_data.setImageResource(R.mipmap.list_btn_arrow)
                lv_childs_retrieve.visibility = View.GONE
            } else {
                isExpanded = true
                img_retrieve_data.setImageResource(R.mipmap.list_btn_arrow_down)
                lv_childs_retrieve.visibility = View.VISIBLE
            }
        }
        get_location.setOnClickListener {
            StoreSdk.getInstance().startLocate(applicationContext) { locationInfo ->
                LogUtils.d(locationInfo)
                Toast.makeText(this@MainActivity,
                        "Get Location Result：$locationInfo", Toast.LENGTH_SHORT).show()
            }
        }
        //TODO remove
        get_online_status.setOnClickListener {
            val onlineStatusFromPAXSTORE = StoreSdk.getInstance().getOnlineStatusFromPAXSTORE(applicationContext)
            Toast.makeText(this@MainActivity, onlineStatusFromPAXSTORE.toString(), Toast.LENGTH_SHORT).show()
        }
        root.smoothScrollTo(0, 0)
    }

    override fun onDestroy() {
        unregisterReceiver(msgReceiver)
        super.onDestroy()
    }

    inner class MsgReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) { //update main page UI for push status
            when (intent.getIntExtra(DemoConstants.DOWNLOAD_RESULT_CODE, 0)) {
                DemoConstants.DOWNLOAD_STATUS_SUCCESS -> {
                    bannerTitleTV!!.text = spUtil!!.getString(DemoConstants.PUSH_RESULT_BANNER_TITLE)
                    bannerTextTV!!.text = spUtil!!.getString(DemoConstants.PUSH_RESULT_BANNER_TEXT)
                    bannerSubTextTV!!.text = spUtil!!.getString(DemoConstants.PUSH_RESULT_BANNER_SUBTEXT)
                    datalist = spUtil!!.getDataList(DemoConstants.PUSH_RESULT_DETAIL)
                    if (datalist != null && datalist!!.size > 0) { //display push history detail
                        detailListView!!.visibility = View.VISIBLE
                        nodataLayout!!.visibility = View.GONE
                        demoListViewAdapter = DemoListViewAdapter(this@MainActivity, datalist!!, R.layout.param_detail_list_item)
                        detailListView!!.adapter = demoListViewAdapter
                    } else {
                        detailListView!!.visibility = View.GONE
                        nodataLayout!!.visibility = View.VISIBLE
                        Toast.makeText(context, "File parse error.Please check the downloaded file.", Toast.LENGTH_SHORT).show()
                    }
                }
                DemoConstants.DOWNLOAD_STATUS_START -> {
                    bannerTitleTV!!.text = DemoConstants.DOWNLOAD_START
                    bannerTextTV!!.text = "Your push parameters are downloading"
                }
                DemoConstants.DOWNLOAD_STATUS_FAILED -> {
                    bannerTitleTV!!.text = DemoConstants.DOWNLOAD_FAILED
                    bannerTextTV!!.text = spUtil!!.getString(DemoConstants.PUSH_RESULT_BANNER_TEXT)
                    //display as no data
                    detailListView!!.visibility = View.GONE
                    nodataLayout!!.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val handler = Handler()
        private val PERMISSIONS_STORAGE = arrayOf(
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE")

        /**
         * this method request sd card rw permission, you don't need this when you use internal storage
         *
         * @param activity
         */
        fun verifyStoragePermissions(activity: AppCompatActivity?) {
            try { //check permissions
                val permission = ActivityCompat.checkSelfPermission(activity!!,
                        "android.permission.WRITE_EXTERNAL_STORAGE")
                if (permission != PackageManager.PERMISSION_GRANTED) { // request permissions if don't have
                    ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}