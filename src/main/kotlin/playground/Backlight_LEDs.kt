package playground

import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.MouseButton
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.ColorRGBa.Companion.PINK
import org.openrndr.draw.Drawer
import org.openrndr.draw.shadeStyle
import org.openrndr.math.Vector2
import org.openrndr.panel.elements.round
import org.openrndr.shape.Shape
import org.openrndr.shape.Triangle

fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
    program {

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
            ledList.forEach {
                drawer.drawBacklightLED(it, 1.0)
            }

            val led = getTriangleLED(mouse.position, seconds*20.0, color)
            drawer.drawBacklightLED(led, .4)
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> application.exit()
            }
        }
        mouse.buttonDown.listen {
            when (it.button) {
                MouseButton.LEFT ->addLED(getTriangleLED(mouse.position, seconds*20.0, color))
                else -> return@listen
            }
        }
    }
}

fun getTriangleLED(pos: Vector2, rot: Double, color: ColorRGBa = PINK) : BacklightLED {
    val off = Vector2(30.0, 0.0)
    val posList = List(3) { pos + off.rotate(360.0/3*it + rot) }
    val shape = Triangle(posList[0], posList[1], posList[2]).shape
    return BacklightLED(shape, pos, color)
}

data class BacklightLED(val shape: Shape, val center: Vector2, val color: ColorRGBa)

fun Drawer.drawBacklightLED(backlightLED: BacklightLED, opacity: Double = 1.0) {
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
    this.fill = color.opacify(opacity)
    this.rectangle(0.0, 0.0, width.toDouble(), height.toDouble())

    popStyle()
    popTransforms()

    this.fill = color
    this.shape(shape)
}