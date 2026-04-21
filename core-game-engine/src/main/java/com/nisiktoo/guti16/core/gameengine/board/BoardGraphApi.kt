package com.nisiktoo.guti16.core.gameengine.board

import com.nisiktoo.guti16.core.gameengine.model.BoardNode
import com.nisiktoo.guti16.core.gameengine.model.BoardNodeId
import java.util.ArrayDeque
import javax.sound.midi.MidiEvent
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

    /** Cached set of all node IDs for quick validation and iteration. */
    private val nodeIds: Set<Int> by lazy {
        nodes.keys.map { it.value }.toSet()
    }

    /** Cached maximum node ID + 1 for efficient matrix indexing. */
    private val matrixSize: Int by lazy {
        (nodes.keys.maxOfOrNull { it.value } ?: 0) + 1
    }

    /** Cached adjacency matrix for O(1) edge existence checks. */
    private val adjacencyMatrix: Array<BooleanArray> by lazy {
        Array(matrixSize) { BooleanArray(matrixSize) }
            .also { matrix ->
                for (node in nodes.values) {
                    for (neighbor in node.neighbors) {
                        matrix[node.id.value][neighbor.value] = true
                    }
                }
            }
    }

    /** Represents a valid capture path consisting of a source node, a middle node (the captured piece), and a target node.
     * @param source the starting node of the move.
     * @param middle the node representing the piece being captured.
     * @param target the destination node of the move after the capture.
     */

    data class CapturePath(
        val source: BoardNodeId,
        val middle: BoardNodeId,
        val target: BoardNodeId,
    )

    /** Cached matrix mapping pairs of nodes to the middle node for valid captures, or -1 if no capture is possible. */
    private val middleNodeMatrix: Array<IntArray> by lazy {
        Array(matrixSize) { IntArray(matrixSize) { -1 } }.also { matrix ->
            buildMiddleNodeMatrix(matrix)
        }
    }

    /** Cached list of valid capture targets for each source node, used for efficient move generation. */
    private val nodeCapturePathsOf: Array<MutableList<Int>> by lazy {
        Array(matrixSize) { mutableListOf() }
    }

    /** Retrieves the middle node ID for a valid capture move from [source] to [target], or null if no capture is possible.
     *
     * @param source the starting node of the move.
     * @param target the destination node of the move after the capture.
     * @return the middle node ID representing the captured piece, or null if no capture is possible.
     * @throws IllegalArgumentException if either [source] or [target] is not a valid node ID.
     */
    private fun buildMiddleNodeMatrix(matrix: Array<IntArray>) {
        fun register(source: BoardNodeId, middle: BoardNodeId, target: BoardNodeId) {
            matrix[source.value][target.value] = middle.value
            nodeCapturePathsOf[source.value].add(target.value)
        }

        // 1) mirrored graph-based capture paths
        for (source in nodes.values) {
            for (middleId in source.neighbors) {
                val middle = nodes[middleId] ?: continue
                val target = nodes.values.firstOrNull {
                    it.row == middle.row + (middle.row - source.row) &&
                            it.col == middle.col + (middle.col - source.col)
                } ?: continue

                if (isAdjacent(source.id, middle.id) && isAdjacent(middle.id, target.id)) {
                    register(source.id, middle.id, target.id)
                }
            }
        }

        fun registerSpecial(sr: Int, sc: Int, mr: Int, mc: Int, tr: Int, tc: Int) {
            val s = nodes.values.firstOrNull { it.row == sr && it.col == sc }?.id ?: return
            val m = nodes.values.firstOrNull { it.row == mr && it.col == mc }?.id ?: return
            val t = nodes.values.firstOrNull { it.row == tr && it.col == tc }?.id ?: return
            register(s, m, t)
        }

        registerSpecial(0, 1, 1, 1, 2, 2)
        registerSpecial(0, 3, 1, 3, 2, 2)
        registerSpecial(2, 2, 1, 1, 0, 1)
        registerSpecial(2, 2, 1, 3, 0, 3)
        registerSpecial(8, 1, 7, 1, 6, 2)
        registerSpecial(6, 2, 7, 1, 8, 1)
        registerSpecial(8, 3, 7, 3, 6, 2)
        registerSpecial(6, 2, 7, 3, 8, 3)
    }

    /** Validates that [id] is a known node in the board graph.
     *
     * @param id the node identifier to check.
     * @return `true` if [id] exists in the board graph, `false` otherwise.
     */
    private fun isValidNodeId(id: BoardNodeId): Boolean = id.value in nodeIds


    /** Checks whether [to] is directly adjacent to [from] in the board graph.
     *
     * @param from the source node id.
     * @param to the target node id.
     * @return `true` if [to] is a direct neighbor of [from], `false` otherwise.
     * @throws IllegalArgumentException if either [from] or [to] is not a valid node ID.
     */
    fun isAdjacent(from: BoardNodeId, to: BoardNodeId): Boolean {
        if (!isValidNodeId(from) || !isValidNodeId(to)) {
            throw IllegalArgumentException("Invalid node ID: from=${from.value}, to=${to.value}")
        }
        return adjacencyMatrix[from.value][to.value]
    }


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
    fun neighborsOf(id: BoardNodeId): List<BoardNodeId> = getNode(id).neighbors

    /** Checks if a valid capture move exists from [from] to [to] by verifying the presence of a middle node.
     *
     * @param from the source node id.
     * @param to the target node id.
     * @return `true` if a valid capture move exists (i.e., there is a middle node between [from] and [to]), `false` otherwise.
     * @throws IllegalArgumentException if either [from] or [to] is not a valid node ID.
     */
    fun isCaptureAdjacent(from: BoardNodeId, to: BoardNodeId): Boolean {
        if (!isValidNodeId(from) || !isValidNodeId(to)) {
            throw IllegalArgumentException("Invalid node ID: from=${from.value}, to=${to.value}")
        }
        return middleNodeMatrix[from.value][to.value] != -1
    }

    /** Retrieves the middle node ID for a valid capture move from [from] to [to], or null if no capture is possible.
     *
     * @param from the source node id.
     * @param to the target node id.
     * @return the middle node ID representing the captured piece, or null if no capture is possible.
     * @throws IllegalArgumentException if either [from] or [to] is not a valid node ID.
     */
    fun getCaptureMiddleNode(from: BoardNodeId, to: BoardNodeId): BoardNodeId? {
        if (!isValidNodeId(from) || !isValidNodeId(to)) {
            throw IllegalArgumentException("Invalid node ID: from=${from.value}, to=${to.value}")
        }
        val middleValue = middleNodeMatrix[from.value][to.value]
        return if (middleValue != -1) BoardNodeId(middleValue) else null
    }
    /** Get all the board nodes as a list. The order of nodes is not guaranteed and should not be relied upon.
     *
     * @return a list of all [BoardNode]s in the board graph.
     */
    fun getAllBoardNodes(): List<BoardNode> = nodes.values.toList()

}
