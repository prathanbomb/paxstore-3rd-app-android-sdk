package com.pax.android.demoapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.pax.market.android.app.sdk.PushConstants
import org.slf4j.LoggerFactory

/**
 * Created by fojut on 2019/5/20.
 */
class PushMessageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when {
            PushConstants.ACTION_NOTIFY_DATA_MESSAGE_RECEIVED == intent.action -> {
                logger.info("### NOTIFY_DATA_MESSAGE_RECEIVED ###")
                val title = intent.getStringExtra(PushConstants.EXTRA_MESSAGE_TITLE)
                val content = intent.getStringExtra(PushConstants.EXTRA_MESSAGE_CONTENT)
                logger.info("### notification title={}, content={} ###", title, content)
                val dataJson = intent.getStringExtra(PushConstants.EXTRA_MESSAGE_DATA)
                logger.info("### data json={} ###", dataJson)
                Toast.makeText(context, "  data=$dataJson", Toast.LENGTH_SHORT).show()
            }
            PushConstants.ACTION_DATA_MESSAGE_RECEIVED == intent.action -> {
                logger.info("### NOTIFY_DATA_MESSAGE_RECEIVED ###")
                val dataJson = intent.getStringExtra(PushConstants.EXTRA_MESSAGE_DATA)
                logger.info("### data json={} ###", dataJson)
                Toast.makeText(context, "  data=$dataJson", Toast.LENGTH_SHORT).show()
            }
            PushConstants.ACTION_NOTIFICATION_MESSAGE_RECEIVED == intent.action -> {
                logger.info("### NOTIFICATION_MESSAGE_RECEIVED ###")
                val title = intent.getStringExtra(PushConstants.EXTRA_MESSAGE_TITLE)
                val content = intent.getStringExtra(PushConstants.EXTRA_MESSAGE_CONTENT)
                logger.info("### notification title={}, content={} ###", title, content)
            }
            PushConstants.ACTION_NOTIFICATION_CLICK == intent.action -> {
                logger.info("### NOTIFICATION_CLICK ###")
                val nid = intent.getIntExtra(PushConstants.EXTRA_MESSAGE_NID, 0)
                val title = intent.getStringExtra(PushConstants.EXTRA_MESSAGE_TITLE)
                val content = intent.getStringExtra(PushConstants.EXTRA_MESSAGE_CONTENT)
                val dataJson = intent.getStringExtra(PushConstants.EXTRA_MESSAGE_DATA)
                logger.info("### notification nid={}, title={}, content={}, dataJson={} ###", nid, title, content, dataJson)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PushMessageReceiver::class.java)
    }
}