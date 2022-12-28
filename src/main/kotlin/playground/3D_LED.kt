package playground

import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.camera.Orbital
import org.openrndr.math.Spherical
import org.openrndr.math.Vector3
import utils.displayLinesOfText
import utils.map
import utils.showCoordinateSystem

fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
        multisample = WindowMultisample.SampleCount(4)
    }
    program {
        val cam = Orbital().apply {
            eye = Vector3.UNIT_Z * 150.0
            camera.depthTest = false
        }

        val linNum = 8
        val startLen = 30.0
        val endLen = 90.0

        val ledList = List(linNum) { i ->
            val phi = i.map(0, linNum, 0.0, 360.0)
            val spherical = Spherical(90.0, phi, 1.0)
            val base = Vector3.fromSpherical(spherical)
            val brg = i.map(-1, linNum, 0.0, 1.0)
            LinearLED(base * startLen, base * endLen, brg)
        }
        var hitIndex = 0
        val dampFac = 0.98

        // MAIN
        extend(cam)
        extend {
            if (frameCount%30==0) {
                hitIndex++
                hitIndex %= linNum
                ledList[hitIndex].brightness = 1.0
            }

            ledList.forEachIndexed { i, line ->
                drawer.isolated {
                    stroke = ColorRGBa.PINK
                    strokeWeight = i * 10.0 + 5.0
                    fill = ColorRGBa.PINK

                    stroke = stroke!!.shade(line.brightness)
                    lineSegment(line.start, line.end)

                    line.brightness *= dampFac
                }
            }
        }

        // DEBUG
        extend {
            drawer.showCoordinateSystem(10.0)
            drawer.translate(-250.0, 150.0, -30.0)
            drawer.rotate(Vector3.UNIT_X, 180.0)
            drawer.scale(.5)
            drawer.displayLinesOfText(
                listOf(
                    "Controls:",
                    "Mouse      - Hold LMB and drag to change view direction",
                    "W/S        - Move forwards/backwards",
                    "A/D        - Move left/right",
                    "Q/E        - Move up/down",
                    "ESCAPE     - Close application"
                )
            )
        }

        keyboard.keyDown.listen {
            when(it.key) {
                KEY_ESCAPE -> application.exit()
            }
        }
    }
}

data class LinearLED(val start: Vector3, val end: Vector3, var brightness: Double = 0.0)