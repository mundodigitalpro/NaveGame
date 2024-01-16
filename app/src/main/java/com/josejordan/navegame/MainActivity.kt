package com.josejordan.navegame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

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
    private val playerSize = 150f
    private val enemies = mutableListOf<RectF>()
    private val enemySize = 50f
    private val enemySpeed = 10f

    init {
        paint.color = Color.BLUE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dibujar al jugador
        canvas.drawRect(playerX, playerY, playerX + playerSize, playerY + playerSize, paint)

        // Dibujar enemigos
        paint.color = Color.RED
        for (enemy in enemies) {
            canvas.drawOval(enemy, paint)
            enemy.offset(0f, enemySpeed)

            // Eliminar enemigos que salen de la pantalla
            if (enemy.top > height) {
                enemies.remove(enemy)
            }
        }

        // Agregar un nuevo enemigo ocasionalmente
        if (Random.nextInt(100) < 5) {
            val x = Random.nextFloat() * (width - enemySize)
            enemies.add(RectF(x, -enemySize, x + enemySize, 0f))
        }

        // Verificar colisiones
        checkCollision()

        // Redibujar
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Actualizar la posición del jugador
                playerX = event.x - playerSize / 2
                playerY = event.y - playerSize / 2
            }
        }
        return true
    }

    private fun checkCollision() {
        val playerRect = RectF(playerX, playerY, playerX + playerSize, playerY + playerSize)
        for (enemy in enemies) {
            if (RectF.intersects(playerRect, enemy)) {
                // Colisión detectada, manejar lógica de fin del juego o vida perdida
                resetGame()
                break
            }
        }
    }

    private fun resetGame() {
        // Reiniciar el juego o disminuir la vida del jugador
        playerX = 50f
        playerY = 50f
        enemies.clear()
    }
}

