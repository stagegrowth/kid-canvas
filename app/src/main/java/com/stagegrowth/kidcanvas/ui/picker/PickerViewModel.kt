package com.stagegrowth.kidcanvas.ui.picker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stagegrowth.kidcanvas.data.repository.ColoringRepository
import com.stagegrowth.kidcanvas.domain.model.Category
import com.stagegrowth.kidcanvas.domain.model.ColoringTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 카테고리 내 캐릭터 목록 + 각 캐릭터의 "시작했는지" 표시.
 *
 * categoryId 는 NavGraph 의 route 인자로부터 SavedStateHandle 에 자동 주입.
 * Spring 비유: @PathVariable("categoryId") String categoryId 와 같은 역할.
 */
@HiltViewModel
class PickerViewModel @Inject constructor(
    private val repository: ColoringRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val categoryId: String = savedStateHandle.get<String>(KEY_CATEGORY_ID).orEmpty()

    val uiState: StateFlow<PickerUiState> = if (categoryId.isBlank()) {
        flowOf(PickerUiState(isLoaded = true)).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PickerUiState(isLoaded = true),
        )
    } else {
        combine(
            repository.getCategory(categoryId),
            repository.startedTargetIds(categoryId),
        ) { category, startedIds ->
            PickerUiState(
                category = category,
                startedTargetIds = startedIds,
                isLoaded = true,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PickerUiState(),
        )
    }

    /** 길게 누른 캐릭터의 그림을 처음부터 다시 시작하도록 DB 행 제거. */
    fun resetTarget(targetId: String) {
        viewModelScope.launch {
            repository.resetDrawing(targetId)
        }
    }

    companion object {
        const val KEY_CATEGORY_ID = "categoryId"
    }
}

data class PickerUiState(
    val category: Category? = null,
    val startedTargetIds: Set<String> = emptySet(),
    val isLoaded: Boolean = false,
) {
    val targets: List<ColoringTarget> get() = category?.targets.orEmpty()
}
