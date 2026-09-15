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
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.util.Stack
import kotlin.math.abs
import kotlin.random.Random

// --- МОДЕЛЬ ДАННЫХ И ДВИЖОК ЛАБИРИНТА ---
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

// --- ГЛАВНАЯ АКТИВНОСТЬ ---
class MainActivity : android.app.Activity() {
    private lateinit var mainLayout: LinearLayout
    private var rows = 15
    private var cols = 15
    private var doorCount = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(18, 18, 18))
        }
        setContentView(mainLayout)
        showMenu()
    }

    private fun showMenu() {
        mainLayout.removeAllViews()
        val context = this

        val title = TextView(context).apply {
            text = "Лабиринт Квестов"
            textColor = Color.WHITE
            textSize = 24f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 50, 0, 50)
        }
        mainLayout.addView(title)

        val inputRows = EditText(context).apply { hint = "Высота (5-99)"; setTextColor(Color.WHITE); setText("15") }
        val inputCols = EditText(context).apply { hint = "Ширина (5-99)"; setTextColor(Color.WHITE); setText("15") }
        val inputDoors = EditText(context).apply { hint = "Дверей (0-5)"; setTextColor(Color.WHITE); setText("3") }

        mainLayout.addView(inputRows)
        mainLayout.addView(inputCols)
        mainLayout.addView(inputDoors)

        val startBtn = Button(context).apply {
            text = "Сгенерировать лабиринт"
            setOnClickListener {
                rows = inputRows.text.toString().toIntOrNull()?.coerceIn(5, 99) ?: 15
                cols = inputCols.text.toString().toIntOrNull()?.coerceIn(5, 99) ?: 15
                doorCount = inputDoors.text.toString().toIntOrNull()?.coerceIn(0, 5) ?: 3
                startGame()
            }
        }
        mainLayout.addView(startBtn)
    }

    private fun startGame() {
        mainLayout.removeAllViews()
        val grid = generateMaze(rows, cols, doorCount)
        val gameView = MazeGameView(this, grid, rows, cols) {
            showWinScreen()
        }
        mainLayout.addView(gameView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        
        // Кнопка возврата в меню
        val backBtn = Button(this).apply {
            text = "В меню"
            setOnClickListener { showMenu() }
        }
        mainLayout.addView(backBtn)
    }

    private fun showWinScreen() {
        mainLayout.removeAllViews()
        val winText = TextView(this).apply {
            text = "ВЫ ПРОШЛИ ЛАБИРИНТ! 🏆"
            textColor = Color.YELLOW
            textSize = 26f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 100, 0, 100)
        }
        mainLayout.addView(winText)
        val menuBtn = Button(this).apply {
            text = "В главное меню"
            setOnClickListener { showMenu() }
        }
        mainLayout.addView(menuBtn)
    }

    // --- АЛГОРИТМ DFS ГЕНЕРАЦИИ ---
    private fun generateMaze(rows: Int, cols: Int, doorCount: Int): Array<Array<MazeCell>> {
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

        // Умная расстановка квеста ключей/дверей под любой размер поля
        val actualDoors = minOf(doorCount, KeyColor.values().size)
        for (i in 0 until actualDoors) {
            val color = KeyColor.values()[i]

            // Ставим дверь на случайной строке, но строго на финишной вертикали справа
            val doorRow = Random.nextInt(rows / 2, rows)
            grid[doorRow][cols - 1].doorColor = color

            // Прячем ключ в безопасной зоне ближе к старту (левая верхняя четверть лабиринта)
            var keyPlaced = false
            while (!keyPlaced) {
                val kr = Random.nextInt(0, rows / 2)
                val kc = Random.nextInt(0, cols / 2)
                val cell = grid[kr][kc]
                if (!cell.isStart && cell.keyColor == null && cell.doorColor == null) {
                    cell.keyColor = color
                    keyPlaced = true
                }
            }
        }

}

// --- КЛАСС ГРАФИКИ CANVAS И СВАЙПОВ ---
class MazeGameView(
    context: Context, 
    private val maze: Array<Array<MazeCell>>, 
    private val rows: Int, 
    private val cols: Int,
    private val onWin: () -> Unit
) : View(context) {

    private var player = Point(0, 0)
    private val inventory = mutableListOf<KeyColor>()
    private val openedDoors = mutableListOf<KeyColor>()
    
    private val wallPaint = Paint().apply { color = Color.WHITE; strokeWidth = 5f }
    private val startPaint = Paint().apply { color = Color.argb(60, 0, 255, 0) }
    private val finishPaint = Paint().apply { color = Color.argb(60, 255, 0, 0) }
    private val playerPaint = Paint().apply { color = Color.rgb(41, 182, 246) }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val diffX = e2.x - (e1?.x ?: 0f)
            val diffY = e2.y - (e1?.y ?: 0f)
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > 100) { if (diffX > 0) movePlayer(0, 1) else movePlayer(0, -1) }
            } else {
                if (abs(diffY) > 100) { if (diffY > 0) movePlayer(1, 0) else movePlayer(-1, 0) }
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
                invalidate() // Перерисовать холст Canvas
                if (targetCell.isFinish) onWin()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cellSize = minOf(width / cols.toFloat(), height / rows.toFloat())
        val offsetX = (width - cellSize * cols) / 2
        val offsetY = (height - cellSize * rows) / 2

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = maze[r][c]
                val x = offsetX + c * cellSize
                val y = offsetY + r * cellSize

                if (cell.isStart) canvas.drawRect(x, y, x + cellSize, y + cellSize, startPaint)
                if (cell.isFinish) canvas.drawRect(x, y, x + cellSize, y + cellSize, finishPaint)
                
                cell.keyColor?.let {
                    val p = Paint().apply { color = it.colorInt }
                    canvas.drawCircle(x + cellSize/2, y + cellSize/2, cellSize/4, p)
                }
                
                cell.doorColor?.let {
                    if (!openedDoors.contains(it)) {
                        val p = Paint().apply { color = it.colorInt }
                        canvas.drawRect(x + cellSize*0.1f, y + cellSize*0.1f, x + cellSize*0.9f, y + cellSize*0.9f, p)
}}if (cell.hasTopWall) canvas.drawLine(x, y, x + cellSize, y, wallPaint)if (cell.hasLeftWall) canvas.drawLine(x, y, x, y + cellSize, wallPaint)}}val px = offsetX + player.c * cellSize + cellSize / 2val py = offsetY + player.r * cellSize + cellSize / 2canvas.drawCircle(px, py, cellSize / 3, playerPaint)}}
