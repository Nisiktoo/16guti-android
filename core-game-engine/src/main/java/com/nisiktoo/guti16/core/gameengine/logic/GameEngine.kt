package com.nisiktoo.guti16.core.gameengine.logic

import com.nisiktoo.guti16.core.gameengine.board.BoardGraphApi
import com.nisiktoo.guti16.core.gameengine.model.BoardNodeId
import com.nisiktoo.guti16.core.gameengine.model.GamePhase
import com.nisiktoo.guti16.core.gameengine.model.Move
import com.nisiktoo.guti16.core.gameengine.model.Piece
import com.nisiktoo.guti16.core.gameengine.model.PieceId
import com.nisiktoo.guti16.core.gameengine.model.Player
import com.nisiktoo.guti16.core.gameengine.state.GameState
import com.nisiktoo.guti16.core.gameengine.model.MoveSound

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

    fun performStepMove(state: GameState, from: BoardNodeId?, to: BoardNodeId?): GameState {

        /* select another piece of the current player */
        if (state.isOccupied(to)) {
            if (state.getPieceOwner(state.pieceAt(to)!!) == state.currentPlayer) {
                return state.copy(
                    selectedNode = to,
                    selectedPiece = state.pieceAt(to)
                )
            }
            return state
        }
//        println("Attempting step move from ${from} to $to")
        if (!BoardGraphApi.isAdjacent(from!!, to!!)) return state
        val pieceId = state.pieceAt(from) ?: return state
        val newOccupancy = state.occupancy.toMutableMap().apply {
            remove(from)
            put(to, pieceId)
        }
        // Update pieces list
        val newPieces = state.pieces.map { piece ->
            when (piece.id) {
                pieceId -> piece.copy(position = to)
                else -> piece
            }
        }
        return state.copy(
            pieces = newPieces,
            occupancy = newOccupancy,
            selectedNode = null,
            selectedPiece = null,
            gamePhase = GamePhase.NORMAL,
            currentPlayer = if (state.currentPlayer == Player.A) Player.B else Player.A,
            lastMoveSound = MoveSound.MOVE,
        )
    }
    /**
     * Executes a capture move and returns the updated GameState.
     */
    fun performCapture(state: GameState, from: BoardNodeId, to: BoardNodeId): GameState {
        val middleNodeId = BoardGraphApi.getCaptureMiddleNodeId(from, to) ?: return state
        val pieceId = state.pieceAt(from) ?: return state
        val capturedPieceId = state.pieceAt(middleNodeId) ?: return state

        // Update occupancy map
        val newOccupancy = state.occupancy.toMutableMap().apply {
            remove(from)
            remove(middleNodeId)
            put(to, pieceId)
        }


        // Update pieces list
        val newPieces = state.pieces.map { piece ->
            when (piece.id) {
                pieceId -> piece.copy(position = to)
                capturedPieceId -> piece.copy(isAlive = false, position = null)
                else -> piece
            }
        }

        // Return updated state
        return state.copy(
            pieces = newPieces,
            occupancy = newOccupancy,
            capturedCountA = if (state.currentPlayer == Player.A) state.capturedCountA + 1 else state.capturedCountA,
            capturedCountB = if (state.currentPlayer == Player.B) state.capturedCountB + 1 else state.capturedCountB,
            selectedNode = to, // Update selected node to the new position for potential chain captures
            lastMoveSound = MoveSound.CAPTURE,
        )
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


    fun selectNode(state: GameState, nodeId: BoardNodeId?): GameState {
        val nstate = state.copy(lastMoveSound = MoveSound.NONE)
        if (nodeId == null) {
            /* Deselect */
            if (nstate.gamePhase == GamePhase.SELECTED) {
                return nstate.copy(
                    selectedPiece = null,
                    selectedNode = null,
                    gamePhase = GamePhase.NORMAL,
                )
            }
            return nstate
        }

        /* no node is already selected */
        if (nstate.gamePhase == GamePhase.NORMAL) {
            val pieceId = nstate.pieceAt(nodeId) ?: return nstate
            if (nstate.currentPlayer != nstate.getPieceOwner(pieceId)) return nstate
            return nstate.copy(
                selectedPiece = pieceId,
                selectedNode = nodeId,
                gamePhase = GamePhase.SELECTED,
                lastMoveSound = MoveSound.NONE,
            )
        }
        // current gamePhase is either selected or capture chain

        val targetNodeId = nodeId
        if (canCapture(nstate, nstate.selectedNode!!, targetNodeId)) {
            val newState = performCapture(nstate, nstate.selectedNode, targetNodeId)
            if (canCaptureAnotherPiece(newState, targetNodeId)) {
                return newState.copy(
                    gamePhase = GamePhase.CAPTURE_CHAIN,
                )
            } else {
                return newState.copy(
                    selectedPiece = null,
                    selectedNode = null,
                    gamePhase = GamePhase.NORMAL,
                    currentPlayer = if (newState.currentPlayer == Player.A) Player.B else Player.A,
                )
            }
        }
        if (nstate.gamePhase == GamePhase.SELECTED) {
            return performStepMove(nstate, nstate.selectedNode, targetNodeId)
        }


        return nstate
    }

    /**
     * returns true if it is possible to capture by going to destination node from current selected node
     */
    fun canCapture(state: GameState, currentNodeId: BoardNodeId, destinationNodeId: BoardNodeId): Boolean {
        val currentPlayer = state.getPieceOwner(state.selectedPiece!!)
        // if it is not a valid capture move
        val middleNodeId = BoardGraphApi.getCaptureMiddleNodeId(currentNodeId, destinationNodeId) ?: return false
        val middlePieceId = state.pieceAt(middleNodeId) ?: return false
        if (state.isOccupied( destinationNodeId)) return false
        return state.getPieceOwner(middlePieceId) != currentPlayer
    }

    /**
     * returns true if it is possible to capture another Piece from current selected node
     */
    fun canCaptureAnotherPiece(state: GameState, currentNodeId: BoardNodeId): Boolean {
        val currentPlayer = state.getPieceOwner(state.selectedPiece!!)
        val destinationNodes = BoardGraphApi.getCaptureNodesOf(currentNodeId)
        for (destinationNodeId in destinationNodes) {
            if (state.isOccupied(BoardNodeId(destinationNodeId))) continue;
            val middleNodeId = BoardGraphApi.getCaptureMiddleNodeId(currentNodeId, BoardNodeId(destinationNodeId))
            if (state.isOccupied(middleNodeId)) {
                val middlePieceId = state.pieceAt(middleNodeId) ?: continue
                if (state.getPieceOwner(middlePieceId) != currentPlayer) {
                    return true
                }
            }
        }
        return false;
    }
}