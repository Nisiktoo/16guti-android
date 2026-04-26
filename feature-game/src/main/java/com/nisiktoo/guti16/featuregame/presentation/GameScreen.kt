package com.nisiktoo.guti16.featuregame.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nisiktoo.guti16.core.gameengine.model.BoardNodeId
import com.nisiktoo.guti16.featuregame.components.BoardCanvas

@Composable
fun GameScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    viewModel: GameViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    Column (
        modifier = modifier.padding(16.dp)
    ){
        BoardCanvas(pieces = uiState.pieces,
            onNodeClick = { nodeId ->
                val boardNodeId = nodeId?.let { BoardNodeId(it) }
                viewModel.onEvent(GameEvent.BoardNodeTapped(boardNodeId))
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    GameScreen()
}