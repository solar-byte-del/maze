package com.example.maze

import java.util.Stack
import kotlin.random.Random

object MazeEngine {
    fun generateMaze(rows: Int, cols: Int, doorCount: Int): Array<Array<MazeCell>> {
        val grid = Array(rows) { r -> Array(cols) { c -> MazeCell(r, c) } }
        val visited = Array(rows) { BooleanArray(cols) }
        val stack = Stack<Point>()
        
        grid[0][0].isStart = true
        visited[0][0] = true
        stack.push(Point(0, 0))

        while (!stack.isEmpty()) {
            val curr = stack.peek()
            val neighbors = mutableListOf<Pair<Point, String>>()

            if (curr.r > 0 && !visited[curr.r - 1][curr.c]) neighbors.add(Pair(Point(curr.r - 1, curr.c), "UP"))
            if (curr.r < rows - 1 && !visited[curr.r + 1][curr.c]) neighbors.add(Pair(Point(curr.r + 1, curr.c), "DOWN"))
            if (curr.c > 0 && !visited[curr.r][curr.c - 1]) neighbors.add(Pair(Point(curr.r, curr.c - 1), "LEFT"))
            if (curr.c < cols - 1 && !visited[curr.r][curr.c + 1]) neighbors.add(Pair(Point(curr.r, curr.c + 1), "RIGHT"))

            if (neighbors.isNotEmpty()) {
                val nextMove = neighbors[Random.nextInt(neighbors.size)]
                val next = nextMove.first
                when (nextMove.second) {
                    "UP" -> grid[curr.r][curr.c].hasTopWall = false
                    "DOWN" -> grid[next.r][next.c].hasTopWall = false
                    "LEFT" -> grid[curr.r][curr.c].hasLeftWall = false
                    "RIGHT" -> grid[next.r][next.c].hasLeftWall = false
                }
                visited[next.r][next.c] = true
                stack.push(next)
            } else { stack.pop() }
        }
        grid[rows - 1][cols - 1].isFinish = true

        val actualDoors = minOf(doorCount, KeyColor.values().size)
        for (i in 0 until actualDoors) {
            val color = KeyColor.values()[i]
            val doorRow = Random.nextInt(rows / 2, rows)
            grid[doorRow][cols - 1].doorColor = color
            
            var keyPlaced = false
            var attempts = 0
            while (!keyPlaced && attempts < 50) {
                attempts++
                val kr = Random.nextInt(0, rows / 2)
                val kc = Random.nextInt(0, cols / 2)
                val cell = grid[kr][kc]
                if (!cell.isStart && cell.keyColor == null && cell.doorColor == null) {
                    cell.keyColor = color
                    keyPlaced = true
                }
            }
        }
        return grid
    }
}
