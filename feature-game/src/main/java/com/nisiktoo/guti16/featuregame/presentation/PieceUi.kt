package com.nisiktoo.guti16.featuregame.presentation

import androidx.compose.ui.graphics.Color
import com.nisiktoo.guti16.core.gameengine.model.BoardNodeId
import com.nisiktoo.guti16.core.gameengine.model.PieceId
import com.nisiktoo.guti16.core.gameengine.model.Player

/**
 * Represents the UI state of a piece on the board, including its ID, owner, position, and whether it is alive.
 * @param id The unique identifier of the piece.
 * @param owner The player who owns this piece (A or B).
 * @param position The current position of the piece on the board, represented by a BoardNodeId.
 * @param isAlive Indicates whether the piece is still in play (not captured) or
 */
data class PieceUi(
    val id: PieceId,
    val owner: Player,
    val position: BoardNodeId?,
    val isAlive: Boolean = true,
    val pieceColor: Color,
    val borderColor: Color,
    val selectedGlowColor: Color,
    val isSelected: Boolean = false,
)