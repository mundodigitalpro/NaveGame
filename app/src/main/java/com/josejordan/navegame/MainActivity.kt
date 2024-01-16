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
    private var playerY = 0f // Inicializar playerY a 0
    private val playerSize = 150f
    private val enemies = mutableListOf<RectF>()
    private val enemySize = 50f
    private val enemySpeed = 10f
    private var movingDirection = 0 // 0 para quieto, -1 para izquierda, 1 para derecha
    private val playerSpeed = 20f // Velocidad de movimiento del jugador
    private var screenHeight = 0 // Variable para almacenar la altura de la pantalla

    private var score = 0
    private var lives = 3

    private val textPaint = Paint()

    private var lastUpdateTime = System.currentTimeMillis()

    private var isGameOver = false

    init {
        paint.color = Color.BLUE
        // Configuración inicial del Paint para texto
        textPaint.color = Color.WHITE
        textPaint.textSize = 60f
        textPaint.textAlign = Paint.Align.LEFT
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

        if (isGameOver) {
            // Mostrar mensaje GAME OVER
            textPaint.textAlign = Paint.Align.CENTER  // Asegurarse de que el texto esté centrado
            canvas.drawText("GAME OVER", width / 2f, height / 2f, textPaint)

            // Opcional: Detener el juego por unos segundos antes de reiniciar
            postDelayed({ resetGame() }, 3000) // 3000ms = 3 segundos
            return
        }

        // Actualizar y dibujar juego
        updateGame()

        // Dibujar la puntuación
        canvas.drawText("Score: $score", 20f, 60f, textPaint)

        // Dibujar las vidas
        canvas.drawText("Lives: $lives", width - 300f, 60f, textPaint)

        // Actualizar la posición del jugador basado en la dirección
        playerX += movingDirection * playerSpeed
        // Asegurarse de que el jugador no salga de la pantalla
        playerX = max(0f, min(playerX, width - playerSize))


        // Dibujar al jugador
        canvas.drawRect(playerX, playerY, playerX + playerSize, playerY + playerSize, paint)

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

    private fun updateGame() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime > 1000) { // Incrementar el score cada segundo
            score++
            lastUpdateTime = currentTime
        }
    }

    private fun checkCollision() {
        val playerRect = RectF(playerX, playerY, playerX + playerSize, playerY + playerSize)
        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            if (RectF.intersects(playerRect, enemy)) {
                lives--
                iterator.remove() // Eliminar usando el iterator
                if (lives <= 0) {
                    isGameOver = true
                    postDelayed({ resetGame() }, 3000) // Retrasar la llamada a resetGame()
                    return // Importante para detener la ejecución adicional en este punto
                }
            }
        }
    }

    private fun resetGame() {

        // Reiniciar el juego
        score = 0
        lives = 3

        // Reiniciar la posición del jugador a la parte inferior de la pantalla
        playerX = 50f // O la posición X deseada
        playerY = screenHeight - playerSize

        // Limpiar la lista de enemigos
        enemies.clear()

        isGameOver = false

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 60f

        invalidate() // Para forzar un redibujado y volver al bucle de juego normal
    }
}
