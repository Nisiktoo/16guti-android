package com.nisiktoo.guti16.featuregame.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.nisiktoo.guti16.core.gameengine.logic.GameEngine
import com.nisiktoo.guti16.core.gameengine.model.GamePhase
import com.nisiktoo.guti16.core.gameengine.model.Move
import com.nisiktoo.guti16.core.gameengine.state.GameState

/**
 * ViewModel for the game screen, managing the game state and handling user interactions.
 */
class GameViewModel : ViewModel() {


    private fun GameState.toUiState(): GameUiState {
        return GameUiState(
            pieces = pieces.map { piece ->
                PieceUi(
                    id = piece.id,
                    owner = piece.owner,
                    position = piece.position,
                    isAlive = true, // This should be determined based on the game state
                )
            },
            currentPlayer = currentPlayer,
            selectedPieceId = null, // This should be set based on user interaction
            gamePhase = GamePhase.NORMAL, // This should be determined based on the game state
            winner = null, // This should be determined based on the game state
            lastMove = null, // This should be set to the last move made in the game
            errorMessage = null,
        )
    }
}