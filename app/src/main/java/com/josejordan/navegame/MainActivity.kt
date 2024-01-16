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
    private var screenWidth = 0
    private var screenHeight = 0
    private lateinit var gameManager: GameManager
    private var movingDirection = 0f // Dirección del movimiento del jugador
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 60f
        textAlign = Paint.Align.LEFT
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w
        screenHeight = h
        gameManager = GameManager(screenWidth, screenHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (gameManager.isGameOver) {
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("GAME OVER", screenWidth / 2f, screenHeight / 2f, textPaint)
            postDelayed({ resetGame() }, 3000)
            return
        }
        // Actualizar la posición del jugador
        if (movingDirection != 0f) {
            gameManager.playerX += movingDirection
            gameManager.playerX = max(
                0f,
                min(gameManager.playerX, screenWidth - 150f)
            ) // Asegurar que el jugador no salga de la pantalla
        }

        // Actualizar y verificar el juego
        gameManager.updateGame()
        gameManager.addEnemy()
        gameManager.checkCollision()

        // Dibujar la puntuación
        canvas.drawText("Score: ${gameManager.score}", 50f, 60f, textPaint)

        // Dibujar las vidas
        canvas.drawText("Lives: ${gameManager.lives}", screenWidth - 300f, 60f, textPaint)

        // Dibujar al jugador
        paint.color = Color.BLUE
        canvas.drawRect(
            gameManager.playerX,
            gameManager.playerY,
            gameManager.playerX + 150f,
            gameManager.playerY + 150f,
            paint
        )

        // Dibujar enemigos
        gameManager.updateEnemies()
        paint.color = Color.RED
        for (enemy in gameManager.enemies) {
            canvas.drawOval(enemy, paint) // Asegúrate de que los enemigos se dibujen correctamente
        }

        // Redibujar
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                movingDirection = if (event.x < screenWidth / 2) -10f else 10f // Actualizar la dirección del movimiento
            }
            MotionEvent.ACTION_UP -> {
                movingDirection = 0f // Detener el movimiento cuando se levanta el dedo
            }
        }
        return true
    }

    private fun resetGame() {
        // Reiniciar el juego usando GameManager
        gameManager = GameManager(screenWidth, screenHeight)
        invalidate()
    }
}

class GameManager(private val screenWidth: Int, private val screenHeight: Int) {
    var score = 0
    var lives = 3
    var isGameOver = false

    private val playerSize = 150f
    private val enemySize = 50f
    private val enemySpeed = 10f
    var playerX = 50f
    var playerY = screenHeight - playerSize
    val enemies = mutableListOf<RectF>()

    private var lastUpdateTime = System.currentTimeMillis()

    fun updateGame() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime > 1000) {
            score++
            lastUpdateTime = currentTime
        }
    }

    fun checkCollision() {
        val playerRect = RectF(playerX, playerY, playerX + playerSize, playerY + playerSize)
        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            if (RectF.intersects(playerRect, enemy)) {
                lives--
                iterator.remove()
                if (lives <= 0) {
                    isGameOver = true
                    return
                }
            }
        }
    }

    // Método para agregar nuevos enemigos

    fun addEnemy() {
        if (Random.nextInt(100) < 5) { // 5% de posibilidad de añadir un nuevo enemigo
            val x = Random.nextFloat() * (screenWidth - enemySize)
            enemies.add(
                RectF(
                    x,
                    -enemySize,
                    x + enemySize,
                    0f
                )
            )
        }
    }

    // Método para actualizar la posición de los enemigos
    fun updateEnemies() {
        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            enemy.offset(0f, enemySpeed) // Mover el enemigo hacia abajo

            if (enemy.top > screenHeight) {
                iterator.remove() // Eliminar enemigos que salen de la pantalla
            }
        }
    }
}
