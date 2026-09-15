package com.example.chess

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun MainNavigation() {
    var state by remember { mutableStateOf(GameState.MENU) }
    var rows by remember { mutableStateOf(15) }
    var cols by remember { mutableStateOf(15) }
    var doors by remember { mutableStateOf(3) }

    when (state) {
        GameState.MENU -> MenuScreen(
            initR = rows, initC = cols, initD = doors,
            onStartGame = { r, c, d -> rows = r; cols = c; doors = d; state = GameState.PLAYING }
        )
        GameState.PLAYING -> GameScreen(rows = rows, cols = cols, doorCount = doors, onBack = { state = GameState.MENU })
        GameState.WIN -> WinScreen(onBack = { state = GameState.MENU })
    }
}

@Composable
fun MenuScreen(initR: Int, initC: Int, initD: Int, onStartGame: (Int, Int, Int) -> Unit) {
    var rInput by remember { mutableStateOf(initR.toString()) }
    var cInput by remember { mutableStateOf(initC.toString()) }
    var dInput by remember { mutableStateOf(initD.toString()) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Лабиринт Квестов", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(value = rInput, onValueChange = { rInput = it }, label = { Text("Высота (5-99)") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        OutlinedTextField(value = cInput, onValueChange = { cInput = it }, label = { Text("Ширина (5-99)") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        OutlinedTextField(value = dInput, onValueChange = { dInput = it }, label = { Text("Кол-во дверей (0-20)") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                val r = rInput.toIntOrNull()?.coerceIn(5, 99) ?: 15
                val c = cInput.toIntOrNull()?.coerceIn(5, 99) ?: 15
                val d = dInput.toIntOrNull()?.coerceIn(0, 20) ?: 3
                onStartGame(r, c, d)
            },
            modifier = Modifier.fillMaxWidth().height(55.dp)
        ) { Text("Сгенерировать лабиринт", fontSize = 16.sp) }
    }
}

@Composable
fun GameScreen(rows: Int, cols: Int, doorCount: Int, onBack: () -> Unit) {
    val maze by remember { mutableStateOf(generateMazeWithQuest(rows, cols, doorCount)) }
    var player by remember { mutableStateOf(Point(0, 0)) }
    val inventory = remember { mutableStateListOf<KeyColor>() }
    val openedDoors = remember { mutableStateListOf<KeyColor>() }
    var triggerWin by remember { mutableStateOf(false) }

    if (triggerWin) { LaunchedEffect(Unit) { onBack() } }

    fun movePlayer(dr: Int, dc: Int) {
        val nr = player.r + dr
        val nc = player.c + dc
        if (nr in 0 until rows && nc in 0 until cols) {
            val currentCell = maze[player.r][player.c]
            val targetCell = maze[nr][nc]
            var canMove = false
            
            if (dr == -1 && !currentCell.hasTopWall) canMove = true
            if (dr == 1 && !targetCell.hasTopWall) canMove = true
            if (dc == -1 && !currentCell.hasLeftWall) canMove = true
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
                val key = targetCell.keyColor
                if (key != null && !inventory.contains(key)) {
                    inventory.add(key)
                    targetCell.keyColor = null
                }
                if (targetCell.isFinish) { triggerWin = true }
            }
        }
    }

    var dragX = 0f
    var dragY = 0f

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = {
                    if (abs(dragX) > abs(dragY)) {
                        if (dragX > 50) movePlayer(0, 1) else if (dragX < -50) movePlayer(0, -1)
                    } else {
                        if (dragY > 50) movePlayer(1, 0) else if (dragY < -50) movePlayer(-1, 0)
                    }
                    dragX = 0f; dragY = 0f
                },
                onDrag = { change, amt -> change.consume(); dragX += amt.x; dragY += amt.y }
            )
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack) { Text("В меню") }
            Text("Ключи: ${inventory.size}", color = Color.White, fontSize = 18.sp)
        }
        
        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = minOf(size.width / cols, size.height / rows)
                val offsetX = (size.width - cellSize * cols) / 2
                val offsetY = (size.height - cellSize * rows) / 2
                
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val cell = maze[r][c]
                        val x = offsetX + c * cellSize
                        val y = offsetY + r * cellSize
                        
                        if (cell.isStart) drawRect(Color(0x4400FF00), Offset(x, y), Size(cellSize, cellSize))
                        if (cell.isFinish) drawRect(Color(0x44FF0000), Offset(x, y), Size(cellSize, cellSize))
                        if (cell.keyColor != null) drawCircle(cell.keyColor!!.cellColor, cellSize / 5f, Offset(x + cellSize / 2f, y + cellSize / 2f))
                        if (cell.doorColor != null && !openedDoors.contains(cell.doorColor!!)) {
                            drawRect(cell.doorColor!!.cellColor, Offset(x + cellSize * 0.1f, y + cellSize * 0.1f), Size(cellSize * 0.8f, cellSize * 0.8f))
                        }
                        
                        if (cell.hasTopWall) drawLine(Color.White, Offset(x, y), Offset(x + cellSize, y), strokeWidth = 2.dp.toPx())
                        if (cell.hasLeftWall) drawLine(Color.White, Offset(x, y), Offset(x, y + cellSize), strokeWidth = 2.dp.toPx())
                        if (c == cols - 1) drawLine(Color.White, Offset(x + cellSize, y), Offset(x + cellSize, y + cellSize), strokeWidth = 2.dp.toPx())
                        if (r == rows - 1) drawLine(Color.White, Offset(x, y + cellSize), Offset(x + cellSize, y + cellSize), strokeWidth = 2.dp.toPx())
                    }
                }
                val px = offsetX + player.c * cellSize + cellSize / 2f
                val py = offsetY + player.r * cellSize + cellSize / 2f
                drawCircle(Color(0xFF29B6F6), cellSize / 3.5f, Offset(px, py))
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { movePlayer(-1, 0) }) { Text("▲") }
            Row {
                Button(onClick = { movePlayer(0, -1) }) { Text("◀") }
                Spacer(modifier = Modifier.width(40.dp))
                Button(onClick = { movePlayer(0, 1) }) { Text("▶") }
            }
            Button(onClick = { movePlayer(1, 0) }) { Text("▼") }
        }
    }
}

@Composable
fun WinScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("ВЫ ПРОШЛИ ЛАБИРИНТ! 🏆", color = Color(0xFFFFD700), fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) { Text("В главное меню") }
    }
}
