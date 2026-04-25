package com.nisiktoo.guti16.coredomain.factory

import androidx.compose.ui.graphics.Color
import com.nisiktoo.guti16.core.gameengine.logic.GameEngine
import com.nisiktoo.guti16.core.gameengine.model.Player
import com.nisiktoo.guti16.coredomain.model.GameSession
import com.nisiktoo.guti16.coredomain.model.PlayerAppearance
import com.nisiktoo.guti16.coredomain.model.PlayerProfile
import java.util.UUID

/** Default in-memory session creation strategy for a local match. */
class DefaultGameSessionFactory : GameSessionFactory {

    override fun create(): GameSession {
        return GameSession(
            id = UUID.randomUUID().toString(),
            gameState = GameEngine.createInitialState(),
            playerA = defaultProfile(Player.A, "Player A"),
            playerB = defaultProfile(Player.B, "Player B"),
        )
    }

    private fun defaultProfile(player: Player, name: String): PlayerProfile {
        val appearance = when (player) {
            Player.A -> PlayerAppearance(
                pieceColor = Color(0xFFE53935),
                borderColor = Color(0xFFB71C1C),
                selectedGlowColor = Color(0xFFFFCDD2),
            )

            Player.B -> PlayerAppearance(
                pieceColor = Color(0xFF1E88E5),
                borderColor = Color(0xFF0D47A1),
                selectedGlowColor = Color(0xFFBBDEFB),
            )
        }

        return PlayerProfile(
            id = UUID.randomUUID().toString(),
            name = name,
            player = player,
            appearance = appearance,
        )
    }
}

