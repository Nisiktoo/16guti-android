package com.nisiktoo.guti16.featuregame.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nisiktoo.guti16.core.gameengine.model.BoardNodeId
import com.nisiktoo.guti16.core.gameengine.model.Player
import com.nisiktoo.guti16.featuregame.components.BoardCanvas
import com.nisiktoo.guti16.featuregame.components.ScoreBoard

@Composable
fun GameScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    viewModel: GameViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    Column (
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ){
        val playerAPiece = uiState.pieces.firstOrNull { it.owner == Player.A }
        val playerBPiece = uiState.pieces.firstOrNull { it.owner == Player.B }
        if (playerAPiece != null && playerBPiece != null) {
            ScoreBoard(
                piece1 = playerAPiece,
                score1 = uiState.capturedCountA,
                piece2 = playerBPiece,
                score2 = uiState.capturedCountB,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            BoardCanvas(
                pieces = uiState.pieces,
                onNodeClick = { nodeId ->
                    val boardNodeId = nodeId?.let { BoardNodeId(it) }
                    viewModel.onEvent(GameEvent.BoardNodeTapped(boardNodeId))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    GameScreen()
}