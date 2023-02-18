package playground

import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.color.ColorRGBa.Companion.PINK
import org.openrndr.draw.Drawer
import org.openrndr.draw.shadeStyle
import org.openrndr.extra.noise.perlin2D
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.panel.elements.round
import org.openrndr.shape.Shape
import org.openrndr.shape.Triangle
import utils.map

fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
    program {
        mouse.cursorVisible = false

        val bpm = 122.0 // "Daily Routines" by Oliver Schories
        val secPerBeat = 60.0/bpm
        var phase = 0.0

        val colorList = listOf(
            ColorRGBa.fromHex("#dd00ff"),
            ColorRGBa.fromHex("#4000ff"),
            ColorRGBa.fromHex("#ff0090"),
            ColorRGBa.fromHex("#ffb300")
        )
        var color = colorList.first()
        val ledList = mutableListOf<BacklightLED>()

        val maxLEDs = 25
        fun addLED(backlightLED: BacklightLED) {
            ledList += backlightLED
            while (ledList.size > maxLEDs) ledList.removeFirst()

            color = colorList.random()
        }

        // DRAW
        extend {
            phase += deltaTime
            while (phase > secPerBeat) {
                phase -= secPerBeat
                repeat(4) {
                    val pos = getRandomScreenPos()
                    addLED(getTriangleLED(pos, seconds * 20.0, color))
                }
            }

            ledList.forEachIndexed { i, led ->
                val relPhase = phase/secPerBeat
                val lum = i.map(0, maxLEDs-1, 0.0, 1.0) * relPhase.map(0.0,1.0,0.8,0.2)
                drawer.drawBacklightLED(led, 0.6, lum)
            }

            //val led = getTriangleLED(mouse.position, seconds*20.0, color)
            //drawer.drawBacklightLED(led, .4)
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> application.exit()
                KEY_SPACEBAR -> phase = 0.0
            }
        }
        mouse.buttonDown.listen {
            when (it.button) {
                MouseButton.LEFT -> addLED(getTriangleLED(mouse.position, seconds*20.0, color))
                else -> return@listen
            }
        }
    }
}

var counter = 0
fun Program.getRandomScreenPos() : Vector2 {
    counter++
    val x = perlin2D(42, counter*0.01, 1.0) * 0.5 + 0.5
    val vX = x * width
    val y = perlin2D(43, counter*0.01, 1.0) * 0.5 + 0.5
    val vY = y * height
    return Vector2(vX, vY)
}


/////////////////////////////////////////////////////////////////////////////////////

fun getTriangleLED(pos: Vector2, rot: Double, color: ColorRGBa = PINK) : BacklightLED {
    val off = Vector2(30.0, 0.0)
    val posList = List(3) { pos + off.rotate(360.0/3*it + rot) }
    val shape = Triangle(posList[0], posList[1], posList[2]).shape
    return BacklightLED(shape, pos, color)
}

fun Drawer.drawBacklightLED(backlightLED: BacklightLED, opacity: Double, luminosity: Double) {
    val (shape, center, color) = backlightLED

    pushStyle()
    pushTransforms()

    this.shadeStyle = shadeStyle {
        fragmentTransform = """
            vec2 pos = c_screenPosition.xy;
            pos.x = pos.x - ${center.x.round(3)};
            pos.y = pos.y - ${center.y.round(3)};
            x_fill.rgba *= vec4(1.0/(1.0+length(pos)*0.02)*1.2);
            """.trimIndent()
    }
    val c = color.toHSVa().copy(v = luminosity, alpha = opacity).toRGBa()
    this.fill = c
    this.rectangle(0.0, 0.0, width.toDouble(), height.toDouble())

    popStyle()
    popTransforms()

    this.fill = c
    this.shape(shape)
}

/////////////////////////////////////////////////////////////////////////////////////

data class BacklightLED(val shape: Shape, val center: Vector2, val color: ColorRGBa)
