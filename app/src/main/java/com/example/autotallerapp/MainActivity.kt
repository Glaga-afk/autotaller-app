package com.example.autotallerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.autotallerapp.ui.navigation.NavegacionAutoTaller
import com.example.autotallerapp.ui.theme.AutoTallerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoTallerTheme {
                NavegacionAutoTaller()
            }
        }
    }
}
