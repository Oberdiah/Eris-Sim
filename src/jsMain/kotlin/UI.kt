import games.perses.kudens.game.Game
import games.perses.kudens.text.Texts
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.Path2D
import utils.*
import kotlin.js.Date

const val MARGIN = 30.0
const val SLIDER_WIDTH = 100
var requestedSimSpeed = 1

fun renderUI() {
    val lhs = FRAME_SIZE + MARGIN

    Texts.drawText(
        lhs.f,
        Game.view.height - 100f,
        "Eris",
        font = "bold 62pt Trebuchet MS",
        fillStyle = "rgba(20, 20, 20, 1.0)"
    )

    val currentHeight = Game.view.height - 200f
    val ui = UI(currentHeight.d)
    ui.renderText("Number of People: ${currentSnapshotToRender.entities.size}")
    ui.renderText("Number of Trees: ${currentSnapshotToRender.numTrees}")
    ui.renderText("Number of Bushes: ${currentSnapshotToRender.numBushes}")
    ui.renderPlusMinus(
        { "Simulation Speed: ${requestedSimSpeed}x" },
        {
            requestedSimSpeed = min(32768, requestedSimSpeed * 2)
        },
        {
            requestedSimSpeed = max(1, requestedSimSpeed / 2).i
        }
    )

    val topOfWorldSlider = ui.currentHeight - MARGIN
    val bottomOfWorldSlider = 50.0
    val worldPositionSliderBackground =
        Rect(Point(lhs, bottomOfWorldSlider), Size(SLIDER_WIDTH, topOfWorldSlider - bottomOfWorldSlider))

    val latestSnapshotPosition =
        (latestSnapshotTick / TOTAL_GAME_TICKS.toDouble()) * (topOfWorldSlider - bottomOfWorldSlider)

    val latestSnapshotSliderBackground = Rect(
        Point(lhs, topOfWorldSlider - latestSnapshotPosition),
        Size(SLIDER_WIDTH, latestSnapshotPosition)
    )

    val currentTick = currentSnapshotToRender.tick
    val sliderPosition = (currentTick / TOTAL_GAME_TICKS.toDouble()) * (topOfWorldSlider - bottomOfWorldSlider)
    val slider = Rect(
        Point(lhs, topOfWorldSlider - sliderPosition - 5),
        Size(SLIDER_WIDTH, 10.0)
    )

    drawRoundRect(worldPositionSliderBackground, "rgba(240, 240, 240, 1.0)")
    drawRoundRect(latestSnapshotSliderBackground, "rgba(35, 205, 180, 1.0)")

    drawRect(slider, "rgba(60, 60, 60, 1.0)")

    drawTimeOnSlider(currentTick, topOfWorldSlider - sliderPosition)


    val sliderDragArea =
        Rect(Point(lhs, bottomOfWorldSlider), Size(SLIDER_WIDTH, topOfWorldSlider - bottomOfWorldSlider + 100.0))
    if (sliderDragArea.contains(mousePosition)) {
        val mouseY = clamp(mousePosition.y, topOfWorldSlider - latestSnapshotPosition, topOfWorldSlider)

        val sliderOverlay = Rect(
            Point(lhs, mouseY - 5),
            Size(SLIDER_WIDTH, 10.0)
        )
        drawRoundRect(sliderOverlay, "rgba(20, 20, 20, 0.2)")
        if (isMouseDown) {
            val newTick =
                (((topOfWorldSlider - mouseY) / (topOfWorldSlider - bottomOfWorldSlider)) * TOTAL_GAME_TICKS).toInt()
            currentSnapshotToRender = (snapshotSnapshots[newTick / SNAP_SNAP_FREQ] ?: currentSnapshotToRender).clone()
            isPaused = true
        }
    }

    val pauseButtonPos = Point(lhs + 300, Game.view.height - 100)
    val pauseButton = Rect(pauseButtonPos, Size(70.0, 70.0))
    drawButton(pauseButton)
    if (mouseLeftClickedThisFrame && pauseButton.contains(mousePosition)) {
        if (isPaused) {
            // Nuke all snapshots after our current snapshot to render position.
            val currentSnapshotIndex = currentSnapshotToRender.tick / SNAP_SNAP_FREQ
            latestSnapshotTick = currentSnapshotIndex * SNAP_SNAP_FREQ
            for (i in (currentSnapshotIndex + 1) until snapshotSnapshots.size) {
                snapshotSnapshots[i] = null
            }
        }

        isPaused = !isPaused
    }
    if (!isPaused) {
        // Draw pause lines
        drawRect(pauseButtonPos.x + 30 - 15, pauseButtonPos.y + 30.0 - 15, 10.0, 40.0, "rgba(60, 60, 60, 1.0)")
        drawRect(pauseButtonPos.x + 60 - 15, pauseButtonPos.y + 30.0 - 15, 10.0, 40.0, "rgba(60, 60, 60, 1.0)")
    } else {
        // Draw play button
        val width = 40.0
        val height = 40.0
        val y = pauseButtonPos.y + 30.0 - 15
        val yy = Game.view.height - y - height
        Game.htmlCanvas().fillStyle = "rgba(60, 60, 60, 1.0)"
        Game.htmlCanvas().fill(
            Path2D().apply {
                moveTo(pauseButtonPos.x + 30 - 15, yy)
                lineTo(pauseButtonPos.x + 30 - 15, yy + height)
                lineTo(pauseButtonPos.x + 30 - 15 + width, yy + height / 2)
                closePath()
            }
        )
    }

//    drawTimeOnSlider(0, topOfWorldSlider)
//    drawTimeOnSlider(TOTAL_GAME_TICKS, bottomOfWorldSlider)
}

fun drawTimeOnSlider(tick: Int, y: Double) {
    val lhs = FRAME_SIZE + MARGIN + SLIDER_WIDTH + MARGIN

    val hourOfDay = tickToFractOfDay(tick) * 24
    val minuteOfDay = (hourOfDay - hourOfDay.toInt()) * 60
    // Formatted as (HH:MM), padding with 0s
    val time = "${hourOfDay.toInt().toString().padStart(2, '0')}:${minuteOfDay.toInt().toString().padStart(2, '0')}"

    Texts.drawText(
        lhs.f,
        y.f - 10f,

        "Year ${tickToYear(tick)}, Day ${tickToDay(tick)} ($time)",
        font = "20pt Trebuchet MS",
        fillStyle = "rgba(20, 20, 20, 1.0)"
    )
}

class UI(var currentHeight: Double) {
    fun renderPlusMinus(
        text: () -> String,
        add: () -> Unit,
        minus: () -> Unit
    ) {
        val lhs = FRAME_SIZE + MARGIN

        Texts.drawText(
            lhs.f,
            currentHeight.f,
            text(),
            font = "bold 20pt Trebuchet MS",
            fillStyle = "rgba(20, 20, 20, 1.0)"
        )

        var buttonLhs = lhs + 350

        val plusFrame = Rect(
            Point(buttonLhs - 5, currentHeight - 5), Size(30.0, 30.0)
        )

        // Draw plus button frame
        drawButton(plusFrame)

        // Draw plus
        drawRect(buttonLhs, currentHeight + 8, 20.0, 4.0, "rgba(60, 60, 60, 1.0)")
        drawRect(buttonLhs + 8, currentHeight, 4.0, 20.0, "rgba(60, 60, 60, 1.0)")

        buttonLhs += 40
        // Draw minus button frame
        val minusFrame = Rect(Point(buttonLhs - 5, currentHeight - 5), Size(30.0, 30.0))
        drawButton(minusFrame)

        // Draw minus
        drawRect(buttonLhs, currentHeight + 8, 20.0, 4.0, "rgba(60, 60, 60, 1.0)")

        if (mouseLeftClickedThisFrame) {
            if (plusFrame.contains(mousePosition)) {
                add()
            } else if (minusFrame.contains(mousePosition)) {
                minus()
            }
        }

        currentHeight -= MARGIN
    }

    fun renderText(text: String) {
        val lhs = FRAME_SIZE + MARGIN

        Texts.drawText(
            lhs.f,
            currentHeight.f,
            text,
            font = "20pt Trebuchet MS",
            fillStyle = "rgba(20, 20, 20, 1.0)"
        )

        currentHeight -= MARGIN
    }
}

fun drawButton(frame: Rect) {
    if (frame.contains(mousePosition)) {
        drawRoundRect(frame, "rgba(140, 140, 140, 1.0)")
    } else {
        drawRoundRect(frame, "rgba(240, 240, 240, 1.0)")
    }
}

fun drawRect(rect: Rect, style: String) {
    drawRect(rect.p.x, rect.p.y, rect.s.w.d, rect.s.h.d, style)
}

fun drawRoundRect(rect: Rect, style: String) {
    roundRect(rect.p.x, rect.p.y, rect.s.w.d, rect.s.h.d, style)
}

fun drawRect(x: Double, y: Double, width: Double, height: Double, style: String) {
    val yy = Game.view.height - y - height
    Game.htmlCanvas().fillStyle = style
    Game.htmlCanvas().fillRect(x, yy, width, height)
}

fun roundRect(x: Double, yy: Double, width: Double, height: Double, style: String) {
    val y = Game.view.height - yy - height
    Game.htmlCanvas().fillStyle = style

    var radius = 8.0
    if (width < 2 * radius) radius = width / 2
    if (height < 2 * radius) radius = height / 2

    val ctx = Game.htmlCanvas()
    ctx.beginPath()
    ctx.moveTo(x + radius, y)
    ctx.lineTo(x + width - radius, y)
    ctx.quadraticCurveTo(x + width, y, x + width, y + radius)
    ctx.lineTo(x + width, y + height - radius)
    ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height)
    ctx.lineTo(x + radius, y + height)
    ctx.quadraticCurveTo(x, y + height, x, y + height - radius)
    ctx.lineTo(x, y + radius)
    ctx.quadraticCurveTo(x, y, x + radius, y)
    ctx.closePath()
    ctx.fill()
}