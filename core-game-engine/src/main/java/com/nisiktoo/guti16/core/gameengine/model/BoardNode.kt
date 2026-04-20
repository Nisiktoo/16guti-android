package com.nisiktoo.guti16.core.gameengine.model
/**
 * Unique identifier for a BoardNode in the 16 Guti game board.
 *
 * Each BoardNodeId represents a fixed intersection point on the board graph.
 * It is used to reference nodes efficiently instead of using raw coordinates.
 *
 * This ID is stable throughout the game and does not change during gameplay.
 */
data class BoardNodeId(val value: Int)
/**
 * Represents a single intersection point on the 16 Guti board.
 *
 * A BoardNode is a valid position where a piece (guti) can be placed.
 * The entire board is modeled as a graph of BoardNodes connected by edges.
 *
 * Each node:
 * - Has a unique identifier (BoardNodeId)
 * - Maintains a list of neighboring nodes that define valid movement paths
 *
 *
 * BoardNodes are static and do not change during the game.
 * They describe the structure of the board, not the game state.
 *
 * Pieces are placed on BoardNodes, and all movement and capture logic
 * operates through transitions between these nodes.
 * @param id Unique identifier of this board node.
 * @param neighbors List of adjacent node IDs that define valid movement paths.
 * @param row Matrix row index used for UI rendering and coordinate mapping.
 * @param col Matrix column index used for UI rendering and coordinate mapping.
 */
data class BoardNode(
    val id: BoardNodeId,
    val neighbors: List<BoardNodeId>,
    val row: Int,
    val col: Int,
)