package com.nisiktoo.guti16.featuregame.presentation

import com.nisiktoo.guti16.core.gameengine.model.BoardNodeId
/**
 * Represents user interactions with the game board.
 * Events are dispatched from the UI layer to the ViewModel for handling.
 */
sealed interface GameEvent {
    /**
     * User tapped on a board node (either empty or occupied).
     */
    data class BoardNodeTapped(val nodeId: BoardNodeId?) : GameEvent

    /**
     * Resets the game to its initial state.
     */
    data object ResetGame : GameEvent

    /**
     * Undoes the last move made.
     */
    data object UndoMove : GameEvent
}