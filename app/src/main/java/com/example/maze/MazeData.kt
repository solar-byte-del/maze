package com.example.maze

import android.graphics.Color

// Базовые типы данных, которые свяжут генератор и холст игры лабиринта воедино
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
