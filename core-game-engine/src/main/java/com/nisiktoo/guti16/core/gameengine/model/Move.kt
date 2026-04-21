package com.nisiktoo.guti16.core.gameengine.model

data class Move(
    val from: BoardNodeId,
    val to: BoardNodeId
) {
    fun isSamePosition(): Boolean = from == to
}