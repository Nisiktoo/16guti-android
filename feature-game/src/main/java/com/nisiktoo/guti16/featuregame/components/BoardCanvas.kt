package com.nisiktoo.guti16.featuregame.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.nisiktoo.guti16.core.gameengine.board.BoardGraphApi
import com.nisiktoo.guti16.core.gameengine.model.BoardNodeId
import com.nisiktoo.guti16.coreui.theme.PieceTheme
import com.nisiktoo.guti16.featuregame.presentation.PieceUi
import kotlin.math.min

/**
 *BoardCanvas draws the board lines on the canvas.

 */
@Composable
fun BoardCanvas(
    modifier: Modifier = Modifier,
    pieces: List<PieceUi> = emptyList(),
    boardNodeIds: List<Int> = BoardGraphApi.getAllBoardNodeIds(),
    onNodeClick: (Int) -> Unit = {},
) {
    val currentOnNodeClick by rememberUpdatedState(onNodeClick)
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var boardLayout by remember(boardNodeIds) {
        mutableStateOf<BoardLayout?>(null)
    }

    LaunchedEffect(canvasSize, boardNodeIds) {
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            boardLayout = buildBoardLayout(
                canvasWidth = canvasSize.width.toFloat(),
                canvasHeight = canvasSize.height.toFloat(),
                boardNodeIds = boardNodeIds,
            )
        }
    }

    Box(
        modifier = modifier.fillMaxSize().onSizeChanged{ canvasSize = it}
    ) {
        Canvas(modifier = Modifier
            .matchParentSize()
            .pointerInput(boardLayout) {
                detectTapGestures {  tap ->
                    boardLayout?.hitTest(tap)?.let(currentOnNodeClick)
                }
            }
        ) {
            val layout = boardLayout ?: return@Canvas
            drawBoard(layout, pieces)
        }
    }
}

/**
 *drawBoard actually draws the board background.

 */
private fun DrawScope.drawBoard(layout: BoardLayout, pieces: List<PieceUi> = emptyList()) {
    drawBoardLines(layout)
    drawPieces(layout, pieces)
}
/** BoardPoint represents a point on the board.
 * BoardPoint is used to translate the coordinate of the board to the coordinate of the canvas.
 * Basically it is a point on the board.
 */
private data class BoardPoint(val rowUnit: Float, val colUnit: Float)

/**
 * BoardLine represents a line on the board.
 * @param start: BoardPoint -- the start point of the line
 * @param end: BoardPoint -- the end point of the line
 */
private data class BoardLine(val start: BoardPoint, val end: BoardPoint)


/** NodeHitRegion represents a region on the board where a node can be hit.
 * @param nodeId: Int -- the id of the node
 * @param center: Offset -- the center of the region
 * @param radius: Float -- the radius of the region
 */
private data class NodeHitRegion(
    val nodeId: Int,
    val center: Offset,
    val radius: Float,
)

/** BoardLayout represents the layout of the board.
 * @param originX: Float -- the x coordinate of the origin of the board
 * @param originY: Float -- the y coordinate of the origin of the board
 * @param unit: Float -- the unit of the board
 * @param nodeCenters: Map<Int, Offset> -- the center of each node
 * @param hitRegions: List<NodeHitRegion> -- the hit regions of each node
 */
private data class BoardLayout(
    val originX: Float,
    val originY: Float,
    val unit: Float,
    val nodeCenters: Map<Int, Offset>,
    val hitRegions : List<NodeHitRegion>,
) {

    /** hitTest returns the id of the node that was hit, or null if no node was hit.
     * @param tap: Offset -- the tap point
     * @return Int? -- the id of the node that was hit, or null if no node was hit
     */
    fun hitTest(tap: Offset): Int? {
        var bestNodeId: Int? = null
        var bestDistSq = Float.MAX_VALUE

        for (region in hitRegions) {
            val dx = tap.x - region.center.x
            val dy = tap.y - region.center.y
            val distSq = dx * dx + dy * dy
            val radiusSq = region.radius * region.radius
            if (distSq <= radiusSq && distSq < bestDistSq) {
                bestDistSq = distSq
                bestNodeId = region.nodeId
            }
        }
        return bestNodeId
    }
    fun centerOf(nodeId: Int) : Offset? = nodeCenters[nodeId]
}

private fun buildBoardLayout(
    canvasWidth: Float,
    canvasHeight: Float,
    boardNodeIds: List<Int>,
): BoardLayout {
    val unit = min(canvasWidth / 4f, canvasHeight / 6f)
    val boardWidth = 4f * unit
    val boardHeight = 6f * unit

    val originX = (canvasWidth - boardWidth) / 2f
    val originY = (canvasHeight - boardHeight) / 2f

    val pieceRadius = PieceTheme().pieceRadius
    val hitRadius = maxOf(pieceRadius * 1.35f, unit * 0.28f)
    val nodeCenters = LinkedHashMap<Int, Offset>(boardNodeIds.size)
    val hitRegions = ArrayList<NodeHitRegion>(boardNodeIds.size)

    for (nodeId in boardNodeIds) {
        val node = BoardGraphApi.getNode(BoardNodeId(nodeId))
        val center = boardPointToOffset(
            boardPoint = boardPoint(row = node.row, col = node.col),
            originX = originX,
            originY = originY,
            unit = unit,
        )
        nodeCenters[nodeId] = center
        hitRegions += NodeHitRegion(
            nodeId = nodeId,
            center = center,
            radius = hitRadius,
        )
    }
    return BoardLayout(
        originX = originX,
        originY = originY,
        unit = unit,
        nodeCenters = nodeCenters,
        hitRegions = hitRegions,
    )
}

/** Get the exact location of a point on the board given its matrix coordinate.
 * It translates the coordinate of the board to the coordinate of the canvas.
 * @param row: Int -- the row of matrix coordinate
 * @param col: Int -- the column of matrix coordinate
 */
private fun boardPoint(row: Int, col: Int): BoardPoint {
    val rowUnit: Float
    val colUnit: Float

    when (row) {
        0 -> {
            rowUnit = 0f
            colUnit = col.toFloat()
        }

        1 -> {
            rowUnit = 0.5f
            colUnit = 2f + 0.5f * (col - 2)
        }

        7 -> {
            rowUnit = 5.5f
            colUnit = 2f + 0.5f * (col - 2)
        }

        else -> {
            val adjustedRow = if (row == 8) 7 else row
            rowUnit = (adjustedRow - 1).toFloat()
            colUnit = col.toFloat()
        }
    }

    return BoardPoint(rowUnit = rowUnit, colUnit = colUnit)
}

private fun boardPointToOffset(
    boardPoint: BoardPoint,
    originX: Float,
    originY: Float,
    unit: Float,
): Offset = Offset(
    x = originX + (boardPoint.colUnit * unit),
    y = originY + (boardPoint.rowUnit * unit),
)


/**
 * This function manually builds the board lines to build the game board.
 * it draws some horizontal, vertical and diagonal lines such that it looks like the board.
 * There are 5 central horizontal lines (row [2 - 6) (0 - based).
 *
 */
private fun buildBoardLines(): List<BoardLine> {

    /* 5 horizontal lines
     * 5 vertical lines
     * 3 diagonal lines
     */
    val lines = mutableListOf<BoardLine>()

    // Main 5x5 lattice (same topology as the original JS renderer)
    for (i in 1..5) {
        /* 5 center horizontal lines */
        // left to right
        lines += BoardLine(BoardPoint(i.toFloat(), 0f), BoardPoint(i.toFloat(), 4f))

        /* 5 center vertical lines of length 4 */
        // up to down
        lines += BoardLine(BoardPoint(1f, (i - 1).toFloat()), BoardPoint(5f, (i - 1).toFloat()))
    }

    /* bottom left to upper right Diagonals starts */
    /* here we are manually inserting the lines ends */
    // (4, 0) -> (0, 3)
    lines += BoardLine(BoardPoint(3f, 0f), BoardPoint(0f, 3f))
    // (5, 0) -> (2, 4)
    lines += BoardLine(BoardPoint(5f, 0f), BoardPoint(1f, 4f))
    // (8, 1) -> (4, 4)
    lines += BoardLine(BoardPoint(6f, 1f), BoardPoint(3f, 4f))
    /* bottom left to upper right Diagonals ends */

    /* upper left to bottom right Diagonals starts */
    lines += BoardLine(BoardPoint(3f, 0f), BoardPoint(6f, 3f))
    lines += BoardLine(BoardPoint(1f, 0f), BoardPoint(5f, 4f))
    lines += BoardLine(BoardPoint(0f, 1f), BoardPoint(3f, 4f))
    /* upper left to bottom right Diagonals ends */

    // Special small segments
    /* (0, 1) -> (0, 3) */
    lines += BoardLine(BoardPoint(0f, 1f), BoardPoint(0f, 3f))
    /* (1, 1) -> (1, 3) */
    lines += BoardLine(BoardPoint(0.5f, 1.5f), BoardPoint(0.5f, 2.5f))

    // Segments expressed from board coordinates used by game logic
    lines += BoardLine(boardPoint(row = 0, col = 2), boardPoint(row = 2, col = 2))
    lines += BoardLine(boardPoint(row = 7, col = 1), boardPoint(row = 7, col = 3))
    lines += BoardLine(boardPoint(row = 8, col = 1), boardPoint(row = 8, col = 3))
    lines += BoardLine(boardPoint(row = 6, col = 2), boardPoint(row = 8, col = 2))

    return lines
}


/** drawBoardLines draws the board lines on the canvas.
 * @param layout: BoardLayout -- the layout of the board
 */
private fun DrawScope.drawBoardLines(layout: BoardLayout) {
    val strokeWidth = (size.minDimension * 0.004f).coerceAtLeast(2f)
    val boardColor = Color(0xFF3D91A0)

    buildBoardLines().forEach { line ->
        drawLine(
            color = boardColor,
            start = boardPointToOffset(line.start, layout.originX, layout.originY, layout.unit),
            end = boardPointToOffset(line.end, layout.originX, layout.originY, layout.unit),
            strokeWidth = strokeWidth,
        )
    }
}



/** drawPieces draws the pieces on the board.
 * @param layout: BoardLayout -- the layout of the board
 * @param pieces: List<PieceUi> -- the list of pieces to draw
 */
private fun DrawScope.drawPieces(layout: BoardLayout, pieces: List<PieceUi>) {
    if (pieces.isEmpty()) return

    val pieceRadius = PieceTheme().pieceRadius

    pieces
        .asSequence()
        .filter { it.isAlive && it.position != null }
        .forEach { piece ->
            val center = layout.centerOf(piece.position?.value!!) ?: return@forEach

            drawCircle(
                color = piece.pieceColor,
                radius = pieceRadius,
                center = center,
            )
            drawCircle(
                color = piece.selectedGlowColor.copy(alpha = 0.22f),
                radius = pieceRadius * 0.82f,
                center = center,
            )
            drawCircle(
                color = piece.borderColor,
                radius = pieceRadius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = (pieceRadius * 0.12f).coerceAtLeast(1.5f)
                ),
            )
        }
}