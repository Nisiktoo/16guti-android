package com.nisiktoo.guti16.core.gameengine.board

import com.nisiktoo.guti16.core.gameengine.model.BoardNodeId
import com.nisiktoo.guti16.core.gameengine.model.BoardNode

/**
 * Internal graph builder for the 16 Guti board.
 *
 * Responsibilities:
 * - Nodes represent playable intersections only.
 * - Edges represent legal single-step moves.
 * - No rendering geometry or UI concerns are stored here.
 *
 * Public callers should use [BoardGraphApi] instead of touching this builder directly.
 *
 * The graph is generated from the original game rules:
 * - Orthogonal moves are available on every playable node.
 * - Diagonal moves are available only when `(row + col) % 2 == 0`.
 * - A final pruning pass removes a fixed set of irregular edges.
 */
internal object BoardGraph {

    private const val ROWS = 9
    private const val COLS = 5

    private val ORTHOGONAL_DELTAS = listOf(
        Delta(1, 0),
        Delta(-1, 0),
        Delta(0, 1),
        Delta(0, -1)
    )

    private val DIAGONAL_DELTAS = listOf(
        Delta(1, 1),
        Delta(1, -1),
        Delta(-1, 1),
        Delta(-1, -1)
    )

    /**
     * Direction-independent edge removals translated from the legacy JS implementation.
     *
     * Each entry removes one directed edge `from -> to` after base connectivity is built.
     * This keeps the generated topology aligned with the original board layout.
     */
    private val PRUNED_EDGES = listOf(
        Cell(1, 1) to Cell(2, 0),
        Cell(1, 1) to Cell(2, 1),
        Cell(1, 1) to Cell(0, 2),

        Cell(0, 2) to Cell(1, 1),
        Cell(0, 2) to Cell(1, 3),

        Cell(1, 3) to Cell(0, 2),
        Cell(1, 3) to Cell(2, 3),
        Cell(1, 3) to Cell(2, 4),

        Cell(2, 0) to Cell(1, 1),
        Cell(2, 1) to Cell(1, 1),
        Cell(2, 3) to Cell(1, 3),
        Cell(2, 4) to Cell(1, 3),

        Cell(6, 0) to Cell(7, 1),
        Cell(6, 1) to Cell(7, 1),
        Cell(6, 3) to Cell(7, 3),
        Cell(6, 4) to Cell(7, 3),

        Cell(7, 1) to Cell(6, 0),
        Cell(7, 1) to Cell(6, 1),
        Cell(7, 1) to Cell(8, 2),

        Cell(7, 3) to Cell(6, 3),
        Cell(7, 3) to Cell(6, 4),
        Cell(7, 3) to Cell(8, 2),

        Cell(8, 2) to Cell(7, 1),
        Cell(8, 2) to Cell(7, 3)
    )

    /**
     * Immutable map of playable nodes keyed by [BoardNodeId].
     *
     * Uses the shared [BoardNode] model so graph topology has a single canonical node type.
     */
    internal val nodes: Map<BoardNodeId, BoardNode> = buildGraph()

    private data class Cell(val row: Int, val col: Int)
    private data class Delta(val dr: Int, val dc: Int)

    private fun id(row: Int, col: Int): BoardNodeId = BoardNodeId(row * COLS + col)

    /**
     * Returns whether a matrix coordinate is a playable intersection.
     *
     * Playable shape rule:
     * - Full middle rectangle at rows 2..6 (all columns)
     * - Plus side columns 1..3 on top/bottom wings
     */
    private fun isPlayable(row: Int, col: Int): Boolean {
        if (row !in 0 until ROWS || col !in 0 until COLS) return false
        if (row in 2..6) return true
        if (col in 1..3) return true
        return false
    }

    /**
     * Builds the board graph in three stages:
     * 1) create playable nodes,
     * 2) add orthogonal + parity-gated diagonal neighbors,
     * 3) prune irregular edges.
     */
    private fun buildGraph(): Map<BoardNodeId, BoardNode> {
        val cells = createCells()
        val adjacency = cells.keys.associateWith { mutableListOf<BoardNodeId>() }.toMutableMap()

        connect(adjacency, ORTHOGONAL_DELTAS) { _, _ -> true }
        connect(adjacency, DIAGONAL_DELTAS) { row, col -> (row + col) % 2 == 0 }
        applyPrunedEdges(adjacency)

        return cells.mapValues { (nodeId, cell) ->
            BoardNode(
                id = nodeId,
                neighbors = adjacency.getValue(nodeId).toList(),
                row = cell.row,
                col = cell.col
            )
        }
    }

    /** Creates coordinate metadata for every playable node in the board matrix. */
    private fun createCells(): MutableMap<BoardNodeId, Cell> {
        val map = mutableMapOf<BoardNodeId, Cell>()
        for (r in 0 until ROWS) {
            for (c in 0 until COLS) {
                if (!isPlayable(r, c)) continue
                val nodeId = id(r, c)
                map[nodeId] = Cell(r, c)
            }
        }
        return map
    }

    /**
     * Adds directed edges based on [deltas] for each playable node satisfying [enabledAt].
     */
    private fun connect(
        adjacency: MutableMap<BoardNodeId, MutableList<BoardNodeId>>,
        deltas: List<Delta>,
        enabledAt: (row: Int, col: Int) -> Boolean
    ) {
        for (r in 0 until ROWS) {
            for (c in 0 until COLS) {
                if (!isPlayable(r, c) || !enabledAt(r, c)) continue

                val fromNeighbors = adjacency.getValue(id(r, c))
                for ((dr, dc) in deltas) {
                    val nr = r + dr
                    val nc = c + dc
                    if (isPlayable(nr, nc)) {
                        fromNeighbors += id(nr, nc)
                    }
                }
            }
        }
    }

    /** Applies the fixed set of edge removals required by the official board topology. */
    private fun applyPrunedEdges(adjacency: MutableMap<BoardNodeId, MutableList<BoardNodeId>>) {
        for ((from, to) in PRUNED_EDGES) {
            adjacency[id(from.row, from.col)]?.remove(id(to.row, to.col))
        }
    }
}