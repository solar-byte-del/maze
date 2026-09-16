package com.example.maze

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Stack
import kotlin.math.abs
import kotlin.random.Random

// --- ГЛАВНАЯ АКТИВНОСТЬ (ТЕПЕРЬ НА БАЗЕ COMPONENT_ACTIVITY) ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(18, 18, 18)),
                contentAlignment = Alignment.Center
            ) {
                MazeGameScreen()
            }
        }
    }
}

// --- ГЛАВНЫЙ ЭКРАН ИГРЫ ---
@Composable
fun MazeGameScreen() {
    var rows by remember { mutableStateOf(11) }
    var cols by remember { mutableStateOf(11) }
    var doorCount by remember { mutableStateOf(2) }
    
    // Ключ для принудительного перезапуска генерации лабиринта
    var gameSessionKey by remember { mutableStateOf(0) }

    val mazeGrid = remember(rows, cols, doorCount, gameSessionKey) {
        generateMazeData(rows, cols, doorCount)
    }

    var playerRow by remember(mazeGrid) { mutableStateOf(0) }
    var playerCol by remember(mazeGrid) { mutableStateOf(0) }
    val inventory = remember(mazeGrid) { mutableStateListOf<KeyColor>() }
    val openedDoors = remember(mazeGrid) { mutableStateListOf<KeyColor>() }
    var isWon by remember(mazeGrid) { mutableStateOf(false) }

    // Направление свайпа
    var dragAmountX by remember { mutableStateOf(0f) }
    var dragAmountY by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(mazeGrid, isWon) {
                if (isWon) return@pointerInput
                detectDragGestures(
                    onDragStart = {
                        dragAmountX = 0f
                        dragAmountY = 0f
                    },
                    onDragEnd = {
                        val minSwipeDistance = 50f
                        if (abs(dragAmountX) > abs(dragAmountY)) {
                            if (abs(dragAmountX) > minSwipeDistance) {
                                if (dragAmountX > 0) {
                                    // Вправо
                                    movePlayer(0, 1, mazeGrid, rows, cols, playerRow, playerCol, inventory, openedDoors) { r, c, win ->
                                        playerRow = r; playerCol = c; isWon = win
                                    }
                                } else {
                                    // Влево
                                    movePlayer(0, -1, mazeGrid, rows, cols, playerRow, playerCol, inventory, openedDoors) { r, c, win ->
                                        playerRow = r; playerCol = c; isWon = win
                                    }
                                }
                            }
                        } else {
                            if (abs(dragAmountY) > minSwipeDistance) {
                                if (dragAmountY > 0) {
                                    // Вниз
                                    movePlayer(1, 0, mazeGrid, rows, cols, playerRow, playerCol, inventory, openedDoors) { r, c, win ->
                                        playerRow = r; playerCol = c; isWon = win
                                    }
                                } else {
                                    // Вверх
                                    movePlayer(-1, 0, mazeGrid, rows, cols, playerRow, playerCol, inventory, openedDoors) { r, c, win ->
                                        playerRow = r; playerCol = c; isWon = win
                                    }
                                }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAmountX += dragAmount.x
                        dragAmountY += dragAmount.y
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Modifier.SpaceBetween
    ) {
        // Верхняя панель: Статус
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Ключей в кармане: ${inventory.size}",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            if (isWon) {
                Text(
                    text = "ПОБЕДА! 🏆",
                    color = Color.Yellow,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Поле Лабиринта
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints {
                val availableWidth = maxWidth
                val availableHeight = maxHeight
                val cellSize = minOf(availableWidth / cols, availableHeight / rows)

                Column(
                    modifier = Modifier
                        .size(cellSize * cols, cellSize * rows)
                        .background(Color(30, 30, 30))
                ) {
                    for (r in 0 until rows) {
                        Row(modifier = Modifier.height(cellSize)) {
                            for (c in 0 until cols) {
                                val cell = mazeGrid[r][c]
                                val isPlayerHere = (r == playerRow && c == playerCol)

                                Box(
                                    modifier = Modifier
                                        .width(cellSize)
                                        .fillMaxHeight()
                                        .drawMazeWalls(cell)
                                ) {
                                    // Отрисовка бэкграунда Старт / Финиш
                                    if (cell.isStart) {
                                        Box(modifier = Modifier.fillMaxSize().background(Color(0, 255, 0, 40)))
                                    }
                                    if (cell.isFinish) {
                                        Box(modifier = Modifier.fillMaxSize().background(Color(255, 0, 0, 40)))
                                    }

                                    // Дверь
                                    if (cell.doorColor != null && !openedDoors.contains(cell.doorColor)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize(0.7f)
                                                .align(Alignment.Center)
                                                .background(Color(cell.doorColor!!.colorInt), RoundedCornerShape(4.dp))
                                        )
                                    }

                                    // Ключ
                                    if (cell.keyColor != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(cellSize * 0.4f)
                                                .align(Alignment.Center)
                                                .clip(CircleShape)
                                                .background(Color(cell.keyColor!!.colorInt))
                                        )
                                    }

                                    // Игрок
                                    if (isPlayerHere) {
                                        Box(
                                            modifier = Modifier
                                                .size(cellSize * 0.6f)
                                                .align(Alignment.Center)
                                                .clip(CircleShape)
                                                .background(Color(41, 182, 246))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Нижняя панель управления: Кнопки масштаба
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val btnColors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White)
            
            Button(onClick = { rows = 5; cols = 5; doorCount = 1 }, colors = btnColors) { Text("5x5") }
Button(onClick = { rows = 15; cols = 15; doorCount = 3 }, colors = btnColors) { Text("15x15") }Button(onClick = { rows = 25; cols = 25; doorCount = 4 }, colors = btnColors) { Text("25x25") }if (isWon) {Button(onClick = { gameSessionKey++ },colors = ButtonDefaults.buttonColors(containerColor = Color(76, 175, 80), contentColor = Color.White)) {Text("ЗАНОВО")}}}}}// --- МОДИФИКАТОР ОТРИСОВКИ СТЕН ---fun Modifier.drawMazeWalls(cell: MazeCell): Modifier = this.drawWithContent {drawContent()val wallThickness = 4fif (cell.hasTopWall) {drawLine(color = Color.White, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = wallThickness)}if (cell.hasLeftWall) {drawLine(color = Color.White, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(0f, size.height), strokeWidth = wallThickness)}// Внешние правая и нижняя границы всего лабиринтаif (cell.c == cell.c) { // Универсально для сеткиdrawLine(color = Color.White, start = androidx.compose.ui.geometry.Offset(size.width, 0f), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = 1f)}if (cell.r == cell.r) {drawLine(color = Color.White, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = 1f)}}fun Modifier.drawWithContent(onDraw: Canvas.() -> Unit): Modifier = this.then(Modifier.pointerInput(Unit) {}).clip(RoundedCornerShape(0.dp)).then(object : androidx.compose.ui.draw.DrawModifier {override fun androidx.compose.ui.graphics.drawscope.ContentDrawScope.draw() {drawContent()val scope = thisval wallThickness = 5f// Извлекаем переданную ячейку через контекст кастомного рисования}})// Альтернативный простой способ отрисовки через стандартный бордюр Composefun Modifier.drawMazeWalls(cell: MazeCell) = this.then(Modifier.background(Color.Transparent)).then(object : androidx.compose.ui.draw.DrawModifier {override fun androidx.compose.ui.graphics.drawscope.ContentDrawScope.draw() {drawContent()val t = 4fif (cell.hasTopWall) drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f,0f), androidx.compose.ui.geometry.Offset(size.width,0f), strokeWidth = t)if (cell.hasLeftWall) drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f,0f), androidx.compose.ui.geometry.Offset(0f,size.height), strokeWidth = t)}})// --- ЛОГИКА ДВИЖЕНИЯ ИГРОКА ---fun movePlayer(dr: Int, dc: Int,maze: Array<Array>,rows: Int, cols: Int,pRow: Int, pCol: Int,inventory: MutableList,openedDoors: MutableList,onUpdate: (Int, Int, Boolean) -> Unit) {val nr = pRow + drval nc = pCol + dcif (nr in 0 until rows && nc in 0 until cols) {val currCell = maze[pRow][pCol]val targetCell = maze[nr][nc]var canMove = falseif (dr == -1 && !currCell.hasTopWall) canMove = trueif (dr == 1 && !targetCell.hasTopWall) canMove = trueif (dc == -1 && !currCell.hasLeftWall) canMove = trueif (dc == 1 && !targetCell.hasLeftWall) canMove = trueif (canMove) {val door = targetCell.doorColorif (door != null && !openedDoors.contains(door)) {if (inventory.contains(door)) {openedDoors.add(door)inventory.remove(door)} else {return // Дверь заперта, проход закрыт}}// Забираем ключ, если он естьtargetCell.keyColor?.let {inventory.add(it)targetCell.keyColor = null}val win = targetCell.isFinishonUpdate(nr, nc, win)}}}// --- МАТЕМАТИЧЕСКИЙ ДВИЖОК ГЕНЕРАЦИИ КАРТЫ ---fun generateMazeData(rows: Int, cols: Int, doorCount: Int): Array<Array> {val grid = Array(rows) { r -> Array(cols) { c -> MazeCell(r, c) } }val visited = Array(rows) { BooleanArray(cols) }val stack = Stack()grid[0][0].isStart = truevisited[0][0] = truestack.push(Point(0, 0))while (!stack.isEmpty()) {val curr = stack.peek()val neighbors = mutableListOf<Pair<Point, String>>()if (curr.r > 0 && !visited[curr.r - 1][curr.c]) neighbors.add(Pair(Point(curr.r - 1, curr.c), "UP"))if (curr.r < rows - 1 && !visited[curr.r + 1][curr.c]) neighbors.add(Pair(Point(curr.r + 1, curr.c), "DOWN"))if (curr.c > 0 && !visited[curr.r][curr.c - 1]) neighbors.add(Pair(Point(curr.r, curr.c - 1), "LEFT"))if (curr.c < cols - 1 && !visited[curr.r][curr.c + 1]) neighbors.add(Pair(Point(curr.r, curr.c + 1), "RIGHT"))if (neighbors.isNotEmpty()) {val nextMove = neighbors[Random.nextInt(neighbors.size)]val next = nextMove.firstwhen (nextMove.second) {"UP" -> grid[curr.r][curr.c].hasTopWall = false"DOWN" -> grid[next.r][next.c].hasTopWall = false"LEFT" -> grid[curr.r][curr.c].hasLeftWall = false"RIGHT" -> grid[next.r][next.c].hasLeftWall = false}visited[next.r][next.c] = truestack.push(next)} else {stack.pop()}}grid[rows - 1][cols - 1].isFinish = trueval actualDoors = minOf(doorCount, KeyColor.values().size)for (i in 0 until actualDoors) {val color = KeyColor.values()[i]val doorRow = Random.nextInt(rows / 2, rows)grid[doorRow][cols - 1].doorColor = colorvar keyPlaced = falsevar attempts = 0while (!keyPlaced && attempts < 50) {attempts++val kr = Random.nextInt(0, rows / 2)val kc = Random.nextInt(0, cols / 2)val cell = grid[kr][kc]if (!cell.isStart && cell.keyColor == null && cell.doorColor == null) {cell.keyColor = colorkeyPlaced = true}}}return grid}
