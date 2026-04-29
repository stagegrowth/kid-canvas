package com.stagegrowth.kidcanvas.domain.model

/**
 * 한 카테고리에 대한 진행 상황.
 *   - total: 카테고리에 속한 캐릭터 수 (content.json 기준)
 *   - started: strokes 가 1개 이상 있는 캐릭터 수 (Room 의 drawing_states 기준)
 *
 * "8개 중 3개 그렸어요" 같은 안내 문구를 만들기 위한 값 객체.
 */
data class CategoryProgress(
    val total: Int,
    val started: Int,
) {
    companion object {
        val Empty: CategoryProgress = CategoryProgress(total = 0, started = 0)
    }
}
