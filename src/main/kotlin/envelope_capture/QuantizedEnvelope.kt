package envelope_capture

import animation.SecondOrderDynamics
import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.color.presets.*
import org.openrndr.math.map
import utils.map
import utils.vh
import utils.vw

fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }

    program {

        var level = 0
        val levelMap = hashMapOf("q" to -1, null to 0, "w" to 1, "e" to 2, "r" to 3)
        extend {
            val priorityKey = keyboard.pressedKeys.firstOrNull { levelMap.containsKey(it) }
            level = levelMap[priorityKey]!!
        }

        extend {
            drawer.isolated {
                val x = vw(.5)
                val y = level.map(-1, 3, vh(.2), vh(.8) )
                fill = ColorRGBa.DARK_RED
                stroke = ColorRGBa.WHITE
                circle(x, y, 10.0)
            }
        }

        val sod = SecondOrderDynamics(2.0, 1.0, -.05, 0.0)
        extend {
            if (deltaTime > 0.0) sod.update(deltaTime, level.toDouble())

            drawer.isolated {
                val x = vw(.25)
                val y = sod.y.map(-1.0, 3.0, vh(.2), vh(.8) )
                fill = ColorRGBa.DARK_BLUE
                stroke = ColorRGBa.WHITE
                circle(x, y, 10.0)
            }
        }


        keyboard.keyDown.listen {
            when(it.key) {
                KEY_ESCAPE -> application.exit()
            }
        }


    }
}