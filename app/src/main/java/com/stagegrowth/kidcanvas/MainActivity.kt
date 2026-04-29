package com.stagegrowth.kidcanvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.stagegrowth.kidcanvas.ui.drawing.DrawingScreen
import com.stagegrowth.kidcanvas.ui.theme.KidCanvasTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 앱 단일 Activity.
 * Spring 비유: Servlet 1개로 모든 요청을 받는 Front Controller. 화면 전환은 Compose Navigation으로(M7).
 *
 * M3 PoC: NavGraph 없이 DrawingScreen 을 직접 호출.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KidCanvasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DrawingScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                }
            }
        }
    }
}
