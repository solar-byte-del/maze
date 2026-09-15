package com.example.chess

import androidx.compose.ui.graphics.Color

enum class GameState { MENU, PLAYING, WIN }

enum class KeyColor(val label: String, val cellColor: Color) {
    RED("Красный", Color(0xFFEF5350)),
    BLUE("Синий", Color(0xFF42A5F5)),
    GREEN("Зеленый", Color(0xFF66BB6A)),
    YELLOW("Желтый", Color(0xFFFFEE58)),
    ORANGE("Оранжевый", Color(0xFFFFA726)),
    PURPLE("Фиолетовый", Color(0xFFAB47BC)),
    CYAN("Голубой", Color(0xFF26C6DA)),
    MAGENTA("Пурпурный", Color(0xFFEC407A)),
    LIME("Лайм", Color(0xFFD4E157)),
    BROWN("Коричневый", Color(0xFF8D6E63)),
    TEAL("Бирюзовый", Color(0xFF26A69A)),
    PINK("Розовый", Color(0xFFF48FB1)),
    INDIGO("Индиго", Color(0xFF5C6BC0)),
    AMBER("Янтарный", Color(0xFFFFCA28)),
    DEEP_ORANGE("Рыжий", Color(0xFFFF7043)),
    LIGHT_BLUE("Светло-синий", Color(0xFF29B6F6)),
    DEEP_PURPLE("Темно-фиолетовый", Color(0xFF7E57C2)),
    LIGHT_GREEN("Салатовый", Color(0xFF9CCC65)),
    GOLD("Золотой", Color(0xFFFFD700)),
    SILVER("Серебряный", Color(0xFFC0C0C0))
}

data class Point(val r: Int, val c: Int)

data class MazeCell(
    val r: Int,
    val c: Int,
    var hasLeftWall: Boolean = true,
    var hasTopWall: Boolean = true,
    var isStart: Boolean = false,
    var isFinish: Boolean = false,
    var keyColor: KeyColor? = null,
    var doorColor: KeyColor? = null
)
