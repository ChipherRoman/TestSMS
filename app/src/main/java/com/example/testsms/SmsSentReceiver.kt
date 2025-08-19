package com.example.testsms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.widget.Toast

class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show()
                println("SMS_SENT_RECEIVER: SMS sent successfully")
            }
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                Toast.makeText(context, "SMS generic failure", Toast.LENGTH_SHORT).show()
                println("SMS_SENT_RECEIVER: SMS generic failure")
            }
            SmsManager.RESULT_ERROR_NO_SERVICE -> {
                Toast.makeText(context, "SMS no service", Toast.LENGTH_SHORT).show()
                println("SMS_SENT_RECEIVER: SMS no service")
            }
            SmsManager.RESULT_ERROR_NULL_PDU -> {
                Toast.makeText(context, "SMS null PDU", Toast.LENGTH_SHORT).show()
                println("SMS_SENT_RECEIVER: SMS null PDU")
            }
            SmsManager.RESULT_ERROR_RADIO_OFF -> {
                Toast.makeText(context, "SMS radio off", Toast.LENGTH_SHORT).show()
                println("SMS_SENT_RECEIVER: SMS radio off")
            }
        }
    }
}
