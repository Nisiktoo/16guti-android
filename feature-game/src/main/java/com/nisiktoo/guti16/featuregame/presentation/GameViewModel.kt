package com.nisiktoo.guti16.featuregame.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import com.nisiktoo.guti16.core.gameengine.model.Player
import com.nisiktoo.guti16.core.gameengine.state.GameState
import com.nisiktoo.guti16.coredomain.model.GameSession
import com.nisiktoo.guti16.coredomain.model.PlayerAppearance
import com.nisiktoo.guti16.coredomain.usecase.CreateGameSessionUseCase
import com.nisiktoo.guti16.core.gameengine.board.BoardGraphApi
import com.nisiktoo.guti16.core.gameengine.logic.GameEngine
import com.nisiktoo.guti16.core.gameengine.model.BoardNode

/**
 * ViewModel for the game screen, managing the game state and handling user interactions.
 */
class GameViewModel : ViewModel() {
    fun onEvent(event: GameEvent) {
        when(event) {
            // feature-game/src/main/java/.../presentation/GameViewModel.kt
            is GameEvent.BoardNodeTapped -> {
                val nodeId = event.nodeId
                if (nodeId == null) {
                    println("No node is selected")
                } else {
                    println("Node tapped: $nodeId")
                }
                println("Called")
                // 1. Update selection via GameEngine
                val nextGameState = GameEngine.selectNode(gameSession.gameState, nodeId)

                // 2. Update the session with new state
                gameSession = gameSession.copy(gameState = nextGameState)

                // 3. Sync UI state
                uiState = gameSession.toUiState()
            }
            GameEvent.ResetGame -> {
                /* To Do */
            }
            GameEvent.UndoMove -> {
                /* To Do */
            }
            else -> {
                println("An event with no care")
            }
        }
    }

    private val createGameSessionUseCase = CreateGameSessionUseCase()

    private var gameSession = createGameSessionUseCase()

    var uiState by mutableStateOf(gameSession.toUiState())
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