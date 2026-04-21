package com.nisiktoo.guti16.core.gameengine.state

import com.nisiktoo.guti16.core.gameengine.model.BoardNodeId
import com.nisiktoo.guti16.core.gameengine.model.Piece
import com.nisiktoo.guti16.core.gameengine.model.Player

/**
 * Represents the current state of the game, including the positions of pieces on the board,
 * the current player's turn, and any forced moves.
 *
 * @property occupancy A map of board node IDs to the pieces occupying them.
 * @property currentPlayer The player whose turn it is.
 * @property forcedFrom In a chain-capture scenario, this indicates the node from which
 * the next move must be made. If null, there are no forced moves.
 */
data class GameState(
    val occupancy: Map<BoardNodeId, Piece>,
    val currentPlayer: Player,
    val forcedFrom: BoardNodeId? = null,
)