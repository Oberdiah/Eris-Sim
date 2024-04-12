package utils

value class Color(val num: Int) {
    val red
        get() = num and 0xff

    val green
        get() = (num shr 8) and 0xff
    val blue
        get() = (num shr 16) and 0xff

    val alpha: Int
        get() {
            val n = num
            return js("(n >>> 24) & 255")
        }


    fun multiply(v: Double): Color {
        return fromRGBA((red * v).toInt(), (green * v).toInt(), (blue * v).toInt())
    }

    companion object {
        fun fromRGBA(red: Int, green: Int, blue: Int, alpha: Int = 255): Color {
            val r = clamp(red, 0, 255)
            val g = clamp(green, 0, 255)
            val b = clamp(blue, 0, 255)
            val a = clamp(alpha, 0, 255)

            val num = js("(r | g << 8 | b << 16 | a << 24) >>> 0")
            return Color(num)
        }

        fun black(): Color {
            return fromRGBA(0,0,0, 255)
        }
    }
}