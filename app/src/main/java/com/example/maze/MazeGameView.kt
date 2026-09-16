package com.example.maze

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

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
    private val playerPaint = Paint().apply { color = Color.rgb(41, 182, 246); isAntiAlias = true }
    private val textPaint = Paint().apply { color = Color.WHITE; textSize = 40f; isAntiAlias = true }
    private val btnPaint = Paint().apply { color = Color.DKGRAY }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (isGameFinished) return false
            val x1 = e1?.x ?: 0f
            val y1 = e1?.y ?: 0f
            val diffX = e2.x - x1
            val diffY = e2.y - y1
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

    private fun drawGrid(canvas: Canvas, offsetX: Float, offsetY: Float, cellSize: Float) {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = maze[r][c]
                val x = offsetX + c * cellSize
                val y = offsetY + r * cellSize

                if (cell.isStart) canvas.drawRect(x, y, x + cellSize, y + cellSize, startPaint)
                if (cell.isFinish) canvas.drawRect(x, y, x + cellSize, y + cellSize, finishPaint)
                
                val kc = cell.keyColor
                if (kc != null) {
                    val p = Paint().apply { color = kc.colorInt; style = Paint.Style.FILL; isAntiAlias = true }
                    canvas.drawCircle(x + cellSize / 2f, y + cellSize / 2f, cellSize / 5f, p)
                }
                
                val dc = cell.doorColor
                if (dc != null && !openedDoors.contains(dc)) {
                    val p = Paint().apply { color = dc.colorInt; style = Paint.Style.FILL }
                    canvas.drawRect(x + cellSize * 0.15f, y + cellSize * 0.15f, x + cellSize * 0.85f, y + cellSize * 0.85f, p)
                }

                if (cell.hasTopWall) canvas.drawLine(x, y, x + cellSize, y, wallPaint)
                if (cell.hasLeftWall) canvas.drawLine(x, y, x, y + cellSize, wallPaint)
            }
        }
        canvas.drawLine(offsetX, offsetY + rows * cellSize, offsetX + cols * cellSize, offsetY + rows * cellSize, wallPaint)
        canvas.drawLine(offsetX + cols * cellSize, offsetY, offsetX + cols * cellSize, offsetY + rows * cellSize, wallPaint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(18, 18, 18))

        val usableHeight = height - 220f
        val cellSize = minOf(width / cols.toFloat(), usableHeight / rows.toFloat())
        val offsetX = (width - cellSize * cols) / 2
        val offsetY = (usableHeight - cellSize * rows) / 2
        val h = height.toFloat()

        drawGrid(canvas, offsetX, offsetY, cellSize)

        val px = offsetX + player.c * cellSize + cellSize / 2f
        val py = offsetY + player.r * cellSize + cellSize / 2f
        canvas.drawCircle(px, py, cellSize / 3.5f, playerPaint)

        canvas.drawText("Ключей в кармане: ${inventory.size}", 40f, h - 240f, textPaint)
        
        if (isGameFinished) {
            val winPaint = Paint().apply { color = Color.YELLOW; textSize = 50f; isAntiAlias = true }
            canvas.drawText("ПОБЕДА! 🏆", width / 2f - 120f, h - 240f, winPaint)
        }

        canvas.drawRect(20f, h - 180f, 220f, h - 40f, btnPaint)
        canvas.drawText("5 x 5", 65f, h - 95f, textPaint)

        canvas.drawRect(240f, h - 180f, 440f, h - 40f, btnPaint)
        canvas.drawText("15x15", 275f, h - 95f, textPaint)

        canvas.drawRect(460f, h - 180f, 660f, h - 40f, btnPaint)
        canvas.drawText("30x30", 495f, h - 95f, textPaint)

        if (isGameFinished) {
            val activeBtn = Paint().apply { color = Color.rgb(76, 175, 80); style = Paint.Style.FILL }
            canvas.drawRect(680f, h - 180f, width - 20f, h - 40f, activeBtn)
            canvas.drawText("ЗАНОВО", 700f, h - 95f, textPaint)
        }
    }
}
