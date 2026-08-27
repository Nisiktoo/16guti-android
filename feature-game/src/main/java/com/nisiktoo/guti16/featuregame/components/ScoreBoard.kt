package com.nisiktoo.guti16.featuregame.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.nisiktoo.guti16.coreui.theme.PieceTheme
import com.nisiktoo.guti16.featuregame.presentation.PieceUi

@Composable
fun ScoreBoard(
    piece1: PieceUi,
    score1: Int,
    piece2: PieceUi,
    score2: Int,
    modifier: Modifier = Modifier,
) {
    // Determine a size for the piece representation
    val pieceRadius = PieceTheme().pieceRadius
    val displaySize = (pieceRadius * 2).dp

    Column(modifier = modifier.padding(1.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(displaySize)) {
                drawPiece(piece1)
            }
            Text(
                text = "$score1",
                modifier = Modifier.padding(start = 1.dp)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(displaySize)) {
                drawPiece(piece2)
            }
            Text(
                text = "$score2",
                modifier = Modifier.padding(start = 1.dp)
            )
        }
    }
}

private fun DrawScope.drawPiece(piece: PieceUi) {
    val pieceRadius = PieceTheme().pieceRadius
    drawCircle(
        color = piece.pieceColor,
        radius = pieceRadius,
        center = center,
    )
}