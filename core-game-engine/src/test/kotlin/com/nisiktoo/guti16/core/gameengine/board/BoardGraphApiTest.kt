package com.nisiktoo.guti16.core.gameengine.board

import com.nisiktoo.guti16.core.gameengine.model.BoardNodeId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardGraphApiTest {

    private fun id(row: Int, col: Int): BoardNodeId = BoardNodeId(row * 5 + col)

    @Test
    fun `getNode returns board metadata`() {
        val node = BoardGraphApi.getNode(id(0, 2))

        assertEquals(id(0, 2), node.id)
        assertEquals(0, node.row)
        assertEquals(2, node.col)
    }

    @Test
    fun `neighborsOf exposes legal step neighbors only`() {
        val neighbors = BoardGraphApi.neighborsOf(id(0, 2))

        assertEquals(
            listOf(id(1, 2), id(0, 3), id(0, 1)),
            neighbors
        )
    }

    @Test
    fun `isAdjacent and canStepMove mirror direct neighbors`() {
        assertTrue(BoardGraphApi.isAdjacent(id(0, 2), id(0, 3)))
        assertTrue(BoardGraphApi.canStepMove(id(0, 2), id(0, 3)))
        assertFalse(BoardGraphApi.isAdjacent(id(0, 2), id(2, 2)))
    }

    @Test
    fun `middleNode and jump APIs resolve a valid capture line`() {
        val from = id(0, 1)
        val to = id(0, 3)
        val middle = BoardGraphApi.middleNode(from, to)

        assertEquals(id(0, 2), middle)
        assertTrue(BoardGraphApi.isValidJump(from, to))
        assertEquals(to, BoardGraphApi.getJumpTarget(from, id(0, 2)))
    }

    @Test
    fun `dumpGraph includes all board nodes`() {
        val dump = BoardGraphApi.dumpGraph()
        val lines = dump.lineSequence().filter { it.isNotBlank() }.toList()

        assertEquals(37, lines.size)
        assertTrue(lines.first().startsWith("1(0,1)"))
        assertTrue(lines.any { it.contains("2(0,2)") })
    }

    @Test
    fun `canReach finds a path across the board`() {
        assertTrue(BoardGraphApi.canReach(id(0, 1), id(8, 3)))
        assertNotNull(BoardGraphApi.getNode(id(8, 3)))
    }
}

