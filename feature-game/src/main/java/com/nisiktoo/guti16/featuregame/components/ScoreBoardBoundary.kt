package com.nisiktoo.guti16.featuregame.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable
fun ScoreBoardBoundary(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(50.dp)
            .border(width = 1.dp, color = Color.Black, shape = RectangleShape)
    ) {
        Column {
            Row {
                Text(text = "Player 1", modifier = Modifier.weight(1f))
            }
            Row {

                Text(text = "Player 2", modifier = Modifier.weight(1f))
            }
        }
    }
}
