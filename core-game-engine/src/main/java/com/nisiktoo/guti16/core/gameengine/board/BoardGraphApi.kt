package com.nisiktoo.guti16.core.gameengine.board

import com.nisiktoo.guti16.core.gameengine.model.BoardNode
import com.nisiktoo.guti16.core.gameengine.model.BoardNodeId
import java.util.ArrayDeque
import kotlin.math.abs

/**
 * Public read-only façade over the 16 Guti board graph.
 *
 * This API is the ONLY entry point for game logic to interact with the board.
 * It hides graph structure and exposes move-level semantics.
 */
object BoardGraphApi {

    private val nodes: Map<BoardNodeId, BoardNode>
        get() = BoardGraph.nodes

    // -----------------------------
    // Basic graph access
    // -----------------------------

    /**
     * Returns the board node for [id].
     *
     * @param id the stable node identifier to resolve.
     * @return the matching [BoardNode].
     * @throws IllegalStateException if [id] is not part of the board.
     */
    fun getNode(id: BoardNodeId): BoardNode =
        nodes[id] ?: error("Invalid node id: ${id.value}")

    /**
     * Returns the direct neighbors for [id].
     *
     * @param id the node whose legal one-step neighbors should be returned.
     * @return immutable list of adjacent node ids in board order.
     * @throws IllegalStateException if [id] is not part of the board.
     */
    fun neighborsOf(id: BoardNodeId): List<BoardNodeId> =
        getNode(id).neighbors

    /**
     * Checks whether [to] is a legal direct neighbor of [from].
     *
     * @param from the source node id.
     * @param to the target node id.
     * @return `true` when [to] is reachable in one step from [from].
     */
    fun isAdjacent(from: BoardNodeId, to: BoardNodeId): Boolean =
        to in neighborsOf(from)

    /**
     * Returns the number of direct neighbors for [id].
     *
     * @param id the node to inspect.
     * @return count of one-step moves available from [id].
     */
    fun degree(id: BoardNodeId): Int =
        neighborsOf(id).size

    // -----------------------------
    // Move classification layer
    // -----------------------------

    enum class MoveType {
        STEP,
        JUMP,
        INVALID
    }

    /**
     * Classifies a move by board topology only.
     *
     * @param from the source node id.
     * @param to the target node id.
     * @return [MoveType.STEP] for direct adjacency, [MoveType.JUMP] for a valid
     * two-node jump line, otherwise [MoveType.INVALID].
     */
    fun classifyMove(from: BoardNodeId, to: BoardNodeId): MoveType {
        if (from == to) return MoveType.INVALID

        if (isAdjacent(from, to)) {
            return MoveType.STEP
        }

        if (middleNode(from, to) != null) {
            return MoveType.JUMP
        }

        return MoveType.INVALID
    }

    /**
     * Checks whether a move is valid by board topology alone.
     *
     * @param from the source node id.
     * @param to the target node id.
     * @return `true` when the move is either a step or a jump.
     */
    fun isValidMove(from: BoardNodeId, to: BoardNodeId): Boolean =
        classifyMove(from, to) != MoveType.INVALID

    /**
     * Checks whether [to] is a legal one-step move from [from].
     *
     * @param from the source node id.
     * @param to the target node id.
     * @return `true` when [to] is directly adjacent to [from].
     */
    fun canStepMove(from: BoardNodeId, to: BoardNodeId): Boolean =
        classifyMove(from, to) == MoveType.STEP

    /**
     * Checks whether [to] is a legal jump landing point from [from].
     *
     * @param from the source node id.
     * @param to the landing node id.
     * @return `true` when [to] is two board units away and a valid middle node exists.
     */
    fun isValidJump(from: BoardNodeId, to: BoardNodeId): Boolean =
        classifyMove(from, to) == MoveType.JUMP

    /**
     * Returns the middle node for a valid jump line from [from] to [to].
     *
     * @param from the source node id.
     * @param to the destination node id.
     * @return the unique middle node id, or `null` if the pair is not a valid jump line.
     */
    fun middleNode(from: BoardNodeId, to: BoardNodeId): BoardNodeId? =
        resolveMiddleNode(from, to)

    // -----------------------------
    // Jump logic (internal core)
    // -----------------------------

    private fun resolveMiddleNode(from: BoardNodeId, to: BoardNodeId): BoardNodeId? {
        val a = nodeAt(from) ?: return null
        val b = nodeAt(to) ?: return null

        val dr = b.row - a.row
        val dc = b.col - a.col

        val isTwoStepLine =
            (dr == 0 && abs(dc) == 2) ||
                    (dc == 0 && abs(dr) == 2) ||
                    (abs(dr) == 2 && abs(dc) == 2)

        if (!isTwoStepLine) return null

        val midRow = a.row + dr / 2
        val midCol = a.col + dc / 2

        val mid = nodeAt(midRow, midCol) ?: return null

        return if (isAdjacent(from, mid.id) && isAdjacent(mid.id, to)) {
            mid.id
        } else null
    }

    /**
     * Returns the jump landing node after crossing [middle] from [from].
     *
     * @param from the source node id.
     * @param middle the intermediate node being jumped over.
     * @return the landing node id, or `null` if the jump line is invalid.
     */
    fun getJumpTarget(from: BoardNodeId, middle: BoardNodeId): BoardNodeId? {
        val a = nodeAt(from) ?: return null
        val m = nodeAt(middle) ?: return null

        val dr = m.row - a.row
        val dc = m.col - a.col

        val targetRow = m.row + dr
        val targetCol = m.col + dc

        val target = nodeAt(targetRow, targetCol) ?: return null

        return if (isAdjacent(from, middle) && isAdjacent(middle, target.id)) {
            target.id
        } else null
    }

    // -----------------------------
    // Reachability (BFS)
    // -----------------------------

    /**
     * Checks whether [to] can be reached from [from] by repeatedly taking legal steps.
     *
     * @param from the source node id.
     * @param to the destination node id.
     * @return `true` if a path exists using one-step edges only.
     */
    fun canReach(from: BoardNodeId, to: BoardNodeId): Boolean {
        if (from == to) return true

        val visited = mutableSetOf(from)
        val queue = ArrayDeque<BoardNodeId>()
        queue.add(from)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()

            for (n in neighborsOf(current)) {
                if (!visited.add(n)) continue
                if (n == to) return true
                queue.add(n)
            }
        }

        return false
    }

    // -----------------------------
    // Debug utilities
    // -----------------------------

    /**
     * Produces a deterministic, line-based dump of the graph.
     *
     * @return multi-line text containing each node id, board coordinate, and adjacency list.
     */
    fun dumpGraph(): String =
        nodes.values
            .sortedBy { it.id.value }
            .joinToString("\n") { node ->
                val ns = node.neighbors.joinToString(",") { it.value.toString() }
                "${node.id.value}(${node.row},${node.col}) -> [$ns]"
            }

    private fun nodeAt(id: BoardNodeId): BoardNode? = nodes[id]

    private fun nodeAt(row: Int, col: Int): BoardNode? =
        nodes.values.firstOrNull { it.row == row && it.col == col }
}