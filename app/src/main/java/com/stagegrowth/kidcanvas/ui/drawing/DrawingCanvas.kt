package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.stagegrowth.kidcanvas.domain.model.NormalizedPoint
import com.stagegrowth.kidcanvas.domain.model.Stroke
import com.stagegrowth.kidcanvas.domain.model.Tool
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke

/**
 * 재사용 가능한 드로잉 캔버스.
 * 좌표 처리: 모든 stroke 의 점은 정규화 좌표(0~1).
 *           draw 시점에 size 를 곱해 픽셀 좌표로 변환 → 회전·리사이즈에도 비율 유지.
 *
 * 지우개("긁기" 방식):
 *   바깥 graphicsLayer + CompositingStrategy.Offscreen 으로 별도 레이어를 만들고,
 *   ERASER stroke 는 BlendMode.Clear 로 그 레이어 픽셀을 투명화.
 *   → 부모 흰 배경이 비치며 진짜 '지운' 느낌.
 *
 * 영역 인식 자유 드로잉:
 *   BRUSH stroke 가 seed(첫 터치 위치) 를 가진 경우, 그 seed 가 속한 영역의 ImageBitmap 마스크를
 *   maskBySeed 에서 조회. saveLayer 로 임시 sub-layer 를 만들고 stroke 를 그린 다음
 *   BlendMode.DstIn 으로 마스크 합성 → 마스크 불투명 영역 안의 stroke 만 살아남음.
 *
 * Spring 비유:
 *   saveLayer { ... } restore 는 try-finally 트랜잭션. saveLayer 안의 그림 작업은
 *   임시 레이어에 격리되고, restore 시점에 부모 레이어로 합성된다.
 */
@Composable
fun DrawingCanvas(
    strokes: List<Stroke>,
    currentStroke: Stroke?,
    activeMask: ImageBitmap?,
    maskBySeed: Map<NormalizedPoint, ImageBitmap>,
    onStrokeStart: (Offset, IntSize) -> Unit,
    onStrokeUpdate: (Offset, IntSize) -> Unit,
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
        // 완료된 stroke 들 — 각자의 seed 에 해당하는 마스크 (있으면) 로 클리핑.
        strokes.forEach { stroke ->
            val mask = stroke.seed?.let { maskBySeed[it] }
            drawStrokeMaybeMasked(stroke, density, mask)
        }
        // 진행 중 stroke — activeMask 로 클리핑 (마스크 도착 전엔 자유 드로잉).
        currentStroke?.let { drawStrokeMaybeMasked(it, density, activeMask) }
    }
}

/**
 * stroke 를 그린다. 마스크가 있고 BRUSH 인 경우 sub-layer 안에서 마스크 클리핑 합성.
 *   - mask == null : 자유 드로잉 (기존 동작)
 *   - ERASER       : 마스크 무시 (어디든 지움)
 *   - 그 외        : saveLayer → drawStroke → drawImage(mask, DstIn) → restore
 */
private fun DrawScope.drawStrokeMaybeMasked(
    stroke: Stroke,
    density: Density,
    mask: ImageBitmap?,
) {
    if (mask == null || stroke.tool == Tool.ERASER) {
        drawStroke(stroke, density)
        return
    }
    drawIntoCanvas { canvas ->
        // 임시 레이어 시작. 이 안에서 그려진 모든 픽셀은 격리된 버퍼에 쌓이고,
        // restore 시점에 한꺼번에 부모 레이어로 합성됨.
        canvas.saveLayer(Rect(0f, 0f, size.width, size.height), Paint())
        drawStroke(stroke, density)
        // BlendMode.DstIn:
        //   "destination(이미 그려진 stroke) 를 source(mask) 의 알파만큼 보존."
        //   = 마스크가 불투명한 영역 안의 stroke 만 남고 나머지는 사라짐.
        drawImage(
            image = mask,
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.toInt(), size.height.toInt()),
            blendMode = BlendMode.DstIn,
        )
        canvas.restore()
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
