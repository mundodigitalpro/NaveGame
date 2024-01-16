package com.josejordan.navegame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gameView = GameView(this)
        setContentView(gameView)
    }
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.insetsController?.let {
                //it.hide(WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE) // Deprecated
                it.hide(WindowInsetsController.BEHAVIOR_DEFAULT)
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
}

class GameView(context: Context) : View(context) {
    private val paint = Paint()
    private var playerX = 50f // Posición inicial del jugador en X
    private var playerY = 50f // Posición inicial del jugador en Y

    init {
        paint.color = Color.BLUE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dibujar al jugador
        canvas.drawRect(playerX, playerY, playerX + 150f, playerY + 150f, paint)

        // Redibujar
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Actualizar la posición del jugador
                playerX = event.x - 75f // Centrar el rectángulo en la posición del toque
                playerY = event.y - 75f
            }
        }
        return true
    }
}
