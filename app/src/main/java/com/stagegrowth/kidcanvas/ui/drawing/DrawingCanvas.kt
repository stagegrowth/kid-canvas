package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.stagegrowth.kidcanvas.domain.model.Stroke
import com.stagegrowth.kidcanvas.domain.model.Tool
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke

/**
 * 재사용 가능한 드로잉 캔버스.
 * 좌표 처리: 모든 stroke 의 점은 정규화 좌표(0~1).
 *           draw 시점에 size 를 곱해 픽셀 좌표로 변환 → 회전·리사이즈에도 비율 유지.
 *
 * 지우개("긁기" 방식):
 *   graphicsLayer + CompositingStrategy.Offscreen 으로 별도 레이어를 만들고,
 *   ERASER stroke 는 BlendMode.Clear 로 그 레이어의 픽셀을 투명화.
 *   → 부모 흰 배경이 비치며 진짜 '지운' 느낌. 외곽선(z=2)은 별도 레이어라 영향 없음.
 */
@Composable
fun DrawingCanvas(
    strokes: List<Stroke>,
    currentStroke: Stroke?,
    onStrokeStart: (Offset, androidx.compose.ui.unit.IntSize) -> Unit,
    onStrokeUpdate: (Offset, androidx.compose.ui.unit.IntSize) -> Unit,
    onStrokeEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    Canvas(
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    onStrokeStart(down.position, size)
                    down.consume()

                    while (true) {
                        val event = awaitPointerEvent()
                        val pointer = event.changes.firstOrNull { it.id == down.id }
                        if (pointer == null || !pointer.pressed) break
                        onStrokeUpdate(pointer.position, size)
                        pointer.consume()
                    }
                    onStrokeEnd()
                }
            }
    ) {
        strokes.forEach { drawStroke(it, density) }
        currentStroke?.let { drawStroke(it, density) }
    }
}

private fun DrawScope.drawStroke(stroke: Stroke, density: Density) {
    if (stroke.points.isEmpty()) return
    val widthPx = with(density) { stroke.widthDp.dp.toPx() }
    val color = if (stroke.tool == Tool.ERASER) Color.Black else Color(stroke.color)
    val blendMode = if (stroke.tool == Tool.ERASER) BlendMode.Clear else BlendMode.SrcOver

    // 점이 1개면 작은 원 하나
    if (stroke.points.size == 1) {
        val p = stroke.points[0]
        drawCircle(
            color = color,
            radius = widthPx / 2f,
            center = Offset(p.x * size.width, p.y * size.height),
            blendMode = blendMode,
        )
        return
    }

    val path = Path()
    val first = stroke.points.first()
    path.moveTo(first.x * size.width, first.y * size.height)

    if (stroke.points.size == 2) {
        val last = stroke.points.last()
        path.lineTo(last.x * size.width, last.y * size.height)
    } else {
        // 3개 이상: 각 점을 control point, 다음 점과의 중점을 endpoint 로 quadratic 보간
        for (i in 1 until stroke.points.size - 1) {
            val cur = stroke.points[i]
            val next = stroke.points[i + 1]
            val midX = (cur.x + next.x) / 2f * size.width
            val midY = (cur.y + next.y) / 2f * size.height
            path.quadraticTo(
                x1 = cur.x * size.width,
                y1 = cur.y * size.height,
                x2 = midX,
                y2 = midY,
            )
        }
        // 마지막 점까지 연결
        val last = stroke.points.last()
        path.lineTo(last.x * size.width, last.y * size.height)
    }

    drawPath(
        path = path,
        color = color,
        style = DrawStroke(
            width = widthPx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
        blendMode = blendMode,
    )
}
