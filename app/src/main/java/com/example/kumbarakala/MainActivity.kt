package com.example.kumbarakala

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.kumbarakala.ui.KumbaraKalaApp
import com.example.kumbarakala.ui.theme.KumbaraKalaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KumbaraKalaTheme {
                KumbaraKalaApp()
            }
        }
    }
}
