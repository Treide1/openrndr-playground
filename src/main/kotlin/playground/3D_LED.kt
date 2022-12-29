package playground

import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.camera.Orbital
import org.openrndr.extra.meshgenerators.boxMesh
import org.openrndr.extra.meshgenerators.cylinderMesh
import org.openrndr.math.Spherical
import org.openrndr.math.Vector3
import utils.displayLinesOfText
import utils.getAngle
import utils.map
import utils.showCoordinateSystem

/**
 * Showcase for LEDs in 3D space.
 *
 * Simple rose of LinearLEDs with cyclic flashing and overall fading.
 * Move around in 3D space and change between different styles of LEDs.
 */
fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
        multisample = WindowMultisample.SampleCount(4)
    }
    program {
        // SETUP
        // Camera
        val cam = Orbital().apply {
            eye = Vector3.UNIT_Z * 150.0
        }

        // LinearLED init
        val linNum = 8
        val startLen = 30.0
        val endLen = 90.0

        val ledList = List(linNum) { i ->
            val phi = i.map(0, linNum, 0.0, 360.0)
            val spherical = Spherical(90.0, phi, 1.0)
            val base = Vector3.fromSpherical(spherical)
            LinearLED(base * startLen, base * endLen, 0.8)
        }

        // LinearLED flash management
        var flashIndex = 0
        val dampFac = 0.98

        // LinearLED style management
        val ledStyleList = LedStyle.values()
        var ledStyleIndex = 0
        fun getLedStyle() = ledStyleList[ledStyleIndex] // Low-cost lookup. Clean solution is a mutable.

        // MAIN
        extend(cam)
        extend {
            // Perform flash
            if (frameCount%30==0) {
                flashIndex++
                flashIndex %= linNum
                ledList[flashIndex].brightness = 1.0
            }

            // Draw with style
            ledList.forEach { line ->
                when (getLedStyle()) {
                    LedStyle.LINE -> drawer.drawLineLED(line)
                    LedStyle.RECT -> drawer.drawRectLED(line)
                    LedStyle.BOX -> drawer.drawBoxLED(line)
                    LedStyle.CYLINDER -> drawer.drawCylinderLED(line)
                }

                // Apply fade (of flash)
                line.brightness *= dampFac
            }
        }

        // QOL
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
                    "SPACEBAR   - Change draw style (current: ${getLedStyle()})",
                    "ESCAPE     - Close application"
                )
            )
        }

        // KEY BINDS
        keyboard.keyDown.listen {
            when(it.key) {
                KEY_ESCAPE -> application.exit()
                KEY_SPACEBAR -> {
                    ledStyleIndex++
                    ledStyleIndex %= ledStyleList.size
                }
            }
        }
    }
}

/**
 * Draw the given [LinearLED] using a 1D LineSegment.
 */
fun Drawer.drawLineLED(line: LinearLED) {
    this.isolated {
        stroke = ColorRGBa.PINK.shade(line.brightness)
        lineSegment(line.start, line.end)
    }
}

/**
 * Draw the given [LinearLED] using a 2D rectangle.
 */
fun Drawer.drawRectLED(line: LinearLED) {
    val rectWidth = 4.0

    // Only works for LinearLEDs in the x-y-plane. Done this way for demo only.
    val start = line.start.xy
    val end = line.end.xy
    val diff = end - start

    this.isolated {
        stroke = null
        fill = ColorRGBa.PINK.shade(line.brightness)
        translate(start)
        rotate(-diff.getAngle() + 90.0)
        rectangle(0.0, -rectWidth/2.0, diff.length, rectWidth)
    }
}

/**
 * Draw the given [LinearLED] using a boxMesh.
 */
fun Drawer.drawBoxLED(line: LinearLED) {
    val boxWidth = 4.0

    val start = line.start
    val end = line.end
    val diff = end - start
    val angRot = -diff.xy.getAngle() + 90.0

    val box = boxMesh(diff.length, boxWidth, boxWidth)

    this.isolated {
        stroke = null // Unused for meshes
        fill = ColorRGBa.PINK.shade(line.brightness)
        translate(start*2.0)
        rotate(angRot)
        vertexBuffer(box, DrawPrimitive.TRIANGLES)
    }
}

/**
 * Draw the given [LinearLED] using a cylinderMesh.
 */
fun Drawer.drawCylinderLED(line: LinearLED) {
    val cylWidth = 4.0

    val start = line.start
    val end = line.end
    val diff = end - start
    val angRot = -diff.xy.getAngle() + 90.0

    val cyl = cylinderMesh(radius=cylWidth/2.0, length=diff.length)

    this.isolated {
        stroke = null // Unused for meshes
        fill = ColorRGBa.PINK.shade(line.brightness)
        translate(start*2.0)
        rotate(angRot)
        rotate(Vector3.UNIT_Y, 90.0)
        vertexBuffer(cyl, DrawPrimitive.TRIANGLE_FAN)
    }
}

/**
 * Data class representing an LED in a straight line from [start] to [end] in 3D space.
 * The LED has a certain [brightness] that can be updated.
 * How to draw an LED is managed by [LedStyle].
 */
data class LinearLED(val start: Vector3, val end: Vector3, var brightness: Double = 0.0)

/**
 * Enumerates draw styles for the abstract [LinearLED].
 */
enum class LedStyle {
    LINE,
    RECT,
    BOX,
    CYLINDER,
}
