package com.example.testsms

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SendToN8nWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private fun sendSMS(phoneNumber: String, message: String) {
        println("N8N_WORKER: Preparing to send SMS to $phoneNumber")
        try {
            // FLAG_IMMUTABLE is required for targeting Android S (API 31) and above.
            val sentPI: PendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                0,
                Intent("com.example.testsms.SMS_SENT"),
                PendingIntent.FLAG_IMMUTABLE
            )

            val smsManager = applicationContext.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, null)
            println("N8N_WORKER: SMS send command issued for $phoneNumber. Status will be delivered via BroadcastReceiver.")
        } catch (e: Exception) {
            println("N8N_WORKER: Exception caught while trying to send SMS: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun doWork(): Result {
        println("N8N_WORKER: doWork started.")
        val sender = inputData.getString("sender")
        val message = inputData.getString("message")

        if (sender == null || message == null) {
            println("N8N_WORKER: Error - sender or message is null. Aborting.")
            return Result.failure()
        }
        println("N8N_WORKER: Received sender: $sender, message: $message")

        return try {
            val url = URL("https://rynalde.app.n8n.cloud/webhook-test/sendText")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 30000
            conn.readTimeout = 30000
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            val jsonRequest = """{"sender":"$sender","message":"$message"}"""
            println("N8N_WORKER: Sending JSON Request: $jsonRequest")
            conn.outputStream.use {
                it.write(jsonRequest.toByteArray())
            }

            val responseCode = conn.responseCode
            println("N8N_WORKER: HTTP Response Code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.use { it.readText() }
                println("N8N_WORKER: HTTP Success Response: $response")
                reader.close()

                val outputMessage = response
                if (outputMessage.isNotBlank()) {
                    sendSMS(sender, outputMessage)
                } else {
                    println("N8N_WORKER: Warning - n8n response was blank, not sending SMS.")
                }
                Result.success()
            } else {
                val errorStream = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() } }
                println("N8N_WORKER: HTTP Error Response: $responseCode ${conn.responseMessage}. Error body: $errorStream")
                Result.retry()
            }
        } catch (e: Exception) {
            println("N8N_WORKER: Exception in doWork: ${e.message}")
            e.printStackTrace()
            Result.retry()
        }
    }
}
