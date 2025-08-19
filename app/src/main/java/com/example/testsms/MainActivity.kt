package com.example.testsms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val messageList = mutableStateListOf<String>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ask runtime permissions
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
                // do nothing, just request
            }

        val permissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.INTERNET
        )

        if (!permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            requestPermissionLauncher.launch(permissions)
        }

        Log.d("MainActivity", "onCreate")

        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("SMS → n8n") })
                    }
                ) { padding ->
                    MessageList(
                        messages = messageList,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }

        // Register a callback so SmsReceiver can update UI
        SmsReceiver.onMessageReceived = { sender, body ->
            runOnUiThread {
                messageList.add("From: $sender\n$body")
            }
        }
    }
}

@Composable
fun MessageList(messages: List<String>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.padding(16.dp)) {
        items(messages) { msg ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(msg, modifier = Modifier.padding(16.dp))
            }
        }
    }
}
