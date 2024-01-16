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
import kotlin.math.*

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
    //private var playerY = 50f // Posición inicial del jugador en Y
    private var playerY = 0f // Inicializar playerY a 0
    private val playerSize = 150f
    private val enemies = mutableListOf<RectF>()
    private val enemySize = 50f
    private val enemySpeed = 10f

    private var movingDirection = 0 // 0 para quieto, -1 para izquierda, 1 para derecha
    private val playerSpeed = 20f // Velocidad de movimiento del jugador

    private var screenHeight = 0 // Variable para almacenar la altura de la pantalla


    init {
        paint.color = Color.BLUE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Almacenar la altura de la pantalla
        screenHeight = h

        // Establecer la posición inicial del jugador en la parte inferior de la pantalla
        playerY = screenHeight - playerSize
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Actualizar la posición del jugador basado en la dirección
        playerX += movingDirection * playerSpeed
        // Asegurarse de que el jugador no salga de la pantalla
        playerX = max(0f, min(playerX, width - playerSize))


        // Dibujar al jugador
        canvas.drawRect(playerX, playerY, playerX + playerSize, playerY + playerSize, paint)

        /*// Lista temporal para eliminar enemigos
        val enemiesToRemove = mutableListOf<RectF>()

        // Dibujar enemigos
        paint.color = Color.RED
        for (enemy in enemies) {
            canvas.drawOval(enemy, paint)
            enemy.offset(0f, enemySpeed)

            // Eliminar enemigos que salen de la pantalla
            if (enemy.top > height) {
                //enemies.remove(enemy)
                enemiesToRemove.add(enemy)
            }
        }*/

        // Dibujar enemigos usando un iterator
        val iterator = enemies.iterator()
        paint.color = Color.RED
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            canvas.drawOval(enemy, paint)
            enemy.offset(0f, enemySpeed)

            // Eliminar enemigos que salen de la pantalla
            if (enemy.top > height) {
                iterator.remove() // Eliminar usando el iterator
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

/*    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Actualizar la posición del jugador
                playerX = event.x - playerSize / 2
                playerY = event.y - playerSize / 2
            }
        }
        return true
    }*/

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                movingDirection = when {
                    event.x < playerX + playerSize / 2 -> -1 // Mover a la izquierda
                    event.x > playerX + playerSize / 2 -> 1  // Mover a la derecha
                    else -> 0
                }
            }
            MotionEvent.ACTION_UP -> {
                // Detener el movimiento cuando el usuario levanta el dedo
                movingDirection = 0
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
        // Reiniciar la posición del jugador a la parte inferior de la pantalla
        playerX = 50f // O la posición X deseada
        playerY = screenHeight - playerSize

        // Limpiar la lista de enemigos
        enemies.clear()
    }
}

