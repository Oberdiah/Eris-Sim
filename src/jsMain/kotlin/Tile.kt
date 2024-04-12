import TreeTile.Companion.TREE_ADULT_TICKS
import games.perses.kudens.sprite.Sprite
import utils.*
import kotlin.math.PI
import kotlin.random.Random

abstract class Tile(val pos: Point, val layer: Int, var sprite: Sprite, var scale: Double = 1.0) : Cloneable<Tile> {
    fun render() {
        val scaling = FRAME_SIZE / GAME_WIDTH
        val picSize = 128

        SPRITES.draw(
            sprite, pos.x.toFloat() * scaling + scaling / 2, pos.y.toFloat() * scaling + scaling / 2,
            scale.f * scaling / picSize.f
        )
    }

    open fun tick(world: GameSnapshot) {
        // Do nothing yet.
    }
}

class EmptyTile(p: Point) : Tile(p, -1, EMPTY_TEXTURE) {
    override fun clone(): Tile {
        return EmptyTile(pos.cpy)
    }
}

abstract class FloorTile(p: Point, sprite: Sprite) : Tile(p, LAYER_FLOORING, sprite)
abstract class WallTile(p: Point, sprite: Sprite) : Tile(p, LAYER_WALLS, sprite)

class GrassTile(p: Point) : FloorTile(p, GRASS_TEXTURE) {
    override fun clone(): Tile {
        return GrassTile(pos.cpy)
    }
}

class TreeTile(p: Point, var age: Int) : WallTile(p, TREE_TEXTURE) {
    override fun clone(): Tile {
        return TreeTile(pos.cpy, age)
    }

    init {
        evalScale()
    }

    private fun evalScale() {
        this.scale = min(0.2 + 0.8 * age / TREE_ADULT_TICKS, 1)
    }

    companion object {
        const val TREE_ADULT_TICKS = 10 * YEAR_LENGTH_TICKS
        const val TREE_DEATH_TICKS = 20 * YEAR_LENGTH_TICKS
    }

    override fun tick(world: GameSnapshot) {
        age++

        if (age % YEAR_LENGTH_TICKS == 0 && age > TREE_ADULT_TICKS) {
            val rng = hash(age + (pos.x.i + pos.y.i * GAME_WIDTH) * TOTAL_GAME_TICKS)
            // Spread baby tree
            val dir = rng % 128
            val newPos = pos + Point.fromAngle(dir * PI / 2).ensureInt()
            if (world.isValidLocation(newPos)) {
                world.setTile(TreeTile(newPos, 0))
            }
        }

        evalScale()

        if (age > TREE_DEATH_TICKS) {
            // Death
            world.clearTile(this)
        }
    }
}

class BushTile(p: Point, var age: Int) : WallTile(p, BUSH_TEXTURE) {
    var berryCount = age % 8

    override fun clone(): Tile {
        return BushTile(pos.cpy, age)
    }

    init {

        evalRender()
    }

    private fun evalRender() {
        this.scale = min(0.2 + 0.8 * age / BUSH_ADULT_TICKS, 1)

        if (berryCount > 3) {
            sprite = BERRY_BUSH_TEXTURE
        } else {
            sprite = BUSH_TEXTURE
        }
    }

    companion object {
        const val BUSH_ADULT_TICKS = 4 * YEAR_LENGTH_TICKS
        const val BUSH_DEATH_TICKS = 10 * YEAR_LENGTH_TICKS
    }

    override fun tick(world: GameSnapshot) {
        age++

        if (age % DAY_LENGTH_TICKS == 0) {
            berryCount++
            if (berryCount > 8) {
                berryCount = 8
            }
        }

        if (age % YEAR_LENGTH_TICKS == 0 && age > BUSH_ADULT_TICKS) {
            // Spread baby bush
            val rng = hash(age + (pos.x.i + pos.y.i * GAME_WIDTH) * TOTAL_GAME_TICKS)
            val dir = rng % 128
            val newPos = pos + Point.fromAngle(dir * PI / 4).ensureInt()
            if (world.isValidLocation(newPos)) {
                world.setTile(BushTile(newPos, 0))
            }
        }

        evalRender()

        if (age > BUSH_DEATH_TICKS) {
            // Death
            world.clearTile(this)
        }
    }
}

class HomeTile(p: Point, val owner: Person) : WallTile(p, HOUSE_TEXTURE) {
    override fun clone(): Tile {
        return HomeTile(pos.cpy, owner)
    }
}