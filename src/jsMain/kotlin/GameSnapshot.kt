import Person.Companion.PERSON_DEATH_AGE_TICKS
import utils.*
import kotlin.random.Random

// 0.0 being midnight, 0.5 being noon.
fun tickToFractOfDay(tick: Int): Double {
    return (tick % DAY_LENGTH_TICKS).toDouble() / DAY_LENGTH_TICKS
}

fun tickToDay(tick: Int): Int {
    return (tick % YEAR_LENGTH_TICKS) / DAY_LENGTH_TICKS
}

fun tickToYear(tick: Int): Int {
    return tick / YEAR_LENGTH_TICKS
}

// Gets copied over every simulation tick of the game.
class GameSnapshot : Cloneable<GameSnapshot> {
    private var tiles: Array<Array<Array<Tile>>> =
        Array(NUM_GAME_LAYERS) { layer -> Array(GAME_WIDTH) { x -> Array(GAME_WIDTH) { y -> EmptyTile(Point(x, y)) } } }
    var entities = mutableListOf<Entity>()
    var tick = 0
    var numTrees = 0
    var numBushes = 0

    fun setTileIfAble(tile: Tile) {
        if (isValidLocation(tile.pos)) {
            setTile(tile)
        }
    }

    fun setTile(tile: Tile) {
        tiles[tile.layer][tile.pos.x.i][tile.pos.y.i] = tile
    }

    fun clearTile(tile: Tile) {
        tiles[tile.layer][tile.pos.x.i][tile.pos.y.i] = EmptyTile(tile.pos)
    }

    init {
        val rng = Random(0)
        val middle = Point.splat(GAME_WIDTH) / 2
        for (x in 0 until GAME_WIDTH) {
            for (y in 0 until GAME_WIDTH) {
                var p = Point(x, y) / GAME_WIDTH
                p *= 2.0
                this.tiles[LAYER_FLOORING][x][y] = GrassTile(Point(x, y))
                val trees = fbm(p.x, p.y, 0.0, 4, 0.8)
                val bushes = fbm(p.x, p.y, 1.0, 4, 0.8)

                val distFromMiddle = (middle - Point(x, y)).len / GAME_WIDTH
                if (trees * 2.5 + distFromMiddle > 0.5) {
                    val tree = TreeTile(Point(x, y), rng.nextInt(TreeTile.TREE_DEATH_TICKS))
                    setTileIfAble(tree)
                } else if (bushes * 2.5 + distFromMiddle > 0.5) {
                    val bush = BushTile(Point(x, y), rng.nextInt(BushTile.BUSH_DEATH_TICKS))
                    setTileIfAble(bush)
                }
            }
        }

        for (i in 0 until 10) {
            entities.add(
                Person(
                    Point(rng.nextInt(5, GAME_WIDTH - 5), rng.nextInt(5, GAME_WIDTH - 5)),
                    rng.nextInt(
                        PERSON_DEATH_AGE_TICKS
                    )
                )
            )
        }
    }

    override fun clone(): GameSnapshot {
        val snapshot = GameSnapshot()
        for (layer in 0 until NUM_GAME_LAYERS) {
            for (x in 0 until GAME_WIDTH) {
                for (y in 0 until GAME_WIDTH) {
                    snapshot.tiles[layer][x][y] = tiles[layer][x][y].clone()
                }
            }
        }
        snapshot.entities.clear()
        snapshot.entities.addAll(entities.map { it.clone() })
        snapshot.tick = tick
        snapshot.numTrees = numTrees
        snapshot.numBushes = numBushes
        return snapshot
    }

    fun simulateStep() {
        this.tick++
        this.numTrees = 0
        this.numBushes = 0
        for (layer in 0 until NUM_GAME_LAYERS) {
            for (x in 0 until GAME_WIDTH) {
                for (y in 0 until GAME_WIDTH) {
                    val tile = this.tiles[layer][x][y]

                    if (tile is TreeTile) {
                        numTrees++
                    }
                    if (tile is BushTile) {
                        numBushes++
                    }

                    tile.tick(this)
                }
            }
        }

        for (entity in entities) {
            entity.tick(this)
        }
    }

    fun render() {
        // Render the game.
        for (layer in 0 until NUM_GAME_LAYERS) {
            for (x in 0 until GAME_WIDTH) {
                for (y in 0 until GAME_WIDTH) {
                    tiles[layer][x][y].render()
                }
            }
            SPRITES.render()
        }
        for (entity in entities) {
            entity.render()
            SPRITES.render()
        }
    }

    fun isValidLocation(pos: Point): Boolean {
        return pos.x.i in 0 until GAME_WIDTH && pos.y.i in 0 until GAME_WIDTH && tiles[LAYER_WALLS][pos.x.i][pos.y.i] is EmptyTile
    }

    fun tileAt(pos: Point): Tile {
        return tiles[LAYER_WALLS][pos.x.i][pos.y.i]
    }
}

fun isOnBoard(pos: Point): Boolean {
    return pos.x.i in 0 until GAME_WIDTH && pos.y.i in 0 until GAME_WIDTH
}