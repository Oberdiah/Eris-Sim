package utils

import kotlinx.browser.window


var timerString = "No data yet"

private var frameStart = 0.0
fun timerStart() {
    frameStart = window.performance.now()
}

private val fpsQueue = mutableListOf<Double>()
private var lastFrameTime = 0.0
class TimedInstance(val name: String, val timeMS: Double) {
    override fun toString(): String {
        return "$name: ${timeMS.format(2)}ms"
    }
}
val frameTimes = mutableListOf<TimedInstance>()

fun timerEnd() {
    lastFrameTime = (window.performance.now() - frameStart)
    fpsQueue.add(lastFrameTime)
    if (fpsQueue.size > 100) {
        fpsQueue.removeAt(0)
    }
    val avg = fpsQueue.average()

    frameTimes.sortByDescending { it.timeMS }

    val line1 = "Last frame: ${lastFrameTime.format(3)} avg: ${avg.format(3)} (${(avg/(10/60.0)).format(1)}%)"
    var line2 = ""
    for (t in frameTimes) {
        if (t.timeMS > 0.2) {
            line2 += "$t\n"
        }
    }

    timerString = "$line1\n$line2"

    frameTimes.clear()
}

fun time(name: String = "Extra", callback: () -> Unit) {
    val start = window.performance.now()
    callback()
    val end = window.performance.now()
    frameTimes.add(TimedInstance(name, (end - start)))
}