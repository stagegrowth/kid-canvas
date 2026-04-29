package com.stagegrowth.kidcanvas.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stagegrowth.kidcanvas.data.repository.ColoringRepository
import com.stagegrowth.kidcanvas.domain.model.Category
import com.stagegrowth.kidcanvas.domain.model.CategoryProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * 카테고리 목록 + 각 카테고리의 진행 상황을 묶어서 화면에 노출.
 *
 * Spring 비유: 여러 Repository 호출을 모아 단일 응답 DTO 로 만드는 @Service.
 * Flow combine 은 여러 비동기 소스를 한 번에 모아서 합쳐주는 zip 의 reactive 버전.
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: ColoringRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CategoryUiState> = repository.getCategories()
        .flatMapLatest { categories ->
            if (categories.isEmpty()) {
                flowOf(CategoryUiState(items = emptyList(), isLoaded = true))
            } else {
                val progressFlows = categories.map { repository.getCategoryProgress(it.id) }
                combine(progressFlows) { progresses ->
                    val items = categories.zip(progresses.toList()) { c, p ->
                        CategoryListItem(category = c, progress = p)
                    }
                    CategoryUiState(items = items, isLoaded = true)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CategoryUiState(),
        )
}

/**
 * UI 가 알아야 하는 단일 상태 객체.
 *   - isLoaded = false: 아직 비어 있는 초기 emit (스피너 등 표시 가능)
 *   - isLoaded = true + items 비어 있음: "그림이 없어요" 안내 표시
 */
data class CategoryUiState(
    val items: List<CategoryListItem> = emptyList(),
    val isLoaded: Boolean = false,
)

data class CategoryListItem(
    val category: Category,
    val progress: CategoryProgress,
)
