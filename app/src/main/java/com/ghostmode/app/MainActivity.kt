package com.ghostmode.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ghost.init(this)
        enableEdgeToEdge()
        setContent { GhostTheme { GhostApp() } }
    }
}

class BlockActivity : ComponentActivity() {
    private fun home() {
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GhostTheme {
                BackHandler { home() }
                Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("GHOST MODE ACTIVE", color = Ice, fontSize = 14.sp, letterSpacing = 4.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("This application is currently restricted.", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Your attention belongs elsewhere.", color = Sil.copy(alpha = .6f))
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = { home() }) { Text("Return home") }
                    TextButton(onClick = {
                        startActivity(Intent(this@BlockActivity, MainActivity::class.java)); finish()
                    }) { Text("Open GHOST MODE (emergency access)", color = Ice) }
                }
            }
        }
    }
}
