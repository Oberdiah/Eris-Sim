import games.perses.kudens.game.DrawMode
import games.perses.kudens.game.Game
import games.perses.kudens.game.Screen
import games.perses.kudens.input.EmptyInputProcessor
import games.perses.kudens.input.Input
import games.perses.kudens.texture.Textures
import kotlinx.browser.document
import utils.Point
import utils.f
import kotlin.js.Date

const val SNAP_SNAP_FREQ = 1000
val snapshotSnapshots = mutableListOf<GameSnapshot?>(GameSnapshot())
var latestSnapshotTick: Int = 0
var currentSnapshotToRender = (snapshotSnapshots.last()!!).clone()

// One tick a second to start with.
var simulationSpeed = 1
var timeSimulated = 0f
var isPaused = false

// Unrelated to game simulation, used to simulate at the correct times.
var totalTime = 0f

var lastUpdateTime = 0.0
var globalRenderFraction = 0.0

class GameScreen : Screen() {
    init {
        val totalNumSnapshots = TOTAL_GAME_TICKS / SNAP_SNAP_FREQ
        for (i in 0 until totalNumSnapshots) {
            snapshotSnapshots.add(null)
        }
    }

    override fun loadResources() {
        loadTextures()
    }

    override fun unloadResources() {
        Textures.dispose()
    }

    override fun update(time: Float, delta: Float) {
        if (isPaused) {
            return
        }

        totalTime += delta * simulationSpeed

        val numIterations = (totalTime - timeSimulated).toInt()
        if (numIterations >= 1) {
            simulationSpeed = requestedSimSpeed
            requestedSimSpeed = simulationSpeed
            for (i in 0 until numIterations) {
                if (currentSnapshotToRender.tick >= TOTAL_GAME_TICKS) {
                    timeSimulated = totalTime
                    isPaused = true
                    break
                }

                currentSnapshotToRender.simulateStep()

                if (currentSnapshotToRender.tick % SNAP_SNAP_FREQ == 0) {
                    val insertLocation = currentSnapshotToRender.tick / SNAP_SNAP_FREQ
                    snapshotSnapshots[insertLocation] = currentSnapshotToRender.clone()
                    latestSnapshotTick = currentSnapshotToRender.tick
                }
                timeSimulated += 1f
            }
            lastUpdateTime = Date().getTime()
        }
    }

    override fun render() {
        if (isPaused) {
            globalRenderFraction = 0.0
        } else {
            globalRenderFraction = ((((Date().getTime() - lastUpdateTime)) / 1000.0) * simulationSpeed).rem(1.0)
        }

        currentSnapshotToRender.render()
        renderUI()

        mouseLeftClickedThisFrame = false
        mouseRightClickedThisFrame = false
    }
}

var mousePosition = Point(0, 0)
var mouseLeftClickedThisFrame = false
var mouseRightClickedThisFrame = false
var isMouseDown = false

fun main(args: Array<String>) {
    // set border color
    document.body?.style?.backgroundColor = "#DADADA"

    Game.pauseOnNoFocus = false

    Game.view.setToHeight(FRAME_SIZE.f)
    Game.view.drawMode = DrawMode.LINEAR

    Game.view.minAspectRatio = 1.4f
    Game.view.maxAspectRatio = 1.4f

    val gray = 218f / 255
    Game.setClearColor(gray, gray, gray, 1.0f)

    Input.setInputProcessor(object : EmptyInputProcessor() {
        override fun mouseMove(x: Float, y: Float) {
            mousePosition = Point(x, y)
        }

        override fun pointerClick(pointer: Int, x: Float, y: Float) {
            if (pointer == 0) {
                mouseLeftClickedThisFrame = true
            } else if (pointer == 2) {
                mouseRightClickedThisFrame = true
            }
        }
    })

    document.body?.onmousedown = {
        isMouseDown = true
        Unit
    }

    document.body?.onmouseup = {
        isMouseDown = false
        Unit
    }

    Game.start(GameScreen())
}
