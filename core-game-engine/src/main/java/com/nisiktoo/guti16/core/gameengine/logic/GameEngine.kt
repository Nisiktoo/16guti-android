package com.nisiktoo.guti16.core.gameengine.logic

import com.nisiktoo.guti16.core.gameengine.board.BoardGraphApi
import com.nisiktoo.guti16.core.gameengine.model.Move
import com.nisiktoo.guti16.core.gameengine.model.Piece
import com.nisiktoo.guti16.core.gameengine.model.PieceId
import com.nisiktoo.guti16.core.gameengine.model.Player
import com.nisiktoo.guti16.core.gameengine.state.GameState
import kotlin.uuid.Uuid.Companion.random

/**
 * Entry point for game state creation and updates.
 */
object GameEngine {

    /**
     * Initializes the game state with pieces placed according to the standard 16 Guti setup and randomly selects the starting player.
     */
    fun createInitialState(): GameState {
        val pieces = createInitialPieces()
        val occupancy = pieces.associate { it.position to it.id }
        return GameState(
            pieces = pieces,
            occupancy = occupancy,
            /* randomly select starting player */
            currentPlayer = if (kotlin.random.Random.nextBoolean()) Player.A else Player.B,
        )

    }

    fun applyMove(state: GameState, move: Move): GameState {
        TODO("Validate the move and return the updated GameState")
    }

    /** Creates the initial pieces for both players and places them on the board according to the standard 16 Guti setup. */
    private fun createInitialPieces(): List<Piece> {
        val pieces = mutableListOf<Piece>()
        var idCounter = 0
        for (node in BoardGraphApi.getAllBoardNodes()) {
            val owner = when (node.row ) {
                in 0..3 -> Player.A
                in 5..8 -> Player.B
                else -> null
            } ?: continue
            pieces.add(Piece(id = PieceId(idCounter++), owner = owner, position = node.id))
        }
        return pieces
    }
}