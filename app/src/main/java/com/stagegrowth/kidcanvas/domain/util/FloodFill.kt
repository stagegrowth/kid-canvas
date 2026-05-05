package com.stagegrowth.kidcanvas.domain.util

/** 외곽선 판정 알파 임계값. 진단 로그와 호출 측 모두 같은 값을 쓰도록 공용 상수로 노출. */
const val OUTLINE_THRESHOLD: Int = 80

/**
 * 4 방향 BFS Flood Fill — 외곽선 PNG 의 알파 채널을 이용해 한 영역(닫힌 폐곡선 안쪽)을 마스크로 추출.
 *
 * Spring 비유: 그래프 BFS. ArrayDeque 가 큐, mask 가 visited.
 *
 * @param alpha 외곽선 비트맵의 알파만 추출한 1 차원 배열 (행 우선, alpha[y*width + x])
 * @param width 비트맵 가로
 * @param height 비트맵 세로
 * @param seedX 시작 픽셀 X
 * @param seedY 시작 픽셀 Y
 * @param threshold 알파 ≥ threshold 면 외곽선으로 간주 (기본 80, 안티에일리어싱 회색까지 포함)
 * @return 마스크 (true = 영역 안, false = 외곽선/영역 밖). 시작 픽셀이 외곽선 위면 null.
 */
fun floodFillRegion(
    alpha: IntArray,
    width: Int,
    height: Int,
    seedX: Int,
    seedY: Int,
    threshold: Int = OUTLINE_THRESHOLD,
): BooleanArray? {
    if (seedX !in 0 until width || seedY !in 0 until height) return null
    val seedIdx = seedY * width + seedX
    if (alpha[seedIdx] >= threshold) return null // 외곽선 위 시작 → 마스크 없음 (자유 드로잉)

    val mask = BooleanArray(width * height)
    val queue = ArrayDeque<Int>()
    queue.addLast(seedIdx)
    mask[seedIdx] = true

    while (queue.isNotEmpty()) {
        val idx = queue.removeFirst()
        val x = idx % width
        val y = idx / width

        // 4 방향 이웃 — 외곽선이 아니고 아직 안 본 곳만 큐에 추가
        if (x > 0) {
            val n = idx - 1
            if (!mask[n] && alpha[n] < threshold) { mask[n] = true; queue.addLast(n) }
        }
        if (x < width - 1) {
            val n = idx + 1
            if (!mask[n] && alpha[n] < threshold) { mask[n] = true; queue.addLast(n) }
        }
        if (y > 0) {
            val n = idx - width
            if (!mask[n] && alpha[n] < threshold) { mask[n] = true; queue.addLast(n) }
        }
        if (y < height - 1) {
            val n = idx + width
            if (!mask[n] && alpha[n] < threshold) { mask[n] = true; queue.addLast(n) }
        }
    }
    return mask
}

/**
 * 마스크 침식(erosion). 마스크 경계 픽셀을 1 px 깎아내 외곽선 안티에일리어싱 회색 라인으로
 * 색이 살짝 묻는 것을 방지한다. iterations 만큼 반복 (보통 1 이면 충분).
 *
 * 알고리즘: 한 픽셀이 살아남으려면 4 방향 이웃 모두 마스크 안이어야 한다.
 */
fun erodeMask(mask: BooleanArray, width: Int, height: Int, iterations: Int = 1): BooleanArray {
    if (iterations <= 0) return mask
    var current = mask
    repeat(iterations) {
        val next = BooleanArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val i = y * width + x
                if (!current[i]) continue
                val left = x > 0 && current[i - 1]
                val right = x < width - 1 && current[i + 1]
                val up = y > 0 && current[i - width]
                val down = y < height - 1 && current[i + width]
                if (left && right && up && down) next[i] = true
            }
        }
        current = next
    }
    return current
}
