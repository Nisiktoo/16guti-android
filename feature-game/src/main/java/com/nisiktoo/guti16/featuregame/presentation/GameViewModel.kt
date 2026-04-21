package com.nisiktoo.guti16.featuregame.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.nisiktoo.guti16.core.gameengine.logic.GameEngine
import com.nisiktoo.guti16.core.gameengine.model.Move

class GameViewModel : ViewModel() {

    var uiState by mutableStateOf(
        GameUiState(gameState = GameEngine.createInitialState())
    )
        private set

    fun onMove(move: Move) {
        uiState = uiState.copy(
            gameState = GameEngine.applyMove(uiState.gameState, move)
        )
    }

    fun resetGame() {
        uiState = GameUiState(gameState = GameEngine.createInitialState())
    }
}