import games.perses.kudens.sprite.Sprite
import games.perses.kudens.sprite.SpriteBatch
import games.perses.kudens.texture.Textures

const val GAME_WIDTH = 16
const val FRAME_SIZE = 1280

const val DAY_LENGTH_TICKS = 24 * 2
// We're on Mercury
const val YEAR_LENGTH_TICKS = 80 * DAY_LENGTH_TICKS

const val NUM_GAME_LAYERS = 2
const val LAYER_FLOORING = 0
const val LAYER_WALLS = 1

// Game lasts for 200 years.
const val TOTAL_GAME_TICKS = YEAR_LENGTH_TICKS * 100


var SPRITES = SpriteBatch()

var GRASS_TEXTURE = Sprite("Grass")
var TREE_TEXTURE = Sprite("Tree")
var EMPTY_TEXTURE = Sprite("Empty")
var MAN_TEXTURE = Sprite("Man")
var HOUSE_TEXTURE = Sprite("House")
var BUSH_TEXTURE = Sprite("Bush")
var BERRY_BUSH_TEXTURE = Sprite("Berry Bush")

fun loadTextures() {
    Textures.load("Grass", "Tiles/Grass.png")
    Textures.load("Tree", "Tiles/Tree.png")
    Textures.load("Empty", "Tiles/Empty.png")
    Textures.load("Man", "Tiles/Man.png")
    Textures.load("House", "Tiles/House.png")
    Textures.load("Bush", "Tiles/Bush.png")
    Textures.load("Berry Bush", "Tiles/Berry Bush.png")
}