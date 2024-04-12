import games.perses.kudens.sprite.Sprite
import utils.*
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.time.times

open class Entity(var pos: Point, var sprite: Sprite, var scale: Double = 1.0) : Cloneable<Entity> {
    var oldPos: Point = pos

    override fun clone(): Entity {
        return Entity(pos.cpy, sprite)
    }

    open fun render() {
        val scaling = FRAME_SIZE / GAME_WIDTH
        val picSize = 128

        val renderPos = oldPos.lerp(pos, globalRenderFraction)

        SPRITES.draw(
            sprite, renderPos.x.toFloat() * scaling + scaling / 2, renderPos.y.toFloat() * scaling + scaling / 2,
            scale.f * scaling / picSize.f
        )
    }

    open fun tick(world: GameSnapshot) {
        // Do nothing yet.
    }

    fun moveTo(newPos: Point) {
        oldPos = pos
        pos = newPos
    }
}

class Person(pos: Point, var age: Int) : Entity(pos, MAN_TEXTURE) {
    var health = 1.0
    var food = 10
    var woodCarried = 0
    var oreCarried = 0
    var homePosition: Point? = null

    init {
        evalScale()
    }

    override fun render() {
        super.render()


    }

    companion object {
        const val PERSON_ADULT_TICKS = 18 * YEAR_LENGTH_TICKS
        const val PERSON_DEATH_AGE_TICKS = 60 * YEAR_LENGTH_TICKS
        const val HOME_WOOD_COST = 100
        const val MAX_FOOD = 10
    }

    private fun evalScale() {
        this.scale = min(0.2 + 0.8 * age / PERSON_ADULT_TICKS, 1)
    }

    override fun clone(): Entity {
        val p = Person(pos.cpy, age)
        p.health = health
        p.food = food
        p.woodCarried = woodCarried
        p.oreCarried = oreCarried
        p.homePosition = homePosition?.cpy
        return p
    }

    private fun die(world: GameSnapshot) {
        // Remove your home
        homePosition?.let {
            world.clearTile(world.tileAt(it))
        }
        world.entities.remove(this)
    }

    override fun tick(world: GameSnapshot) {
        age++

        evalScale()

        if (food < 0) {
            health -= 0.1
        }
        health += 0.01

        if (health < 0) {
            die(world)
            return
        }

        if (age % DAY_LENGTH_TICKS == 0) {
            food--
        }

        if (age > PERSON_DEATH_AGE_TICKS) {
            die(world)
            return
        }

        val rng = hash(age + (pos.x.i + pos.y.i * GAME_WIDTH) * TOTAL_GAME_TICKS)
        val dx = rng % 3 - 1
        val dy = (rng / 3) % 3 - 1

        val tileHere = world.tileAt(pos)
        var doneAction = false

        if (age > PERSON_ADULT_TICKS) {
            if (homePosition == null && woodCarried >= HOME_WOOD_COST && tileHere is EmptyTile) {
                world.setTile(HomeTile(pos, this))
                woodCarried -= HOME_WOOD_COST
                doneAction = true
                homePosition = pos
            } else {
                // If at home, and have full food, make child
                val homePos = homePosition
                if (homePos != null && homePos.x == pos.x && homePos.y == pos.y && food >= MAX_FOOD) {
                    val newPos = pos + Point(dx, dy)
                    if (isOnBoard(newPos)) {
                        world.entities.add(Person(newPos, 0))
                        food -= 5
                        doneAction = true
                    }
                }
            }
        }

        if (age > YEAR_LENGTH_TICKS * 12) {
            // Person can cut wood
            if (tileHere is TreeTile && woodCarried < HOME_WOOD_COST && world.numTrees > 4) {
                woodCarried += 1
                tileHere.age -= YEAR_LENGTH_TICKS
                if (tileHere.age < 0) {
                    world.clearTile(tileHere)
                }
                doneAction = true
            }
        }

        if (food < MAX_FOOD && tileHere is BushTile && tileHere.berryCount > 3 && world.numBushes > 4) {
            val foodToTake = min(MAX_FOOD - food, tileHere.berryCount)
            food += foodToTake
            tileHere.berryCount -= foodToTake
            tileHere.age -= DAY_LENGTH_TICKS * 4
            if (tileHere.age < 0) {
                world.clearTile(tileHere)
            }
            doneAction = true
        }

        if (!doneAction) {
            val newPos = pos + Point(dx, dy)
            if (isOnBoard(newPos)) {
                moveTo(newPos)
            } else {
                moveTo(pos)
            }
        } else {
            moveTo(pos)
        }

    }
}