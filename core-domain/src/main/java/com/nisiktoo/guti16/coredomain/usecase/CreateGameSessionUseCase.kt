package com.nisiktoo.guti16.coredomain.usecase

import com.nisiktoo.guti16.coredomain.factory.DefaultGameSessionFactory
import com.nisiktoo.guti16.coredomain.factory.GameSessionFactory
import com.nisiktoo.guti16.coredomain.model.GameSession

/** Use case that creates a fresh game session for a new match. */
class CreateGameSessionUseCase(
    private val sessionFactory: GameSessionFactory = DefaultGameSessionFactory(),
) {
    operator fun invoke(): GameSession = sessionFactory.create()
}

