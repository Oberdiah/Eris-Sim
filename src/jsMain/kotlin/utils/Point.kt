package utils

import kotlin.math.atan2

operator fun Iterable<Point>.times(simpleSize: Number): List<Point> {
    return this.map { it * simpleSize }
}

open class Point: Cloneable<Point> {
    open var x: Double = 0.0
    open var y: Double = 0.0

    override fun toString(): String {
        return "(${x.format(2)}, ${y.format(2)})"
    }

    override fun clone(): Point {
        return Point(this)
    }

    val cpy: Point
        get() {return Point(this) }

    var angle: Double
        get() {
            return atan2(x, y)
        }
        set(value) {
            x = 0.0
            y = len.d
            rotate(value)
        }

    var len: Number
        get() {
            return sqrt(x * x + y * y)
        }
        set(value) {
            val le = len.d
            if (le < 0.01) {
                x = le
                y = 0.0
               return
            }
            val scale = value.d / le
            x *= scale
            y *= scale
        }

    companion object {
        fun fromAngle(angle: Number): Point {
            return Point(cos(angle), sin(angle))
        }

        fun splat(n: Number): Point {
            return Point(n, n)
        }
    }

    constructor(p: Point) {
        x = p.x
        y = p.y
    }

    constructor(x: Number, y: Number) {
        this.x = x.d
        this.y = y.d
    }

    constructor(): this(0, 0)

    fun angleTo(p: Point): Double {
        return atan2(p.x - x, p.y - y)
    }

    fun ensureInt(): Point {
        return Point(x.i, y.i)
    }

    fun distTo(p: Point): Double {
        val dx = p.x - x
        val dy = p.y - y
        return sqrt(dx * dx + dy * dy)
    }

    fun rotate(angle: Number) {
        val cos = cos(angle)
        val sin = sin(angle)

        val x1 = -(x * cos - y * sin)
        val y1 = -(x * sin + y * cos)
        x = x1
        y = y1
    }

    fun normCpy(): Point {
        val cpy = this.cpy
        cpy.len = 1
        return cpy
    }

    fun lenCpy(l: Number): Point {
        val cpy = this.cpy
        cpy.len = l
        return cpy
    }

    fun lerp(p: Point, t: Number): Point {
        return Point(x + (p.x - x) * t, y + (p.y - y) * t)
    }

    operator fun times(p: Point): Point {
        return Point(x * p.x, y * p.y)
    }
    open operator fun times(i: Number): Point {
        return Point(x * i, y * i)
    }

    open operator fun plus(p: Point): Point {
        return Point(x + p.x, y + p.y)
    }
    operator fun plus(i: Number): Point {
        return Point(x + i, y + i)
    }

    operator fun minus(p: Point): Point {
        return Point(x - p.x, y - p.y)
    }
    operator fun minus(i: Number): Point {
        return Point(x - i, y - i)
    }

    open operator fun div(p: Point): Point {
        return Point(x / p.x, y / p.y)
    }
    open operator fun div(i: Number): Point {
        return Point(x / i, y / i)
    }

    fun minX(m: Number): Point {
        return Point(min(x, m), y)
    }

    fun maxX(m: Number): Point {
        return Point(max(x, m), y)
    }

    fun minY(m: Number): Point {
        return Point(x, min(y, m))
    }

    fun maxY(m: Number): Point {
        return Point(x, max(y, m))
    }

    fun zero() {
        x = 0.0
        y = 0.0
    }

    fun clampX(a: Number, b: Number) {
        x = clamp(x, a, b)
    }

    fun max(d: Number): Point {
        return Point(max(x, d), max(y, d))
    }

    fun min(d: Number): Point {
        return Point(min(x, d), min(y, d))
    }

    fun clampLen(min: Number, max: Number): Point {
        val copy = Point(this)
        val l = len
        if (l > max) {
            copy.len = max
        } else if (l < min) {
            copy.len = min
        }
        return copy
    }

    fun absMin(d: Number): Point {
        return Point(min(abs(x), d) * sign(x), min(abs(y), d) * sign(y))
    }

    fun clamp(a: Point, b: Point) {
        x = clamp(x, a.x, b.x)
        y = clamp(y, a.y, b.y)
    }

    fun dot(p: Point): Number {
        return x * p.x + y * p.y
    }

    fun setTo(p: Point) {
        x = p.x
        y = p.y
    }
}

typealias Velocity = Point

class Size(w: Number, h: Number): Point(w, h) {
    constructor() : this(0, 0)

    override operator fun plus(p: Point): Size {
        return Size(x + p.x, y + p.y)
    }

    override operator fun div(p: Point): Size {
        return Size(x / p.x, y / p.y)
    }

    override operator fun div(i: Number): Size {
        return Size(x / i, y / i)
    }

    override operator fun times(i: Number): Size {
        return Size(x * i, y * i)
    }

    var w: Number
        get() = x
        set(value) {x = value.d}

    var h: Number
        get() = y
        set(value) {y = value.d}
}

class Rect(var p: Point, var s: Size) {
    fun enlargen(buffer: Number): Rect {
        return Rect(p - Point(buffer, buffer), s + Point(buffer, buffer) * 2)
    }

    fun contains(p1: Point): Boolean {
        val a = (p1.x >= p.x)
        val b = (p1.y >= p.y)
        val c = (p1.x <= p.x + s.w)
        val d = (p1.y <= p.y + s.h)
        return a && b && c && d
    }

    fun touches(rect: Rect): Boolean {
        return  p.x < rect.p.x + rect.s.w &&
                p.y < rect.p.y + rect.s.h &&
                p.x + s.w > rect.p.x &&
                p.y + s.h > rect.p.y

    }

    constructor(): this(Point(), Size())
}