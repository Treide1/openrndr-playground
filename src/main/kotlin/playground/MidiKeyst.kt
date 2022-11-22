import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa.Companion.BLUE
import org.openrndr.color.ColorRGBa.Companion.WHITE
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shapes.grid
import utils.getTupleIndex
import utils.splitByLength
import kotlin.math.min


/**
 * Press keys on your keyboard in a 4x8 grid, like this:
 * (1 .. 8)
 * (......)
 * (y .. ,)
 *
 * Pressing keys bumps up their color, which fades over time.
 * Releasing will fade them off shortly after.
 *
 * Hit ESC to quit.
 * Hit SPACE to toggle color randomizer.
 * Hit '+' to change the position mode (cycles through fun modes, keeps the key binds)
 */
fun main() = application {
    configure {
        // width = 1920
        // height = 1080
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
        title = "MidiKeys Alpha-Version 1.1"
    }

    program {
        // Keep both even ! Some effects rely on rows == cols == 0 (mod 2)
        val rows = 4
        val cols = 8

        val keyMap = listOf(
            "12345678", "qwertzui", "asdfghjk", "yxcvbnm,"
        ).map { it.splitByLength() }

        val toggleMap = keyMap.map { col -> col.map { 0.0 }.toMutableList() }.toMutableList()

        val grid = drawer.bounds.grid(cols , rows, 200.0, 200.0, 10.0, 10.0)

        var randomColorMode = false
        val hsvRed = WHITE.toHSVa().copy(s = 1.0)
        val noiseFac = 0.01

        // Multi Position per KeyEvent
        // Press "+" in application -> Cycles through the modes below.
        val multiPosBlocks = mutableListOf<((i: Int, j: Int) -> IntPairList)>(
            { i, j ->
                // Default mode: single position, no duplication
                listOf(Pair(i, j))
            },
            { i, j ->
                // Shift-Add by half in both directions (x and y)
                val otherI = (i+2)%4
                val otherJ = (j+4)%8
                listOf(i, otherI).map { i_ ->
                    listOf(j, otherJ).map {j_ ->
                        Pair(i_, j_)
                    }
                }.flatten()
            },
            { i, j ->
                // Mirror-Add along center axis
                val otherI = (4-1)-i
                val otherJ = (8-1)-j
                listOf(i, otherI).map { i_ ->
                    listOf(j, otherJ).map {j_ ->
                        Pair(i_, j_)
                    }
                }.flatten()
            }
        )

        // Cyclical flag, element of multiPosBlocks.indices = [0 to n-1]
        var multiMode = 0

        fun getMultiPos(pos: List<Int>?): IntPairList {
            if (pos == null) return listOf()

            val i = pos[0]
            val j = pos[1]

            return multiPosBlocks[multiMode].invoke(i, j)
        }

        fun onMultiPos(keyName: String, block: (pos: IntPair) -> Unit) {
            val pos = getTupleIndex(keyName, keyMap)
            val multiPos = getMultiPos(pos)
            multiPos.forEach { pos ->
                block(pos)
            }
        }

        // Input layer.
        // Just registers keys with data manipulations.
        // In that case, the data is the mutable toggleMap being manipulated
        // by the registered effects.

        // // KeyDown events
        keyboard.keyDown.listen {
            when {
                it.key == KEY_ESCAPE -> application.exit()
                it.key == KEY_SPACEBAR -> randomColorMode = !randomColorMode

                // This updates the flag by +1
                // and keeping it in range, namely an element of multiPosBlocks.indices
                it.name == "+" -> multiMode = (multiMode+1) % multiPosBlocks.size

                // Effect "Attack"
                // On hit, set light to 100%.
                else -> {
                    onMultiPos(it.name) { pos ->
                        val i = pos.first
                        val j = pos.second

                        toggleMap[i][j] = 1.0
                    }
                }
            }
        }

        // // KeyUp events
        keyboard.keyUp.listen {
            onMultiPos(it.name) { pos ->
                val i = pos.first
                val j = pos.second

                toggleMap[i][j] = min(toggleMap[i][j], 0.1)
            }
        }

        // Visual layer.
        // // Draw extension. Just visualizes data.
        extend {
            drawer.stroke = WHITE
            drawer.fill = null
            grid.forEachIndexed { i, col -> col.forEachIndexed { j, rect ->
                toggleMap[i][j] *= 0.98
                val rectVal = toggleMap[i][j]

                val targetColor = if (!randomColorMode) BLUE else hsvRed.copy(h = Random.simplex(i*noiseFac, j*noiseFac)*720).toRGBa()
                val color = targetColor.shade(rectVal)
                drawer.fill = color
                drawer.rectangle(rect)
            } }
        }

        // Those layers suffice for generic keystroke-to-visuals applications.
    }
}

// Type aliases for better readability.
typealias IntPair = Pair<Int, Int>
typealias IntPairList = List<IntPair>

