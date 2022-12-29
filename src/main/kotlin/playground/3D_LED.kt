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

fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
        multisample = WindowMultisample.SampleCount(4)
    }
    program {
        val cam = Orbital().apply {
            eye = Vector3.UNIT_Z * 150.0
        }

        val linNum = 8
        val startLen = 30.0
        val endLen = 90.0

        val ledList = List(linNum) { i ->
            val phi = i.map(0, linNum, 0.0, 360.0)
            val spherical = Spherical(90.0, phi, 1.0)
            val base = Vector3.fromSpherical(spherical)
            LinearLED(base * startLen, base * endLen, 0.8)
        }
        var hitIndex = 0
        val dampFac = 0.98

        val ledStyleList = LedStyle.values()
        var ledStyleIndex = LedStyle.BOX.ordinal
        fun getLedStyle() = ledStyleList[ledStyleIndex]

        // MAIN
        extend(cam)
        extend {
            if (frameCount%30==0) {
                hitIndex++
                hitIndex %= linNum
                ledList[hitIndex].brightness = 1.0
            }

            ledList.forEach { line ->
                when (getLedStyle()) {
                    LedStyle.LINE -> drawer.drawLineLED(line)
                    LedStyle.RECT -> drawer.drawRectLED(line)
                    LedStyle.BOX -> drawer.drawBoxLED(line)
                    LedStyle.CYLINDER -> drawer.drawCylinderLED(line)
                }

                line.brightness *= dampFac
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
                    "SPACEBAR   - Change draw style (current: ${getLedStyle()})",
                    "ESCAPE     - Close application"
                )
            )
        }

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

fun Drawer.drawLineLED(line: LinearLED) {
    this.isolated {
        stroke = ColorRGBa.PINK.shade(line.brightness)
        lineSegment(line.start, line.end)
    }
}

fun Drawer.drawRectLED(line: LinearLED) {
    val rectWidth = 4.0

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

fun Drawer.drawCylinderLED(line: LinearLED) {
    val cylWidth = 4.0

    val start = line.start
    val end = line.end
    val diff = end - start
    val angRot = -diff.xy.getAngle() + 90.0

    val cyl = cylinderMesh(radius=cylWidth, length=diff.length)

    this.isolated {
        stroke = null // Unused for meshes
        fill = ColorRGBa.PINK.shade(line.brightness)
        translate(start*2.0)
        rotate(angRot)
        rotate(Vector3.UNIT_Y, 90.0)
        vertexBuffer(cyl, DrawPrimitive.TRIANGLES)
    }
}

data class LinearLED(val start: Vector3, val end: Vector3, var brightness: Double = 0.0)

enum class LedStyle {
    LINE,
    RECT,
    BOX,
    CYLINDER,
}
