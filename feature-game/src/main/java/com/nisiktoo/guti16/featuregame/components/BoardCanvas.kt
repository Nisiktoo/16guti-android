package com.nisiktoo.guti16.featuregame.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.nisiktoo.guti16.core.gameengine.board.BoardGraphApi
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
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawBoard(pieces)
    }
}

/**
 *drawBoard actually draws the board background.

 */
fun DrawScope.drawBoard(pieces: List<PieceUi> = emptyList()) {
    drawBoardLines()
    drawPieces(pieces)
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

fun DrawScope.drawBoardLines() {
    val unit = min(size.width / 4f, size.height / 6f)
    val boardWidth = 4f * unit
    val boardHeight = 6f * unit

    val originX = (size.width - boardWidth) / 2f
    val originY = (size.height - boardHeight) / 2f
    val strokeWidth = (size.minDimension * 0.004f).coerceAtLeast(2f)

    val boardColor = Color(0xFF3D91A0)
    buildBoardLines().forEach { line ->
        drawLine(
            color = boardColor,
            start = boardPointToOffset(line.start, originX, originY, unit),
            end = boardPointToOffset(line.end, originX, originY, unit),
            strokeWidth = strokeWidth
        )
    }
}

/** * Draws the pieces on the board based on their current state.
 * Each piece is drawn as a circle with its corresponding color and position.
 * Only alive pieces with valid positions are rendered.
 *
 * @param pieces List of PieceUi objects representing the current pieces in the game.
 */
private fun DrawScope.drawPieces(pieces: List<PieceUi>) {
    if (pieces.isEmpty()) return

    val unit = min(size.width / 4f, size.height / 6f)
    val boardWidth = 4f * unit
    val boardHeight = 6f * unit
    val originX = (size.width - boardWidth) / 2f
    val originY = (size.height - boardHeight) / 2f
    val pieceRadius = PieceTheme().pieceRadius

    pieces
        .asSequence()
        .filter { it.isAlive && it.position != null }
        .forEach { piece ->
            val node = BoardGraphApi.getNode(piece.position!!)
            val center = boardPointToOffset(boardPoint(node.row, node.col), originX, originY, unit)

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
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = (pieceRadius * 0.12f).coerceAtLeast(1.5f)),
            )
        }
}
