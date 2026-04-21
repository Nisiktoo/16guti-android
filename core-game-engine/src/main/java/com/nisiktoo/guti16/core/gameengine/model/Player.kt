package com.nisiktoo.guti16.core.gameengine.model

/**
 * Represents a side in a two-player 16 Guti match.
 */
enum class Player {
    A,
    B;

    /** Returns the opponent of this player. */
    fun opponent(): Player = if (this == A) B else A
}