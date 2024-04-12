package utils

import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.random.Random

var DEBUG_LOG = false

fun printDebug(s: Any) {
    if (DEBUG_LOG) {
        println(s)
    }
}

var FRAME_NUMBER = 0

fun printOF0(s: Any) {
    if (FRAME_NUMBER == 0) {
        println(s)
    }
}

fun printOF1(s: Any) {
    if (FRAME_NUMBER == 1) {
        println(s)
    }
}

fun Number.format(i: Int): String {
    val t = this
    return js("(+t).toFixed(i)")
}

fun createRandomPoint(): Point {
    return Point(Random.nextDouble(), Random.nextDouble())
}

fun createRandomFacingPoint(): Point {
    return Point.fromAngle(Random.nextDouble() * PI * 2)
}
fun pointListToFloatArr(ps: List<Point>): FloatArray {
    val floatArr = FloatArray(ps.size * 2)
    ps.forEachIndexed { index, point ->
        floatArr[index * 2] = point.x.f
        floatArr[index * 2 + 1] = point.y.f
    }
    return floatArr
}

fun max(a: Number, b: Number): Double {
    return a.toDouble().coerceAtLeast(b.toDouble())
}
fun min(a: Number, b: Number): Double {
    return a.toDouble().coerceAtMost(b.toDouble())
}
fun min(a: Int, b: Int): Int {
    return a.coerceAtMost(b)
}

fun fromColor(red: Int, green: Int, blue: Int, alpha: Int = 0xff): Int {
    // ooo lah lah
    return js("(red | green << 8 | blue << 16 | alpha << 24) >>> 0")
}

val Boolean.i: Int
    get() { return if (this) 1 else 0 }

val Number.d: Double
    get() { return this.toDouble() }

val Number.i: Int
    get() { return this.toInt() }

val Number.u: UInt
    get() { return this.i.toUInt() }

val Number.f: Float
    get() { return this.toFloat() }

fun Number.pow(i: Int): Int {
    return this.d.pow(i).i
}

fun mod(x: Number, y: Number): Double {
    // Taken from the OpenGl definition
    return x.d - y.d * floor(x/y)
}

fun sqrt(n: Number): Double {
    return kotlin.math.sqrt(n.d)
}

fun abs(n: Int): Int {
    return kotlin.math.abs(n)
}

fun sphereAround(xx: Number, yy: Number, zz: Number, radius: Int) = sequence {
    for (x in -radius..radius) {
        for (y in -radius..radius) {
            for (z in -radius..radius) {
                if (x * x + y * y + z * z < radius * radius) {
                    yield(Triple(xx + x, yy + y, zz + z))
                }
            }
        }
    }
}

fun abs(n: Number): Double {
    return kotlin.math.abs(n.d)
}

fun sign(n: Number): Double {
    return kotlin.math.sign(n.d)
}

fun sin(a: Number): Double {
    return kotlin.math.sin(a.d)
}

fun cos(a: Number): Double {
    return kotlin.math.cos(a.d)
}

fun clamp(t: Number, a: Number, b: Number): Double {
    return min(max(t, a), b)
}

fun hash(x: Int): Int {
    var n = x
    n = (n shl 13) xor n
    n = n * (n * n * 15731 + 789221) + 1376312589
    return n.absoluteValue shr 16
}

fun floor(t: Number): Int {
    return kotlin.math.floor(t.d).i
}

fun ceil(t: Number): Int {
    return kotlin.math.ceil(t.d).i
}

fun fract(x: Number): Number {
    return x - floor(x)
}

operator fun Number.compareTo(i: Number): Int {
    return this.d.compareTo(i.d)
}

operator fun Number.plus(i: Number): Number {
    return this.d + i.d
}
operator fun Number.minus(i: Number): Number {
    return this.d - i.d
}
operator fun Number.div(i: Number): Number {
    return this.d / i.d
}
operator fun Number.times(i: Number): Number {
    return this.d * i.d
}
operator fun Number.unaryMinus(): Number {
    return -this.d
}

fun <T, R> T?.notNull(f: (T) -> R){
    if (this != null){
        f(this)
    }
}

// Finds the normal of the line, pointing towards the side specified by "side"
fun lineNormal(p0: Point, p1: Point, side: Point): Point {
    val z = (p1.x-p0.x)*(side.y-p1.y) - (p1.y-p0.y)*(side.x-p1.x)
    val vec = p1 - p0
    vec.len = 1
    if (z > 0) {
        return Point(-vec.y, vec.x)
    } else {
        return Point(vec.y, -vec.x)
    }
}

private val s1 = Point()
private val s2 = Point()
// Copied from https://stackoverflow.com/a/1968345
fun lineIntersection(p0: Point, p1: Point, p2: Point, p3: Point, intersection: Point): Boolean {
    s1.x = p1.x - p0.x
    s1.y = p1.y - p0.y
    s2.x = p3.x - p2.x
    s2.y = p3.y - p2.y

    val s = (-s1.y * (p0.x - p2.x) + s1.x * (p0.y - p2.y)) / (-s2.x * s1.y + s1.x * s2.y)
    val t = ( s2.x * (p0.y - p2.y) - s2.y * (p0.x - p2.x)) / (-s2.x * s1.y + s1.x * s2.y)

    if (s >= 0 && s <= 1 && t >= 0 && t <= 1)
    {
        // Collision detected
        intersection.x = p0.x + (t * s1.x)
        intersection.y = p0.y + (t * s1.y)
        return true
    }

    return false
}