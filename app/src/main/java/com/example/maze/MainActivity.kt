package com.example.maze

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import java.util.Stack
import kotlin.math.abs
import kotlin.random.Random

enum class KeyColor(val label: String, val colorInt: Int) {
    RED("Красный", Color.rgb(239, 83, 80)),
    BLUE("Синий", Color.rgb(66, 165, 245)),
    GREEN("Зеленый", Color.rgb(102, 187, 106)),
    YELLOW("Желтый", Color.rgb(255, 238, 88)),
    ORANGE("Оранжевый", Color.rgb(255, 167, 38))
}

data class Point(val r: Int, val c: Int)

class MazeCell(val r: Int, val c: Int) {
    var hasLeftWall = true
    var hasTopWall = true
    var isStart = false
    var isFinish = false
    var keyColor: KeyColor? = null
    var doorColor: KeyColor? = null
}

class MainActivity : android.app.Activity() {
    private lateinit var mainLayout: LinearLayout
    private var rows = 11
    private var cols = 11
    private var doorCount = 2
    private var isWon = false
    private lateinit var gameView: MazeGameView

    override fun onCreate(savedInstanceState: Bundle?) {
        // Жестко отключаем заголовок ActionBar программно из кода до вызова супер-класса
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        
        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(18, 18, 18))
        }
        setContentView(mainLayout)

        mainLayout.post {
            startNewGame()
        }
    }

    fun startNewGame() {
        mainLayout.removeAllViews()
        isWon = false
        val grid = generateMaze(rows, cols, doorCount)
        
        gameView = MazeGameView(this, grid, rows, cols, 
            onWin = { 
                isWon = true
                gameView.invalidate()
            },
            onResize = { newSize, newDoors ->
                rows = newSize
                cols = newSize
                doorCount = newDoors
                startNewGame()
            }
        )
        mainLayout.addView(gameView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun generateMaze(rows: Int, cols: Int, doorCount: Int): Array<Array<MazeCell>> {
        val grid = Array(rows) { r -> Array(cols) { c -> MazeCell(r, c) } }
        val visited = Array(rows) { BooleanArray(cols) }
        val stack = Stack<Point>()
        
        grid.isStart = true
        visited = true
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
            while (!keyPlaced && attempts < 100) {
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

class MazeGameView(
    context: Context, 
    private val maze: Array<Array<MazeCell>>, 
    private val rows: Int, 
    private val cols: Int,
    private val onWin: () -> Unit,
    private val onResize: (Int, Int) -> Unit
) : View(context) {

    private var player = Point(0, 0)
    private val inventory = mutableListOf<KeyColor>()
    private val openedDoors = mutableListOf<KeyColor>()
    private var isGameFinished = false
    
    private val wallPaint = Paint().apply { color = Color.WHITE; strokeWidth = 6f }
    private val startPaint = Paint().apply { color = Color.argb(70, 0, 255, 0) }
    private val finishPaint = Paint().apply { color = Color.argb(70, 255, 0, 0) }
    private val playerPaint = Paint().apply { color = Color.rgb(41, 182, 246) }
    private val textPaint = Paint().apply { color = Color.WHITE; textSize = 40f; isAntiAlias = true }
    private val btnPaint = Paint().apply { color = Color.DKGRAY }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (isGameFinished) return false
            val diffX = e2.x - (e1?.x ?: 0f)
            val diffY = e2.y - (e1?.y ?: 0f)
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > 80) { if (diffX > 0) movePlayer(0, 1) else movePlayer(0, -1) }
            } else {
                if (abs(diffY) > 80) { if (diffY > 0) movePlayer(1, 0) else movePlayer(-1, 0) }
            }
            return true
        }
    })

    fun movePlayer(dr: Int, dc: Int) {
        val nr = player.r + dr
        val nc = player.c + dc
        if (nr in 0 until rows && nc in 0 until cols) {
            val currCell = maze[player.r][player.c]
            val targetCell = maze[nr][nc]
            var canMove = false

            if (dr == -1 && !currCell.hasTopWall) canMove = true
            if (dr == 1 && !targetCell.hasTopWall) canMove = true
            if (dc == -1 && !currCell.hasLeftWall) canMove = true
            if (dc == 1 && !targetCell.hasLeftWall) canMove = true

            if (canMove) {
                val door = targetCell.doorColor
                if (door != null && !openedDoors.contains(door)) {
                    if (inventory.contains(door)) {
                        openedDoors.add(door)
                        inventory.remove(door)
                    } else { return }
                }
                player = Point(nr, nc)
                targetCell.keyColor?.let { inventory.add(it); targetCell.keyColor = null }
                invalidate()
                if (targetCell.isFinish) {
                    isGameFinished = true
                    onWin()
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_DOWN) {
            val ex = event.x
            val ey = event.y
            val h = height.toFloat()

            if (ey > h - 180 && ey < h - 40) {
                if (ex > 20 && ex < 220) onResize(5, 1)
                if (ex > 240 && ex < 440) onResize(15, 3)
                if (ex > 460 && ex < 660) onResize(30, 5)
                if (isGameFinished && ex > 680) (context as MainActivity).startNewGame()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(18, 18, 18))

        val usableHeight = height - 220f
        val cellSize = minOf(width / cols.toFloat(), usableHeight / rows.toFloat())
        val offsetX = (width - cellSize * cols) / 2
        val offsetY = (usableHeight - cellSize * rows) / 2

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = maze[r][c]
                val x = offsetX + c * cellSize
                val y = offsetY + r * cellSize

                if (cell.isStart) canvas.drawRect(x, y, x + cellSize, y + cellSize, startPaint)
                if (cell.isFinish) canvas.drawRect(x, y, x + cellSize, y + cellSize, finishPaint)
                
                cell.keyColor?.let {
                    val p = Paint().apply { color = it.colorInt }
                    canvas.drawCircle(x + cellSize/2, y + cellSize/2, cellSize/5, p)
                }
                
                cell.doorColor?.let {
                    if (!openedDoors.contains(it)) {
                        val p = Paint().apply { color = it.colorInt }
                        canvas.drawRect(x + cellSize*0.15f, y + cellSize*0.15f, x + cellSize*0.85f, y + cellSize*0.85f, p)
                    }
                }

                if (cell.hasTopWall) canvas.drawLine(x, y, x + cellSize, y, wallPaint)
                if (cell.hasLeftWall) canvas.drawLine(x, y, x, y + cellSize, wallPaint)
            }
        }

        canvas.drawLine(offsetX, offsetY + rows * cellSize, offsetX + cols * cellSize, offsetY + rows * cellSize, wallPaint)
canvas.drawLine(offsetX + cols * cellSize, offsetY, offsetX + cols * cellSize, offsetY + rows * cellSize, wallPaint)val px = offsetX + player.c * cellSize + cellSize / 2val py = offsetY + player.r * cellSize + cellSize / 2canvas.drawCircle(px, py, cellSize / 3.5f, playerPaint)val h = height.toFloat()canvas.drawText("Ключей в кармане: ${inventory.size}", 40f, h - 240f, textPaint)if (isGameFinished) {val winPaint = Paint().apply { color = Color.YELLOW; textSize = 50f }canvas.drawText("ПОБЕДА! 🏆", width / 2f - 120f, h - 240f, winPaint)}canvas.drawRect(20f, h - 180f, 220f, h - 40f, btnPaint)canvas.drawText("5 x 5", 65f, h - 95f, textPaint)canvas.drawRect(240f, h - 180f, 440f, h - 40f, btnPaint)canvas.drawText("15x15", 275f, h - 95f, textPaint)canvas.drawRect(460f, h - 180f, 660f, h - 40f, btnPaint)canvas.drawText("30x30", 495f, h - 95f, textPaint)if (isGameFinished) {val activeBtn = Paint().apply { color = Color.rgb(76, 175, 80) }canvas.drawRect(680f, h - 180f, width - 20f, h - 40f, activeBtn)canvas.drawText("ЗАНОВО", 700f, h - 95f, textPaint)}}}
