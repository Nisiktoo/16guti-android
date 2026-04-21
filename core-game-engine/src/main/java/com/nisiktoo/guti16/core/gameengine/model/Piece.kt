package com.nisiktoo.guti16.core.gameengine.model

data class PieceId(val value: Int)

/**
 * Represents a single guti (piece) in the 16 Guti game.
 *
 * A Piece is a movable entity placed on a BoardNode. It does not define
 * board structure or game rules; it only stores identity and current state.
 *
 * Movement, capture, and turn logic are handled by the game engine.
 *
 * @param id Unique identifier of the piece.
 * @param owner The player who owns this piece (A or B).
 * @param position Current location of the piece on the board graph. stores the BoardNodeId where the piece is currently located.
 * @param isAlive Indicates whether the piece is still in play or captured.
 */
data class Piece(
    val id: PieceId,
    val owner: Player,
    val position: BoardNodeId?,
    val isAlive: Boolean = true
)