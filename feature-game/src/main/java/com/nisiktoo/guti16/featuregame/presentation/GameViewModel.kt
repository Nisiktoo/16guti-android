package com.nisiktoo.guti16.featuregame.presentation

import androidx.lifecycle.ViewModel
import com.nisiktoo.guti16.core.gameengine.state.GameState
import com.nisiktoo.guti16.coredomain.model.GameSession
import com.nisiktoo.guti16.coredomain.model.PlayerAppearance
import com.nisiktoo.guti16.coredomain.usecase.CreateGameSessionUseCase

/**
 * ViewModel for the game screen, managing the game state and handling user interactions.
 */
class GameViewModel : ViewModel() {
    private val createGameSessionUseCase = CreateGameSessionUseCase()
    private val gameSession = createGameSessionUseCase()

    val uiState: GameUiState = gameSession.toUiState()
}

private fun GameSession.toUiState(): GameUiState {
    val styleByPlayer = mapOf(
        playerA.player to playerA.appearance,
        playerB.player to playerB.appearance,
    )

    return gameState.toUiState(styleByPlayer)
}

private fun GameState.toUiState(styleByPlayer: Map<com.nisiktoo.guti16.core.gameengine.model.Player, PlayerAppearance>): GameUiState {
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