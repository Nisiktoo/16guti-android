package com.nisiktoo.guti16.featuregame.presentation

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.nisiktoo.guti16.core.gameengine.logic.GameEngine
import com.nisiktoo.guti16.core.gameengine.model.MoveSound
import com.nisiktoo.guti16.core.gameengine.model.Player
import com.nisiktoo.guti16.core.gameengine.state.GameState
import com.nisiktoo.guti16.coredomain.model.GameSession
import com.nisiktoo.guti16.coredomain.model.PlayerAppearance
import com.nisiktoo.guti16.coredomain.usecase.CreateGameSessionUseCase
import com.nisiktoo.guti16.featuregame.audio.GameSoundPlayer

/**
 * ViewModel for the game screen, managing the game state and handling user interactions.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val createGameSessionUseCase = CreateGameSessionUseCase()

    private var gameSession = createGameSessionUseCase()
    private val soundPlayer = GameSoundPlayer(application)

    var uiState by mutableStateOf(gameSession.toUiState())

    fun onEvent(event: GameEvent) {
        when(event) {
            is GameEvent.BoardNodeTapped -> {
                val nodeId = event.nodeId
                
                // 1. Update selection via GameEngine
                val nextGameState = GameEngine.selectNode(gameSession.gameState, nodeId)

                // 2. Play sound if needed
                if (nextGameState.lastMoveSound != MoveSound.NONE) {
                    soundPlayer.playSound(nextGameState.lastMoveSound)
                    // Reset sound state after playing to prevent duplicate triggers
                    nextGameState.lastMoveSound = MoveSound.NONE
                }

                // 3. Update the session with new state
                gameSession = gameSession.copy(gameState = nextGameState)

                // 4. Sync UI state
                uiState = gameSession.toUiState()
            }
            GameEvent.ResetGame -> {
                gameSession = createGameSessionUseCase()
                uiState = gameSession.toUiState()
            }
            GameEvent.UndoMove -> {
                /* To Do: Implement undo logic */
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundPlayer.release()
    }
}

private fun GameSession.toUiState(): GameUiState {
    val styleByPlayer = mapOf(
        playerA.player to playerA.appearance,
        playerB.player to playerB.appearance,
    )

    return gameState.toUiState(styleByPlayer)
}

/** Converts the game state to the UI state.
 * @param styleByPlayer A map of player to their appearance style.
 * @return The UI state representing the current game state.
 */
private fun GameState.toUiState(styleByPlayer: Map<Player, PlayerAppearance>): GameUiState {
    return GameUiState(
        pieces = pieces.map { piece ->
            val style = requireNotNull(styleByPlayer[piece.owner]) {
                "Missing player style for ${piece.owner}"
            }
            PieceUi(
                id = piece.id,
                owner = piece.owner,
                position = piece.position,
                isAlive = piece.isAlive,
                pieceColor = style.pieceColor,
                borderColor = style.borderColor,
                selectedGlowColor = style.selectedGlowColor,
                isSelected = piece.id == selectedPiece,
            )
        },
        currentPlayer = currentPlayer,
        selectedPieceId = selectedPiece,
        gamePhase = gamePhase,
        winner = winner,
        lastMove = null,
        errorMessage = null,
    )
}
