package com.stagegrowth.kidcanvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.stagegrowth.kidcanvas.ui.category.CategoryScreen
import com.stagegrowth.kidcanvas.ui.drawing.DrawingScreen
import com.stagegrowth.kidcanvas.ui.drawing.DrawingViewModel
import com.stagegrowth.kidcanvas.ui.picker.PickerScreen
import com.stagegrowth.kidcanvas.ui.picker.PickerViewModel
import com.stagegrowth.kidcanvas.ui.theme.KidCanvasTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 앱 단일 Activity. M6 임시 라우팅:
 *   Category → Picker(categoryId) → Drawing(targetId)
 *
 * 시스템 백 버튼은 BackHandler 로 한 단계씩 거슬러 올라가게 처리.
 * NavGraph + 화면 전환 애니메이션은 M7 에서 정식 도입 (Compose Navigation).
 *
 * Spring 비유: Servlet Front Controller. 지금은 라우팅이 sealed class 한 묶음 (M7 에서 매핑 테이블화).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KidCanvasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppRoot(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                }
            }
        }
    }
}

/**
 * 단순 상태 머신.
 *   - rememberSaveable 로 회전·프로세스 일시 종료 시 라우트 보존.
 *   - hiltViewModel(key = ...) 로 라우트 인자별로 다른 ViewModel 인스턴스 생성.
 *   - 인자 주입은 LaunchedEffect 안의 setter 호출 — M7 NavGraph 에선 SavedStateHandle 로 대체 가능.
 */
@Composable
private fun AppRoot(modifier: Modifier = Modifier) {
    var route by rememberSaveable(stateSaver = AppRoute.Saver) {
        mutableStateOf<AppRoute>(AppRoute.Category)
    }

    BackHandler(enabled = route !is AppRoute.Category) {
        route = when (val r = route) {
            is AppRoute.Picker -> AppRoute.Category
            is AppRoute.Drawing -> AppRoute.Picker(r.fromCategoryId)
            AppRoute.Category -> AppRoute.Category
        }
    }

    when (val r = route) {
        AppRoute.Category -> CategoryScreen(
            onCategoryClick = { categoryId -> route = AppRoute.Picker(categoryId) },
            modifier = modifier,
        )

        is AppRoute.Picker -> {
            // key 가 categoryId 별로 달라 같은 화면이라도 카테고리가 바뀌면 새 VM.
            val vm: PickerViewModel = hiltViewModel(key = "picker:${r.categoryId}")
            LaunchedEffect(r.categoryId) { vm.setCategoryId(r.categoryId) }
            PickerScreen(
                onBack = { route = AppRoute.Category },
                onTargetClick = { targetId ->
                    route = AppRoute.Drawing(targetId = targetId, fromCategoryId = r.categoryId)
                },
                viewModel = vm,
                modifier = modifier,
            )
        }

        is AppRoute.Drawing -> {
            val vm: DrawingViewModel = hiltViewModel(key = "drawing:${r.targetId}")
            LaunchedEffect(r.targetId) { vm.setTargetId(r.targetId) }
            DrawingScreen(
                onBack = { route = AppRoute.Picker(r.fromCategoryId) },
                viewModel = vm,
                modifier = modifier,
            )
        }
    }
}

/** 라우팅 상태. NavGraph 가 들어오면 이 sealed class 는 그대로 destination 정의로 진화 가능. */
sealed class AppRoute {
    data object Category : AppRoute()
    data class Picker(val categoryId: String) : AppRoute()
    data class Drawing(val targetId: String, val fromCategoryId: String) : AppRoute()

    companion object {
        // rememberSaveable 의 SaverScope 가 List<*> 를 그대로 Bundle 에 담아 저장.
        val Saver: Saver<AppRoute, Any> = Saver(
            save = { route ->
                when (route) {
                    Category -> listOf("c")
                    is Picker -> listOf("p", route.categoryId)
                    is Drawing -> listOf("d", route.targetId, route.fromCategoryId)
                }
            },
            restore = { value ->
                @Suppress("UNCHECKED_CAST")
                val parts = value as List<String>
                when (parts[0]) {
                    "c" -> Category
                    "p" -> Picker(parts[1])
                    "d" -> Drawing(targetId = parts[1], fromCategoryId = parts[2])
                    else -> Category
                }
            },
        )
    }
}
