package com.example.testsms


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class SmsReceiver : BroadcastReceiver() {
    companion object {
        var onMessageReceived: ((String, String) -> Unit)? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (msg in msgs) {
                val sender = msg.displayOriginatingAddress
                val body = msg.messageBody

                // Update UI if app is open
                onMessageReceived?.invoke(sender, body)

                Log.d("SmsReceiver", "Sender: $sender, Message: $body")

                // Send to n8n via WorkManager
                val work = OneTimeWorkRequestBuilder<SendToN8nWorker>()
                    .setInputData(
                        workDataOf(
                            "sender" to sender,
                            "message" to body
                        )
                    ).build()
                WorkManager.getInstance(context).enqueue(work)
            }
        }
    }
}
