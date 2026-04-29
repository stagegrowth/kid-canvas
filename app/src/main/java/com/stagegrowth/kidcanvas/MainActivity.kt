package com.stagegrowth.kidcanvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.stagegrowth.kidcanvas.ui.navigation.AppNavGraph
import com.stagegrowth.kidcanvas.ui.theme.KidCanvasTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 앱 단일 Activity. 라우팅은 AppNavGraph 가 담당 (NavHost + composable 매핑).
 *
 * Spring 비유: Servlet Front Controller. 요청 분기는 NavGraph 의 라우트 패턴이 처리.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KidCanvasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavGraph(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                }
            }
        }
    }
}
