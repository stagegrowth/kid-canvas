package com.stagegrowth.kidcanvas.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stagegrowth.kidcanvas.ui.category.CategoryScreen
import com.stagegrowth.kidcanvas.ui.drawing.DrawingScreen
import com.stagegrowth.kidcanvas.ui.drawing.DrawingViewModel
import com.stagegrowth.kidcanvas.ui.home.HomeScreen
import com.stagegrowth.kidcanvas.ui.picker.PickerScreen
import com.stagegrowth.kidcanvas.ui.picker.PickerViewModel

/**
 * 앱 전역 네비게이션 그래프.
 *
 * Spring 비유:
 *   - @RestController 의 `@GetMapping("/picker/{categoryId}")` 같은 라우팅 테이블.
 *   - NavHost 가 DispatcherServlet, composable() 가 각 핸들러 매핑.
 *   - SavedStateHandle 은 @PathVariable, 자동으로 Hilt 가 주입.
 *
 * 화면 전환은 fadeIn/fadeOut 150ms — 너무 빠르지도 느리지도 않게.
 */
@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(150)) },
        exitTransition = { fadeOut(animationSpec = tween(150)) },
        popEnterTransition = { fadeIn(animationSpec = tween(150)) },
        popExitTransition = { fadeOut(animationSpec = tween(150)) },
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onStart = { navController.navigate(Routes.CATEGORY) },
            )
        }

        composable(Routes.CATEGORY) {
            CategoryScreen(
                onCategoryClick = { categoryId ->
                    navController.navigate(Routes.pickerOf(categoryId))
                },
            )
        }

        composable(
            route = Routes.PICKER_PATTERN,
            arguments = listOf(
                navArgument(PickerViewModel.KEY_CATEGORY_ID) { type = NavType.StringType },
            ),
        ) {
            PickerScreen(
                onBack = { navController.popBackStack() },
                onTargetClick = { targetId ->
                    navController.navigate(Routes.drawingOf(targetId))
                },
            )
        }

        composable(
            route = Routes.DRAWING_PATTERN,
            arguments = listOf(
                navArgument(DrawingViewModel.KEY_TARGET_ID) { type = NavType.StringType },
            ),
        ) {
            DrawingScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}

/**
 * 라우트 상수와 인자 인코딩 헬퍼. 인자 키는 ViewModel 의 KEY_* 와 일치해야
 * SavedStateHandle 이 자동으로 채워준다.
 */
object Routes {
    const val HOME: String = "home"
    const val CATEGORY: String = "category"

    const val PICKER_PATTERN: String = "picker/{${PickerViewModel.KEY_CATEGORY_ID}}"
    const val DRAWING_PATTERN: String = "drawing/{${DrawingViewModel.KEY_TARGET_ID}}"

    fun pickerOf(categoryId: String): String = "picker/$categoryId"
    fun drawingOf(targetId: String): String = "drawing/$targetId"
}
