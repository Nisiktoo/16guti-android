package com.nisiktoo.guti16.coredomain.factory

import com.nisiktoo.guti16.coredomain.model.GameSession

/** Creates a new game session with initialized state and player profiles. */
fun interface GameSessionFactory {
    fun create(): GameSession
}

