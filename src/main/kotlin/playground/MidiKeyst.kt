import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.ColorRGBa.Companion.BLUE
import org.openrndr.color.ColorRGBa.Companion.PINK
import org.openrndr.color.ColorRGBa.Companion.WHITE
import org.openrndr.color.rgb
import org.openrndr.draw.loadFont
import org.openrndr.draw.loadImage
import org.openrndr.draw.tint
import org.openrndr.extensions.Screenshots
import org.openrndr.extensions.*
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shapes.grid
import org.openrndr.math.Vector2
import utils.getTupleIndex
import utils.splitByLength
import java.util.Vector
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


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
 */
fun main() = application {
    configure {
        width = 1920
        height = 1080
        title = "MidiKeys Example"
    }

    program {
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

        keyboard.keyDown.listen {
            when {
                it.key == KEY_ESCAPE -> application.exit()
                it.key == KEY_SPACEBAR -> randomColorMode = !randomColorMode
                else -> {
                    val pos = getTupleIndex(it.name, keyMap)
                    pos?.let { toggleMap[pos[0]][pos[1]] = 1.0 }
                }
            }
        }

        keyboard.keyUp.listen {
            val pos = getTupleIndex(it.name, keyMap)
            pos?.let { toggleMap[pos[0]][pos[1]] = min(toggleMap[pos[0]][pos[1]], 0.1) }
        }

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
    }
}
