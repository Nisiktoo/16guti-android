package com.nisiktoo.guti16.core.gameengine.state

import com.nisiktoo.guti16.core.gameengine.model.BoardNodeId
import com.nisiktoo.guti16.core.gameengine.model.Piece
import com.nisiktoo.guti16.core.gameengine.model.Player
import com.nisiktoo.guti16.core.gameengine.model.GamePhase
import com.nisiktoo.guti16.core.gameengine.model.PieceId
import com.nisiktoo.guti16.core.gameengine.model.MoveSound

/**
 * Represents the current state of the game, including the positions of pieces on the board, the current player, and other relevant information.
 * @property occupancy A mapping of board node IDs to the pieces currently occupying those nodes.
 * @property currentPlayer The player whose turn it is to move.
 * @property gamePhase The current phase of the game (e.g., normal play, capture chain, game over).
 * @property selectedPiece The ID of the currently selected piece, if any.
 * @property capturedCountA The number of pieces captured by player A.
 * @property capturedCountB The number of pieces captured by player B.
 * @property winner The player who has won the game, if the game is over.
 */
data class GameState(
    val pieces: List<Piece>,
    val occupancy: Map<BoardNodeId?, PieceId?>,
    val currentPlayer: Player,
    val gamePhase: GamePhase = GamePhase.NORMAL,
    val selectedPiece: PieceId? = null,
    val selectedNode: BoardNodeId? = null,
    val capturedCountA: Int = 0,
    val capturedCountB: Int = 0,
    val winner: Player? = null,
    var lastMoveSound: MoveSound = MoveSound.NONE,
    ) {
    /** Retrieves the piece at the specified board node, or null if the node is unoccupied.
     * @param nodeId The ID of the board node to check for a piece.
     * @return The piece occupying the specified node, or null if the node is unoccupied
     */
    fun pieceAt(nodeId: BoardNodeId?): PieceId? = occupancy[nodeId]

    /** Checks if the specified board node is currently occupied by a piece.
     * @param nodeId The ID of the board node to check for occupancy.
     * @return True if the node is occupied by a piece, false if it is unoccupied.
     */
    fun isOccupied(nodeId: BoardNodeId?): Boolean = occupancy.containsKey(nodeId) && occupancy[nodeId] != null

    /** Retrieves a list of all pieces currently owned by the specified player.
     * @param player The player whose pieces to retrieve.
     * @return A list of pieces owned by the specified player.
     */
    fun piecesOfPlayer(player: Player, aliveOnly: Boolean = true): List<Piece> {
        return pieces.filter { it.owner == player && (!aliveOnly || it.isAlive) }
    }

    /** Counts the number of pieces currently owned by the specified player that are still alive (not captured).
     * @param player The player whose alive pieces to count.
     * @return The count of alive pieces owned by the specified player.
     */
    fun aliveCountOfPlayer(player: Player): Int = piecesOfPlayer(player).count { it.isAlive }

    /** Retrieves the owner of the piece with the specified ID.
     * @param pieceId The ID of the piece to retrieve the owner for.
     * @return The player who owns the specified piece.
     */
    fun getPieceOwner(pieceId: PieceId): Player = pieces[pieceId.value].owner

}