package com.nisiktoo.guti16.featuregame.presentation

import com.nisiktoo.guti16.core.gameengine.model.BoardNode
import com.nisiktoo.guti16.core.gameengine.model.BoardNodeId
import com.nisiktoo.guti16.core.gameengine.model.GamePhase
import com.nisiktoo.guti16.core.gameengine.model.PieceId
import com.nisiktoo.guti16.core.gameengine.state.GameState
import com.nisiktoo.guti16.core.gameengine.model.Player
import androidx.compose.ui.graphics.Color

/**
 * Represents the UI state of the game, including the list of pieces, current player, selected piece, game phase, winner, last move, and any error messages.
 * @param pieces The list of pieces currently in the game, represented as PieceUi objects for UI display.
 * @param currentPlayer The player whose turn it is to move.
 * @param selectedPieceId The ID of the currently selected piece, if any.
 * @param gamePhase The current phase of the game (e.g., normal play, capture chain, game over).
 * @param winner The player who has won the game, if the game is over.
 * @param lastMove The last move made in the game, represented as a MoveUi object for UI display.
 * @param errorMessage Any error message to display in the UI, such as invalid move errors or game state issues.
 */
data class GameUiState(
    val pieces: List<PieceUi> = emptyList(),
    val currentPlayer: Player = Player.A,
    val selectedPieceId: PieceId? = null,
    val gamePhase: GamePhase = GamePhase.NORMAL,
    val winner: Player? = null,
    val lastMove: MoveUi? = null,
    val errorMessage: String? = null,
)

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
)

/**
 * Represents a move in the UI, including the starting and ending positions, and any captured piece.
 * @param from The starting position of the move, represented by a BoardNodeId.
 * @param to The ending position of the move, represented by a BoardNodeId.
 * @param captured The piece that was captured as a result of this move, if any.
 * @property isSamePosition A helper function to check if the move is a no-op (i.e., the from and to positions are the same).
 */
data class MoveUi(
    val from: BoardNodeId,
    val to: BoardNodeId,
    val captured: BoardNode? = null,
) {
    fun isSamePosition(): Boolean = from == to
}