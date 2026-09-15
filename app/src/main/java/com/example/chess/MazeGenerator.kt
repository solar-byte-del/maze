package com.example.chess

import java.util.Stack
import kotlin.random.Random

fun generateMazeWithQuest(rows: Int, cols: Int, doorCount: Int): Array<Array<MazeCell>> {
    val grid = Array(rows) { r -> Array(cols) { c -> MazeCell(r, c) } }
    val visited = Array(rows) { BooleanArray(cols) }
    val stack = Stack<Point>()
    val startPoint = Point(0, 0)
    
    grid[0][0].isStart = true
    visited[0][0] = true
    stack.push(startPoint)
    
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
        } else {
            stack.pop()
        }
    }
    
    val finishPoint = Point(rows - 1, cols - 1)
    grid[finishPoint.r][finishPoint.c].isFinish = true
    
    val solutionPath = findPath(startPoint, finishPoint, grid, rows, cols)
    val actualDoors = minOf(doorCount, KeyColor.values().size, maxOf(1, solutionPath.size / 3))
    
    if (actualDoors > 0) {
        val shuffledColors = KeyColor.values().take(actualDoors).shuffled()
        val doorIndices = mutableListOf<Int>()
        val step = maxOf(1, solutionPath.size / (actualDoors + 1))
        
        for (i in 1..actualDoors) {
            doorIndices.add(minOf(i * step, solutionPath.size - 2))
        }
        
        var prevDoorIndex = 0
        for (i in 0 until actualDoors) {
            val color = shuffledColors[i]
            val doorIndex = doorIndices[i]
            val doorPos = solutionPath[doorIndex]
            
            grid[doorPos.r][doorPos.c].doorColor = color
            
            val availablePointsForKey = mutableListOf<Point>()
            for (j in prevDoorIndex until doorIndex) {
                availablePointsForKey.addAll(collectSubtreePoints(solutionPath[j], grid, rows, cols))
            }
            
            val cleanPoints = availablePointsForKey.filter { 
                grid[it.r][it.c].keyColor == null && grid[it.r][it.c].doorColor == null && !grid[it.r][it.c].isStart 
            }
            
            if (cleanPoints.isNotEmpty()) {
                val keyPos = cleanPoints[Random.nextInt(cleanPoints.size)]
                grid[keyPos.r][keyPos.c].keyColor = color
            } else {
                val fallbackPos = solutionPath[maxOf(prevDoorIndex, doorIndex - 1)]
                grid[fallbackPos.r][fallbackPos.c].keyColor = color
            }
            prevDoorIndex = doorIndex + 1
        }
    }
    return grid
}

private fun findPath(start: Point, finish: Point, grid: Array<Array<MazeCell>>, rows: Int, cols: Int): List<Point> {
    val visited = Array(rows) { BooleanArray(cols) }
    val path = mutableListOf<Point>()
    
    fun dfs(curr: Point): Boolean {
        if (curr == finish) {
            path.add(curr)
            return true
        }
        visited[curr.r][curr.c] = true
        path.add(curr)
        
        if (curr.r > 0 && !visited[curr.r - 1][curr.c] && !grid[curr.r][curr.c].hasTopWall && dfs(Point(curr.r - 1, curr.c))) return true
        if (curr.r < rows - 1 && !visited[curr.r + 1][curr.c] && !grid[curr.r + 1][curr.c].hasTopWall && dfs(Point(curr.r + 1, curr.c))) return true
        if (curr.c > 0 && !visited[curr.r][curr.c - 1] && !grid[curr.r][curr.c].hasLeftWall && dfs(Point(curr.r, curr.c - 1))) return true
        if (curr.c < cols - 1 && !visited[curr.r][curr.c + 1] && !grid[curr.r][curr.c + 1].hasLeftWall && dfs(Point(curr.r, curr.c + 1))) return true
        
        path.removeAt(path.size - 1)
        return false
    }
    dfs(start)
    return path
}

private fun collectSubtreePoints(root: Point, grid: Array<Array<MazeCell>>, rows: Int, cols: Int): Set<Point> {
    val points = mutableSetOf<Point>()
    val visited = mutableSetOf<Point>()
    val queue = Stack<Point>()
    
    queue.push(root)
    while (!queue.isEmpty()) {
        val curr = queue.pop()
        if (curr in visited) continue
        visited.add(curr)
        points.add(curr)
        
        if (curr.r > 0 && !grid[curr.r][curr.c].hasTopWall && Point(curr.r - 1, curr.c) !in visited) queue.push(Point(curr.r - 1, curr.c))
        if (curr.r < rows - 1 && !grid[curr.r + 1][curr.c].hasTopWall && Point(curr.r + 1, curr.c) !in visited) queue.push(Point(curr.r + 1, curr.c))
        if (curr.c > 0 && !grid[curr.r][curr.c].hasLeftWall && Point(curr.r, curr.c - 1) !in visited) queue.push(Point(curr.r, curr.c - 1))
        if (curr.c < cols - 1 && !grid[curr.r][curr.c + 1].hasLeftWall && Point(curr.r, curr.c + 1) !in visited) queue.push(Point(curr.r, curr.c + 1))
    }
    return points
}
