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

        // Data Layer
        // (also Control Scheme)
        // Sets up data containers, namely keyMap and toggleMap.
        // Defines visual elements and how they are bound to the data

        // Keep both even ! Some effects rely on rows == cols == 0 (mod 2)
        val rows = 4
        val cols = 8

        // Hardcoded key layout for grid control
        val keyMap = listOf(
            "12345678", "qwertzui", "asdfghjk", "yxcvbnm,"
        ).map { it.splitByLength() }

        val toggleMap = keyMap.map { col -> col.map { 0.0 }.toMutableList() }.toMutableList()

        // Visual grid layout
        val grid = drawer.bounds.grid(cols , rows, 200.0, 200.0, 10.0, 10.0)

        // randomColorMode enables noise-based color choice
        var randomColorMode = false
        val hsvRed = WHITE.toHSVa().copy(s = 1.0)
        val noiseFac = 0.01

        // Multi Position per KeyEvent
        // Critical feature for making this fun !

        // Cyclical flag, element of multiPosBlocks.indices = [0 to n-1]
        // Just holds an integer representation of the multi position trigger mode,
        // short multiMode.
        var multiMode = 0

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
            },
            { i, j ->
                // Select all even shifts in both directions
                List(rows) { it }.filter { (it+i)%2==0 }.map {i_ ->
                    List(cols) { it }.filter { (it+j)%2==0 }.map { j_ ->
                        Pair(i_, j_)
                    }
                }.flatten()
            }
        )

        // Turns grid coords (i, j) into a list of IntPairs.
        // The entries are based on the current multiMode.
        fun getMultiPos(i: Int, j: Int): IntPairList {
            return multiPosBlocks[multiMode].invoke(i, j)
        }

        // Define a block to be executed on the respective positions
        // yielded from getMultiPos.
        fun onMultiPos(keyName: String, block: (i: Int, j: Int) -> Unit) {
            val pos = getTupleIndex(keyName, keyMap) ?: return

            val multiPos = getMultiPos(pos[0], pos[1])
            multiPos.forEach { pos ->
                block(pos.first, pos.second)
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
                    onMultiPos(it.name) { i, j ->
                        toggleMap[i][j] = 1.0
                    }
                }
            }
        }

        // // KeyUp events
        keyboard.keyUp.listen {
            onMultiPos(it.name) { i, j ->
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
