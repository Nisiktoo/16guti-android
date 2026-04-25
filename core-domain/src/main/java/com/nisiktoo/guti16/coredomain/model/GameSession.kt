package com.nisiktoo.guti16.coredomain.model
import com.nisiktoo.guti16.core.gameengine.state.GameState

/**
 * Aggregate root for one running or saved match.
 *
 * It combines engine state (GameState) with user-facing metadata.
 */
data class GameSession(
    val id: String,
    val gameState: GameState,
    val playerA: PlayerProfile,
    val playerB: PlayerProfile,
    val status: SessionStatus = SessionStatus.ACTIVE,
)

enum class SessionStatus {
    ACTIVE,
    FINISHED,
    ABANDONED,
}