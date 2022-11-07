package envelope_capture

import animation.SecondOrderDynamics
import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.color.presets.*
import org.openrndr.extra.shadestyles.linearGradient
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.contour
import utils.map
import utils.vh
import utils.vw
import kotlin.math.pow

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

        val n = 30
        val fac = .99
        val settle = .25
        val x0 = vw(.75 - .15)
        val x1 = vw(.75 + .15)
        val y0 = vh(.2)
        val y1 = vh(.8)
        val waveList = MutableList(n) {0.0}
        val waveWeights = List(n) {fac.pow(it)}
        extend {
            waveList.add(0, sod.y)
            waveList.removeLast()

            val c = contour {
                waveList.forEachIndexed { i, v ->
                    val relV = v.map(-1.0, 3.0, 0.0, 1.0)
                    val x = i.map(0, n-1, x0, x1)
                    val y = ((relV - settle) * waveWeights[i] + settle).map(0.0, 1.0, y0, y1)

                    moveOrLineTo(x, y)
                }

                val settleY = vh(settle.map(0.0, 1.0, .2, .8))
                lineTo(x1, settleY)
                lineTo(x0, settleY)
                close()
            }

            val baseCol = ColorRGBa.PINK.mix(ColorRGBa.PURPLE, sod.y*.5)
            drawer.isolated {
                shadeStyle = linearGradient(baseCol, ColorRGBa.BLACK,
                    offset = Vector2(.1, 0.0), rotation = -80.0)
                stroke = null

                contour(c)
            }
        }

        val textLines = listOf("Controls", "q: To 1", "no key press: 0", "w: to -1", "e: to -2", "r to -3")
        extend {

            drawer.isolated {
                val x = 20.0
                var y = 40.0
                val yOff = 30.0
                stroke = ColorRGBa.WHITE
                fill = ColorRGBa.WHITE
                textLines.forEach {
                    text(it, x, y)
                    y+=yOff
                }

            }
        }

        keyboard.keyDown.listen {
            when(it.key) {
                KEY_ESCAPE -> application.exit()
            }
        }

    }
}