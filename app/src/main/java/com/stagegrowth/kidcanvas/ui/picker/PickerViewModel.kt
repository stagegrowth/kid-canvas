package com.stagegrowth.kidcanvas.ui.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stagegrowth.kidcanvas.data.repository.ColoringRepository
import com.stagegrowth.kidcanvas.domain.model.Category
import com.stagegrowth.kidcanvas.domain.model.ColoringTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 카테고리 내 캐릭터 목록 + 각 캐릭터의 "시작했는지" 표시.
 *
 * categoryId 는 setCategoryId() 로 화면 진입 시 한 번 주입.
 * (M7 NavGraph 도입 시 SavedStateHandle 로 자동 주입되도록 교체 예정.)
 */
@HiltViewModel
class PickerViewModel @Inject constructor(
    private val repository: ColoringRepository,
) : ViewModel() {

    private val categoryId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PickerUiState> = categoryId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) {
                flowOf(PickerUiState(isLoaded = true))
            } else {
                combine(
                    repository.getCategory(id),
                    repository.startedTargetIds(id),
                ) { category, startedIds ->
                    PickerUiState(
                        category = category,
                        startedTargetIds = startedIds,
                        isLoaded = true,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PickerUiState(),
        )

    fun setCategoryId(id: String) {
        if (categoryId.value != id) categoryId.value = id
    }

    /** 길게 누른 캐릭터의 그림을 처음부터 다시 시작하도록 DB 행 제거. */
    fun resetTarget(targetId: String) {
        viewModelScope.launch {
            repository.resetDrawing(targetId)
        }
    }
}

data class PickerUiState(
    val category: Category? = null,
    val startedTargetIds: Set<String> = emptySet(),
    val isLoaded: Boolean = false,
) {
    val targets: List<ColoringTarget> get() = category?.targets.orEmpty()
}
