package com.example.maze

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout

class MainActivity : android.app.Activity() {
    private lateinit var mainLayout: LinearLayout
    private var rows = 11
    private var cols = 11
    private var doorCount = 2
    private var isWon = false
    private lateinit var gameView: MazeGameView

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            super.onCreate(savedInstanceState)
            
            mainLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(android.graphics.Color.rgb(18, 18, 18))
            }
            setContentView(mainLayout)

            mainLayout.post {
                try {
                    startNewGame()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            super.onCreate(savedInstanceState)
            setContentView(View(this))
        }
    }

    fun startNewGame() {
        mainLayout.removeAllViews()
        isWon = false
        val grid = MazeEngine.generateMaze(rows, cols, doorCount)
        
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
}
